package com.readyapi.converter;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles conversion of ReadyAPI Groovy scripts to Postman JavaScript.
 */
public class ScriptConverter {
    private static final Map<String, String> GROOVY_TO_JS_PATTERNS = new HashMap<>();
    private static final Map<String, String> SOAPUI_TO_POSTMAN_MAPPINGS = new HashMap<>();
    private static final Map<String, String> GROOVY_METHODS_TO_POSTMAN = new HashMap<>();
    private static final List<String> UNSUPPORTED_IMPORTS = new ArrayList<>();
    
    static {
        // Groovy to JavaScript patterns
        GROOVY_TO_JS_PATTERNS.put("def\\s+([a-zA-Z0-9_]+)\\s*=", "let $1 =");
        GROOVY_TO_JS_PATTERNS.put("import\\s+[a-zA-Z0-9_.]+", "// $0");
        GROOVY_TO_JS_PATTERNS.put("\\btestRunner\\b", "pm");
        GROOVY_TO_JS_PATTERNS.put("\\bcontext\\b", "pm.context");
        GROOVY_TO_JS_PATTERNS.put("\\blog\\.info\\b", "console.log");
        GROOVY_TO_JS_PATTERNS.put("\\blog\\.error\\b", "console.error");
        GROOVY_TO_JS_PATTERNS.put("\\blog\\.warn\\b", "console.warn");
        GROOVY_TO_JS_PATTERNS.put("\\.each\\s*\\{", ".forEach(function(");
        GROOVY_TO_JS_PATTERNS.put("\\}\\s*\\)", "})");
        GROOVY_TO_JS_PATTERNS.put("\\$\\{([^}]+)\\}", "\" + $1 + \"");
        
        // SoapUI to Postman mappings
        SOAPUI_TO_POSTMAN_MAPPINGS.put("testRunner.testCase.testSuite.project", "pm.collectionVariables");
        SOAPUI_TO_POSTMAN_MAPPINGS.put("testRunner.testCase.testSuite", "pm.testSuite");
        SOAPUI_TO_POSTMAN_MAPPINGS.put("testRunner.testCase", "pm.testCase");
        SOAPUI_TO_POSTMAN_MAPPINGS.put("testRunner.testCase.testSuite.project.getPropertyValue", "pm.collectionVariables.get");
        SOAPUI_TO_POSTMAN_MAPPINGS.put("testRunner.testCase.testSuite.project.setPropertyValue", "pm.collectionVariables.set");
        
        // Groovy methods to Postman equivalents
        GROOVY_METHODS_TO_POSTMAN.put("assert\\s+([^;]+)", "pm.expect($1).to.be.true");
        GROOVY_METHODS_TO_POSTMAN.put("assertEquals\\(([^,]+),\\s*([^)]+)\\)", "pm.expect($2).to.equal($1)");
        GROOVY_METHODS_TO_POSTMAN.put("assertNotNull\\(([^)]+)\\)", "pm.expect($1).to.not.be.null");
        GROOVY_METHODS_TO_POSTMAN.put("assertNull\\(([^)]+)\\)", "pm.expect($1).to.be.null");
        GROOVY_METHODS_TO_POSTMAN.put("sleep\\(([^)]+)\\)", "pm.test.sleep($1)");
        GROOVY_METHODS_TO_POSTMAN.put("random\\(([^)]+)\\)", "Math.floor(Math.random() * $1)");
        GROOVY_METHODS_TO_POSTMAN.put("substring\\(([^,]+),\\s*([^)]+)\\)", "substring($1, $2)");
        GROOVY_METHODS_TO_POSTMAN.put("replaceAll\\(([^,]+),\\s*([^)]+)\\)", "replace(/$2/g, $1)");
        GROOVY_METHODS_TO_POSTMAN.put("split\\(([^,]+),\\s*([^)]+)\\)", "split($2)");
        GROOVY_METHODS_TO_POSTMAN.put("trim\\(\\)", "trim()");
        GROOVY_METHODS_TO_POSTMAN.put("toLowerCase\\(\\)", "toLowerCase()");
        GROOVY_METHODS_TO_POSTMAN.put("toUpperCase\\(\\)", "toUpperCase()");
        GROOVY_METHODS_TO_POSTMAN.put("contains\\(([^)]+)\\)", "includes($1)");
        GROOVY_METHODS_TO_POSTMAN.put("startsWith\\(([^)]+)\\)", "startsWith($1)");
        GROOVY_METHODS_TO_POSTMAN.put("endsWith\\(([^)]+)\\)", "endsWith($1)");
        GROOVY_METHODS_TO_POSTMAN.put("matches\\(([^)]+)\\)", "match(/$1/)");
        GROOVY_METHODS_TO_POSTMAN.put("parse\\(([^)]+)\\)", "JSON.parse($1)");
        GROOVY_METHODS_TO_POSTMAN.put("stringify\\(([^)]+)\\)", "JSON.stringify($1)");
        
        // List of unsupported imports
        UNSUPPORTED_IMPORTS.add("import groovy.json.JsonSlurper");
        UNSUPPORTED_IMPORTS.add("import groovy.json.JsonOutput");
        UNSUPPORTED_IMPORTS.add("import groovy.xml.XmlSlurper");
        UNSUPPORTED_IMPORTS.add("import groovy.xml.XmlUtil");
        UNSUPPORTED_IMPORTS.add("import groovy.sql.Sql");
        UNSUPPORTED_IMPORTS.add("import groovy.net.http.HTTPBuilder");
        UNSUPPORTED_IMPORTS.add("import groovy.util.XmlParser");
        UNSUPPORTED_IMPORTS.add("import groovy.util.XmlNodePrinter");
    }
    
