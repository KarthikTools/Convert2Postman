package com.readyapi.converter;

import org.dom4j.Element;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enhanced converter for Groovy scripts to JavaScript.
 * Provides comprehensive handling of ReadyAPI/SoapUI Groovy constructs for Postman.
 */
public class ScriptConverter {
    private static final Logger logger = LoggerFactory.getLogger(ScriptConverter.class);
    
    // Common Groovy to JavaScript conversion patterns
    private static final List<PatternReplacement> SYNTAX_CONVERSIONS = new ArrayList<>();
    
    static {
        // Initialize comprehensive conversion patterns
        
        // Basic conversions
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("def\\s+(\\w+)\\s*=\\s*(.+?)(?:;|$)"),
            "let $1 = $2;"
        ));
        
        // String interpolation
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("\"(.*?)\\$\\{(.*?)\\}(.*?)\""),
            "`$1${$2}$3`"
        ));
        
        // Logging
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("log\\.info\\((.+?)\\)"),
            "console.log($1);"
        ));
        
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("println\\s+(.+?)(?:;|$)"),
            "console.log($1);"
        ));
        
        // TestRunner property access
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("def\\s+(\\w+)\\s*=\\s*testRunner\\.testCase\\.testSuite\\.project\\.getPropertyValue\\(\"(.+?)\"\\)"),
            "let $1 = pm.collectionVariables.get('$2');"
        ));
        
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("testRunner\\.testCase\\.testSuite\\.project\\.setPropertyValue\\(\"(.+?)\",\\s*(.+?)\\)"),
            "pm.collectionVariables.set('$1', $2);"
        ));
        
        // Test step property access
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("def\\s+(\\w+)\\s*=\\s*testRunner\\.testCase\\.testSteps\\[\"(.+?)\"\\]\\.getPropertyValue\\(\"(.+?)\"\\)"),
            "let $1 = pm.collectionVariables.get('$2_$3');"
        ));
        
        // JSON parsing
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("def\\s+(\\w+)\\s*=\\s*new\\s+JsonSlurper\\(\\)\\.parseText\\((.+?)\\)"),
            "let $1 = JSON.parse($2);"
        ));
        
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("def\\s+(\\w+)\\s*=\\s*(\\w+)\\.(\\w+)"),
            "let $1 = $2.$3;"
        ));
        
        // Assertions
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("assert\\s+(.+?)\\s*==\\s*(.+?)(?:;|$)"),
            "pm.test('Assert: $1 equals $2', function () { pm.expect($1).to.eql($2); });"
        ));
        
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("assert\\s+(.+?)(?:;|$)"),
            "pm.test('Assert: $1 is truthy', function () { pm.expect($1).to.be.ok; });"
        ));
        
        // Type conversions
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("(int|Integer)\\s+(\\w+)\\s*=\\s*(\\w+)\\.toInteger\\(\\)"),
            "let $2 = parseInt($3);"
        ));
        
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("(float|Float|double|Double)\\s+(\\w+)\\s*=\\s*(\\w+)\\.toDouble\\(\\)"),
            "let $2 = parseFloat($3);"
        ));
        
        // Groovy list operations
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("\\[([^\\]]*?)\\]\\s+as\\s+List"),
            "[$1]"
        ));
        
        // Groovy map declaration
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("\\[([^\\]]*?)\\]\\s+as\\s+Map"),
            "{$1}"
        ));
        
        // Groovy collection operations
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("(\\w+)\\.each\\s*\\{\\s*(?:it|(\\w+(?:,\\s*\\w+)*))\\s*->\\s*(.+?)\\s*\\}"),
            "$1.forEach(($2 != null ? $2 : 'item') => { $3 })"
        ));
        
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("(\\w+)\\.collect\\s*\\{\\s*(?:it|(\\w+(?:,\\s*\\w+)*))\\s*->\\s*(.+?)\\s*\\}"),
            "$1.map(($2 != null ? $2 : 'item') => { return $3; })"
        ));
        
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("(\\w+)\\.find\\s*\\{\\s*(?:it|(\\w+(?:,\\s*\\w+)*))\\s*->\\s*(.+?)\\s*\\}"),
            "$1.find(($2 != null ? $2 : 'item') => { return $3; })"
        ));
        
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("(\\w+)\\.findAll\\s*\\{\\s*(?:it|(\\w+(?:,\\s*\\w+)*))\\s*->\\s*(.+?)\\s*\\}"),
            "$1.filter(($2 != null ? $2 : 'item') => { return $3; })"
        ));
        
        // Context variables
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("context\\.expand\\('\\$\\{(.+?)\\}'\\)"),
            "pm.variables.get('$1')"
        ));
        
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("context\\.testCase\\.setPropertyValue\\(\"(.+?)\",\\s*(.+?)\\)"),
            "pm.variables.set('$1', $2);"
        ));
        
        // ReadyAPI specific constructs
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("testRunner\\.testCase\\.getTestStepByName\\(\"(.+?)\"\\)"),
            "// Postman equivalent: Use pm.variables to access step data\n// Original: testRunner.testCase.getTestStepByName('$1')"
        ));
        
        // File operations
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("new\\s+File\\((.+?)\\)"),
            "// Note: File operations are limited in Postman. Using console output instead.\n// Original: new File($1)"
        ));
        
        // HTTP requests
        SYNTAX_CONVERSIONS.add(new PatternReplacement(
            Pattern.compile("def\\s+(\\w+)\\s*=\\s*testRunner\\.testCase\\.testSteps\\[\"(.+?)\"\\]\\.run\\((.+?)\\)"),
            "// Note: Step execution not available in Postman.\n// Original: $1 = testRunner.testCase.testSteps[\"$2\"].run($3)"
        ));
    }
    
    /**
     * Convert a Groovy script to JavaScript with detailed analysis and pattern matching.
     * 
     * @param groovyScript The Groovy script to convert
     * @param scriptType The type of script (e.g., "library", "test", "setup", "teardown")
     * @return The converted JavaScript
     */
    public static String convertToJavaScript(String groovyScript, String scriptType) {
        if (groovyScript == null || groovyScript.isEmpty()) {
            return "// Empty " + scriptType + " script";
        }
        
        StringBuilder jsScript = new StringBuilder();
        
        // Add documentation header
        jsScript.append("/**\n");
        jsScript.append(" * Converted from ReadyAPI Groovy " + scriptType + " to Postman JavaScript\n");
        jsScript.append(" * Some Groovy-specific features may require manual adjustment\n");
        jsScript.append(" */\n\n");
        
        try {
            // Process the script line by line for better analysis
            String[] lines = groovyScript.split("\n");
            List<String> processedLines = new ArrayList<>();
            boolean hasJsonSlurper = false;
            boolean hasStringToStringMap = false;
            
            // Pre-process to detect imports and context
            for (String line : lines) {
                if (line.contains("JsonSlurper")) hasJsonSlurper = true;
                if (line.contains("StringToStringMap")) hasStringToStringMap = true;
                
                // Skip typical imports - we'll handle these with appropriate replacements
                if (line.trim().startsWith("import ")) continue;
                
                // Skip package definitions
                if (line.trim().startsWith("package ")) continue;
                
                processedLines.add(line);
            }
            
            // Add appropriate Postman equivalents based on detected imports
            if (hasJsonSlurper) {
                jsScript.append("// JSON parsing support\n");
                jsScript.append("// Note: Postman has native JSON support via pm.response.json()\n\n");
            }
            
            if (hasStringToStringMap) {
                jsScript.append("// Header management setup\n");
                jsScript.append("let headers = {};\n\n");
            }
            
            // Add ReadyAPI context mapping based on script type
            if ("test".equals(scriptType)) {
                jsScript.append("// Access response body\n");
                jsScript.append("let responseBody = pm.response.text();\n");
                jsScript.append("let responseJson;\n");
                jsScript.append("try {\n");
                jsScript.append("    responseJson = pm.response.json();\n");
                jsScript.append("} catch (e) {\n");
                jsScript.append("    console.log('Response is not JSON');\n");
                jsScript.append("}\n\n");
            }
            
            // Process the script content
            StringBuilder convertedScript = new StringBuilder();
            for (String line : processedLines) {
                String processedLine = line;
                
                // Apply all syntax conversions
                for (PatternReplacement conversion : SYNTAX_CONVERSIONS) {
                    processedLine = conversion.apply(processedLine);
                }
                
                convertedScript.append(processedLine).append("\n");
            }
            
            // Add the processed script to the result
            jsScript.append(convertedScript);
            
        } catch (Exception e) {
            logger.error("Error converting script: {}", e.getMessage());
            jsScript.append("// Error during conversion: ").append(e.getMessage()).append("\n");
            jsScript.append("// Original script retained below:\n");
            jsScript.append("/*\n").append(groovyScript).append("\n*/\n");
        }
        
        return jsScript.toString();
    }
    
    /**
     * Helper class for pattern-based replacements with robust error handling.
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
                if (input == null) return "";
                
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
                    
                    // Handle conditional expressions like $2 != null ? $2 : 'item'
                    // with appropriate null checks
                    repl = repl.replaceAll("\\(\\$\\d+ != null \\? \\$\\d+ : '([^']+)'\\)", "$1");
                    
                    // Add the replacement
                    matcher.appendReplacement(result, repl);
                }
                matcher.appendTail(result);
                return result.toString();
            } catch (Exception e) {
                // In case of regex error, log it and return the original string
                logger.error("Error in regex replacement: {}", e.getMessage());
                return input;
            }
        }
    }

    public String convertGroovyToJavaScript(String groovyScript) {
        if (groovyScript == null || groovyScript.trim().isEmpty()) {
            return "";
        }
        
        StringBuilder result = new StringBuilder();
        result.append("// Converted from Groovy script\n");
        
        String[] lines = groovyScript.split("\n");
        for (String line : lines) {
            String convertedLine = convertLine(line);
            if (convertedLine != null && !convertedLine.isEmpty()) {
                result.append(convertedLine).append("\n");
            }
        }
        
        return result.toString();
    }
    
    /**
     * Convert a single line of Groovy to JavaScript.
     * 
     * @param line The line to convert
     * @return The converted line
     */
    private String convertLine(String line) {
        String trimmedLine = line.trim();
        
        // Skip empty lines and comments
        if (trimmedLine.isEmpty() || trimmedLine.startsWith("//")) {
            return line;
        }
        
        // Apply all pattern replacements
        String result = line;
        for (PatternReplacement conversion : SYNTAX_CONVERSIONS) {
            result = conversion.pattern.matcher(result).replaceAll(conversion.replacement);
        }
        
        return result;
    }
}
