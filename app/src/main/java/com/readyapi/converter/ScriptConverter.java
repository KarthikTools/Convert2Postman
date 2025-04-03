package com.readyapi.converter;

import org.dom4j.Element;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enhanced converter for Groovy scripts to JavaScript.
 * Provides more robust handling of complex Groovy constructs.
 */
public class ScriptConverter {
    
    // Common Groovy to JavaScript conversion patterns
    private static final List<PatternReplacement> SYNTAX_CONVERSIONS = new ArrayList<>();
    
    static {
        // Initialize common conversion patterns
        
        // Groovy def keyword to JavaScript let/const
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("def\\s+(\\w+)\\s*=\\s*(.+?)(?:;|$)"),
            "let $1 = $2;"
        ));
        
        // Groovy string interpolation "${var}" to JavaScript `${var}`
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("\"(.*?)\\$\\{(.*?)\\}(.*?)\""),
            "`$1${$2}$3`"
        ));
        
        // Groovy list declaration to JavaScript array
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("\\[([^\\]]*?)\\]\\s+as\\s+List"),
            "[$1]"
        ));
        
        // Groovy map declaration to JavaScript object
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("\\[([^\\]]*?)\\]\\s+as\\s+Map"),
            "{$1}"
        ));
        
        // Groovy each closure to JavaScript forEach
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("(\\w+)\\.each\\s*\\{\\s*(?:it|(\\w+(?:,\\s*\\w+)*))\\s*->\\s*(.+?)\\s*\\}"),
            "$1.forEach(($2 != null ? $2 : 'item') => { $3 })"
        ));
        
        // Groovy collect closure to JavaScript map
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("(\\w+)\\.collect\\s*\\{\\s*(?:it|(\\w+(?:,\\s*\\w+)*))\\s*->\\s*(.+?)\\s*\\}"),
            "$1.map(($2 != null ? $2 : 'item') => { return $3; })"
        ));
        
        // Groovy find closure to JavaScript find
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("(\\w+)\\.find\\s*\\{\\s*(?:it|(\\w+(?:,\\s*\\w+)*))\\s*->\\s*(.+?)\\s*\\}"),
            "$1.find(($2 != null ? $2 : 'item') => { return $3; })"
        ));
        
        // Groovy findAll closure to JavaScript filter
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("(\\w+)\\.findAll\\s*\\{\\s*(?:it|(\\w+(?:,\\s*\\w+)*))\\s*->\\s*(.+?)\\s*\\}"),
            "$1.filter(($2 != null ? $2 : 'item') => { return $3; })"
        ));
        
        // Groovy any closure to JavaScript some
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("(\\w+)\\.any\\s*\\{\\s*(?:it|(\\w+(?:,\\s*\\w+)*))\\s*->\\s*(.+?)\\s*\\}"),
            "$1.some(($2 != null ? $2 : 'item') => { return $3; })"
        ));
        
        // Groovy every/all closure to JavaScript every
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("(\\w+)\\.(every|all)\\s*\\{\\s*(?:it|(\\w+(?:,\\s*\\w+)*))\\s*->\\s*(.+?)\\s*\\}"),
            "$1.every(($3 != null ? $3 : 'item') => { return $4; })"
        ));
        
        // Groovy println to JavaScript console.log
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("println\\s+(.+?)(?:;|$)"),
            "console.log($1);"
        ));
        
        // Groovy assert to JavaScript assertion
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("assert\\s+(.+?)(?:;|$)"),
            "if (!($1)) { throw new Error('Assertion failed: ' + $1); };"
        ));
        
        // Groovy multi-line string (""") to JavaScript template literal
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("\"\"\"([\\s\\S]*?)\"\"\""),
            "`$1`"
        ));
        
        // Groovy for loop with range to JavaScript for loop
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("for\\s*\\(\\s*(\\w+)\\s+in\\s+(\\d+)\\s*\\.\\.<\\s*(\\d+|\\w+)\\s*\\)\\s*\\{"),
            "for (let $1 = $2; $1 < $3; $1++) {"
        ));
        
        // Groovy for loop with inclusive range to JavaScript for loop
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("for\\s*\\(\\s*(\\w+)\\s+in\\s+(\\d+)\\s*\\.\\.\\s*(\\d+|\\w+)\\s*\\)\\s*\\{"),
            "for (let $1 = $2; $1 <= $3; $1++) {"
        ));
        
        // Groovy sleep to JavaScript setTimeout with await
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("sleep\\s*\\(\\s*(\\d+)\\s*\\)"),
            "await new Promise(resolve => setTimeout(resolve, $1))"
        ));
        
        // Groovy try-catch with specific exception type to JavaScript try-catch
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("try\\s*\\{([\\s\\S]*?)\\}\\s*catch\\s*\\(\\s*(\\w+)\\s+(\\w+)\\s*\\)\\s*\\{"),
            "try {$1} catch ($3) {"
        ));
        
        // Groovy elvis operator (?:) to JavaScript nullish coalescing operator (??)
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("(\\w+(?:\\.\\w+)*)\\s*\\?:\\s*(.+?)(?:;|$)"),
            "$1 ?? $2;"
        ));
        
        // Groovy safe navigation operator (?.) to JavaScript optional chaining (?.)
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("(\\w+(?:\\.\\w+)*)\\?\\.(\\w+)"),
            "$1?.$2"
        ));
    }
    
    /**
     * Convert a Groovy script to JavaScript.
     * 
     * @param groovyScript The Groovy script to convert
     * @return The converted JavaScript
     */
    public String convertGroovyToJavaScript(String groovyScript) {
        if (groovyScript == null || groovyScript.isEmpty()) {
            return "// Empty script";
        }
        
        // Check if the script contains async operations
        boolean isAsync = groovyScript.contains("sleep") || 
                         groovyScript.contains("wait") || 
                         groovyScript.contains("async") ||
                         groovyScript.contains("promise");
        
        StringBuilder jsScript = new StringBuilder();
        
        // Add documentation header
        jsScript.append("/**\n");
        jsScript.append(" * Converted from ReadyAPI Groovy script to Postman JavaScript\n");
        jsScript.append(" * Note: Some Groovy-specific features may require manual adjustment\n");
        jsScript.append(" */\n\n");
        
        // Add async wrapper if needed
        if (isAsync) {
            jsScript.append("(async function() {\n");
            jsScript.append("  try {\n");
        } else {
            jsScript.append("(function() {\n");
            jsScript.append("  try {\n");
        }
        
        // Apply all syntax conversions
        String convertedScript = groovyScript;
        for (PatternReplacement conversion : SYNTAX_CONVERSIONS) {
            convertedScript = conversion.apply(convertedScript);
        }
        
        // Add ReadyAPI context mapping
        convertedScript = addReadyApiContextMapping(convertedScript);
        
        // Add indentation to the converted script
        String[] lines = convertedScript.split("\n");
        for (String line : lines) {
            jsScript.append("    ").append(line).append("\n");
        }
        
        // Close the wrapper function
        if (isAsync) {
            jsScript.append("  } catch (error) {\n");
            jsScript.append("    console.error('Script execution error:', error);\n");
            jsScript.append("    throw error;\n");
            jsScript.append("  }\n");
            jsScript.append("})();\n");
        } else {
            jsScript.append("  } catch (error) {\n");
            jsScript.append("    console.error('Script execution error:', error);\n");
            jsScript.append("    throw error;\n");
            jsScript.append("  }\n");
            jsScript.append("})();\n");
        }
        
        return jsScript.toString();
    }
    
    /**
     * Convert a Groovy script to JavaScript with a specific script type.
     * 
     * @param groovyScript The Groovy script to convert
     * @param scriptType The type of script (e.g., "test", "library")
     * @return The converted JavaScript
     */
    public static String convertToJavaScript(String groovyScript, String scriptType) {
        ScriptConverter converter = new ScriptConverter();
        String jsScript = converter.convertGroovyToJavaScript(groovyScript);
        
        // Add additional context based on script type
        if ("library".equals(scriptType)) {
            // For libraries, wrap in a module pattern to avoid global namespace pollution
            StringBuilder libraryScript = new StringBuilder();
            libraryScript.append("// Ready API Library Script\n");
            libraryScript.append("const readyApiLibrary = (function() {\n");
            libraryScript.append("  // Library exports\n");
            libraryScript.append("  return {\n");
            libraryScript.append("    init: function() {\n");
            libraryScript.append("      // Initialize library methods\n");
            
            // Add the converted script with indentation
            String[] lines = jsScript.split("\n");
            for (String line : lines) {
                libraryScript.append("      ").append(line).append("\n");
            }
            
            libraryScript.append("    }\n");
            libraryScript.append("  };\n");
            libraryScript.append("})();\n\n");
            libraryScript.append("// Initialize the library\n");
            libraryScript.append("readyApiLibrary.init();\n");
            
            return libraryScript.toString();
        }
        
        return jsScript;
    }
    
    /**
     * Add ReadyAPI context mapping to the script.
     * 
     * @param script The script to modify
     * @return The modified script
     */
    private String addReadyApiContextMapping(String script) {
        StringBuilder mappedScript = new StringBuilder();
        
        // Add ReadyAPI context mapping
        mappedScript.append("// Map ReadyAPI context to Postman\n");
        mappedScript.append("const context = pm.variables;\n");
        mappedScript.append("const testRunner = {\n");
        mappedScript.append("  testCase: { name: pm.info.requestName },\n");
        mappedScript.append("  testSuite: { name: pm.info.requestName.split(' - ')[0] },\n");
        mappedScript.append("  getStatus: function() { return pm.response.status; },\n");
        mappedScript.append("  getResponseContent: function() { return pm.response.text(); },\n");
        mappedScript.append("  getResponseHeaders: function() { return pm.response.headers.toObject(); },\n");
        mappedScript.append("  getResponseTime: function() { return pm.response.responseTime; },\n");
        mappedScript.append("  log: function(message) { console.log(message); }\n");
        mappedScript.append("};\n\n");
        
        // Replace common ReadyAPI context references
        script = script.replace("log.", "console.");
        script = script.replace("testRunner.", "testRunner.");
        script = script.replace("context.", "context.");
        
        mappedScript.append(script);
        
        return mappedScript.toString();
    }
    
    /**
     * Helper class for pattern-based replacements.
     */
    private static class PatternReplacement {
        private final Pattern pattern;
        private final String replacement;
        
        public PatternReplacement(Pattern pattern, String replacement) {
            this.pattern = pattern;
            this.replacement = replacement;
        }
        
        public String apply(String input) {
            try {
                Matcher matcher = pattern.matcher(input);
                
                // Use StringBuilder and appendReplacement for safer processing
                StringBuilder result = new StringBuilder();
                while (matcher.find()) {
                    // Create dynamic replacement with null checks for capture groups
                    String repl = replacement;
                    for (int i = 0; i <= matcher.groupCount(); i++) {
                        String groupVal = matcher.group(i);
                        if (groupVal != null) {
                            // Properly escape $ and \ in replacement
                            groupVal = groupVal.replace("\\", "\\\\").replace("$", "\\$");
                            repl = repl.replace("$" + i, groupVal);
                        } else {
                            // Replace references to null groups with safe defaults
                            repl = repl.replace("$" + i, "");
                        }
                    }
                    
                    // Handle conditional expressions like $2 ? $2 : 'item'
                    // with appropriate null checks
                    repl = repl.replaceAll("\\(\\$\\d+ != null \\? \\$\\d+ : '([^']+)'\\)", "$1");
                    
                    // Add the replacement
                    matcher.appendReplacement(result, repl);
                }
                matcher.appendTail(result);
                return result.toString();
            } catch (Exception e) {
                // In case of regex error, log it and return the original string
                System.err.println("Error in regex replacement: " + e.getMessage());
                return input;
            }
        }
    }
}