    /**
     * Convert a Groovy script to JavaScript for Postman.
     */
    public static String convertToJavaScript(String groovyScript, String scriptType) {
        if (groovyScript == null || groovyScript.isEmpty()) {
            return "";
        }
        
        StringBuilder jsContent = new StringBuilder();
        
        // Add documentation about unsupported imports
        List<String> foundImports = findImports(groovyScript);
        if (!foundImports.isEmpty()) {
            jsContent.append("// WARNING: The following imports are not supported in Postman:\n");
            for (String imp : foundImports) {
                if (UNSUPPORTED_IMPORTS.contains(imp)) {
                    jsContent.append("// ").append(imp).append(" - Use native Postman methods instead\n");
                }
            }
            jsContent.append("\n");
        }
        
        // Handle function library initialization
        if (scriptType.equals("library")) {
            // Extract library name from class name or use default
            Pattern classPattern = Pattern.compile("class\\s+([a-zA-Z0-9_]+)\\s*\\{");
            Matcher matcher = classPattern.matcher(groovyScript);
            String libraryName = matcher.find() ? matcher.group(1) : "FunctionLibrary";
            
            // Convert the library to a Postman variable
            jsContent.append(FunctionLibraryConverter.convertLibraryToPostmanVariable(libraryName, groovyScript));
        } else {
            String convertedScript = groovyScript;
            
            // Apply Groovy to JavaScript patterns
            for (Map.Entry<String, String> pattern : GROOVY_TO_JS_PATTERNS.entrySet()) {
                convertedScript = convertedScript.replaceAll(pattern.getKey(), pattern.getValue());
            }
            
            // Apply SoapUI to Postman mappings
            for (Map.Entry<String, String> mapping : SOAPUI_TO_POSTMAN_MAPPINGS.entrySet()) {
                convertedScript = convertedScript.replaceAll(mapping.getKey(), mapping.getValue());
            }
            
            // Apply Groovy methods to Postman equivalents
            for (Map.Entry<String, String> method : GROOVY_METHODS_TO_POSTMAN.entrySet()) {
                convertedScript = convertedScript.replaceAll(method.getKey(), method.getValue());
            }
            
            // Handle special cases
            convertedScript = handleSpecialCases(convertedScript, scriptType);
            
            // Add appropriate wrapper based on script type
            jsContent.append(wrapScript(convertedScript, scriptType));
        }
        
        return jsContent.toString();
    }
    
    /**
     * Find all imports in the Groovy script.
     */
    private static List<String> findImports(String script) {
        List<String> imports = new ArrayList<>();
        Pattern importPattern = Pattern.compile("import\\s+[a-zA-Z0-9_.]+");
        Matcher matcher = importPattern.matcher(script);
        while (matcher.find()) {
            imports.add(matcher.group());
        }
        return imports;
    }
    
    /**
     * Handle special cases in the script.
     */
    private static String handleSpecialCases(String script, String scriptType) {
        // Handle soapui.utils.FunctionLibrary initialization
        script = script.replaceAll(
            "new\\s+soapui\\.utils\\.FunctionLibrary\\(([^)]+)\\)",
            "JSON.parse(pm.collectionVariables.get('FunctionLibrary'))"
        );
        
        // Handle test step execution
        if (scriptType.equals("test")) {
            script = script.replaceAll(
                "testRunner\\.runTestStep\\(([^)]+)\\)",
                "pm.testCase.runTestStep($1)"
            );
        }
        
        // Handle XML operations
        script = script.replaceAll(
            "XmlSlurper\\.parse\\(([^)]+)\\)",
            "// WARNING: XmlSlurper.parse() is not supported in Postman. Use pm.response.text() and parse manually."
        );
        
        // Handle HTTP operations
        script = script.replaceAll(
            "HTTPBuilder\\.request\\(([^)]+)\\)",
            "// WARNING: HTTPBuilder is not supported in Postman. Use pm.sendRequest() instead."
        );
        
        return script;
    }
    
    /**
     * Wrap the script with appropriate context.
     */
    private static String wrapScript(String script, String scriptType) {
        StringBuilder wrapped = new StringBuilder();
        
        switch (scriptType) {
            case "pre-request":
                wrapped.append("// Pre-request Script\n");
                wrapped.append("(async function() {\n");
                wrapped.append(script);
                wrapped.append("\n})();");
                break;
                
            case "test":
                wrapped.append("// Test Script\n");
                wrapped.append("pm.test('Test Execution', function() {\n");
                wrapped.append(script);
                wrapped.append("\n});");
                break;
                
            case "library":
                wrapped.append(script);
                break;
                
            default:
                wrapped.append(script);
        }
        
        return wrapped.toString();
    }
} 