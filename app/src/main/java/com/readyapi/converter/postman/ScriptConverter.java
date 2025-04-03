package com.readyapi.converter.postman;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Converts Groovy scripts to JavaScript for Postman.
 */
public class ScriptConverter {
    
    /**
     * Convert a Groovy script to JavaScript.
     * 
     * @param groovyScript The Groovy script to convert
     * @param isPreRequest Whether this is a pre-request script
     * @return The converted JavaScript code
     */
    public static String convertGroovyToJavaScript(String groovyScript, boolean isPreRequest) {
        if (groovyScript == null || groovyScript.isEmpty()) {
            return "";
        }
        
        List<String> jsLines = new ArrayList<>();
        jsLines.add("// Converted from Groovy script");
        
        for (String line : groovyScript.split("\\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                jsLines.add("");
                continue;
            }
            
            if (trimmed.startsWith("//")) {
                jsLines.add(line);
                continue;
            }
            
            // Convert log.info to console.log
            line = line.replaceAll("log\\.info\\((.+?)\\)", "console.log($1);");
            
            // Skip JsonSlurper lines
            if (line.contains("new JsonSlurper().parseText(")) {
                continue;
            }
            
            // Convert property access
            Pattern propertyPattern = Pattern.compile("def (\\w+)\\s*=\\s*testRunner\\.testCase\\.testSteps\\[\"(.*?)\"\\]\\.getPropertyValue\\(\"(.*?)\"\\)");
            Matcher propertyMatcher = propertyPattern.matcher(line);
            if (propertyMatcher.find()) {
                String varName = propertyMatcher.group(1);
                String stepName = propertyMatcher.group(2);
                String propName = propertyMatcher.group(3);
                jsLines.add(String.format("let %s = pm.collectionVariables.get('%s_%s');", varName, stepName, propName));
                continue;
            }
            
            // Convert JSON parsing
            Pattern jsonPattern = Pattern.compile("def (\\w+)\\s*=\\s*parse_json\\.(\\w+)");
            Matcher jsonMatcher = jsonPattern.matcher(line);
            if (jsonMatcher.find()) {
                String varName = jsonMatcher.group(1);
                String field = jsonMatcher.group(2);
                jsLines.add(String.format("let %s = pm.response.json().%s;", varName, field));
                continue;
            }
            
            // Convert integer parsing
            line = line.replaceAll("int (\\w+)\\s*=\\s*(\\w+)\\.toInteger\\(\\)", "let $1 = parseInt($2);");
            
            // Convert assertions
            Pattern assertPattern = Pattern.compile("assert (.+?)\\s*==\\s*(.+)");
            Matcher assertMatcher = assertPattern.matcher(line);
            if (assertMatcher.find()) {
                String left = assertMatcher.group(1);
                String right = assertMatcher.group(2);
                jsLines.add(String.format("pm.test('Assert %s == %s', function () {", left.trim(), right.trim()));
                jsLines.add(String.format("    pm.expect(%s).to.eql(%s);", left.trim(), right.trim()));
                jsLines.add("});");
                continue;
            }
            
            // Add any remaining lines as comments
            if (!line.trim().isEmpty()) {
                jsLines.add("// " + line);
            }
        }
        
        return String.join("\n", jsLines);
    }
} 