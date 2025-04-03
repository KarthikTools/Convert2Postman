package com.readyapi.converter;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts ReadyAPI property transfers to Postman pre-request or test scripts.
 * Handles the conversion of property transfers between test steps.
 */
public class PropertyTransferConverter {
    private static final Logger logger = LoggerFactory.getLogger(PropertyTransferConverter.class);
    
    /**
     * Converts property transfers to Postman pre-request scripts.
     * 
     * @param testStep The test step containing property transfers
     * @return A list of pre-request script lines
     */
    public static List<String> convertToPreRequestScript(ReadyApiTestStep testStep) {
        List<String> script = new ArrayList<>();
        
        for (ReadyApiTestStep.PropertyTransfer transfer : testStep.getPropertyTransfers()) {
            String name = transfer.getName();
            String sourcePath = transfer.getSourcePath();
            if (name != null && sourcePath != null) {
                script.add(String.format("pm.variables.set('%s', pm.response.json()%s);", name, sourcePath));
            }
        }
        
        return script;
    }
    
    /**
     * Converts property transfers to Postman test scripts that save response data for later use.
     * 
     * @param testStep The test step containing a response
     * @return A list of test script lines
     */
    public static List<String> createResponseStorageScript(ReadyApiTestStep testStep) {
        List<String> testScripts = new ArrayList<>();
        
        if (testStep == null || testStep.getName() == null) {
            return testScripts;
        }
        
        String stepName = testStep.getName();
        
        // Add code to store the response for use in property transfers
        testScripts.add("// Store this response for later property transfers");
        testScripts.add(String.format(
            "pm.variables.set('%s_response', pm.response.text());\n",
            stepName));
        
        return testScripts;
    }
    
    /**
     * Format a JSONPath for use in JavaScript.
     * Converts JSONPath expressions to JavaScript property access.
     *
     * @param jsonPath The JSONPath expression to format
     * @return The formatted path for JavaScript access
     */
    private static String formatJsonPath(String jsonPath) {
        if (jsonPath == null || jsonPath.isEmpty()) {
            return "jsonData";
        }
        
        // Handle root notation
        String path = jsonPath.trim();
        if (path.startsWith("$")) {
            path = path.substring(1);
        }
        
        // Replace dot notation with bracket notation for array indices
        StringBuilder formattedPath = new StringBuilder("jsonData");
        
        // Split by dots, but preserve array brackets
        boolean inBracket = false;
        StringBuilder segment = new StringBuilder();
        
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            
            if (c == '[') {
                inBracket = true;
                if (segment.length() > 0) {
                    formattedPath.append(".").append(segment.toString());
                    segment = new StringBuilder();
                }
                formattedPath.append("[");
            } else if (c == ']') {
                inBracket = false;
                formattedPath.append(segment.toString()).append("]");
                segment = new StringBuilder();
            } else if (c == '.' && !inBracket) {
                if (segment.length() > 0) {
                    formattedPath.append(".").append(segment.toString());
                    segment = new StringBuilder();
                }
            } else {
                segment.append(c);
            }
        }
        
        // Append any remaining segment
        if (segment.length() > 0) {
            formattedPath.append(".").append(segment.toString());
        }
        
        return formattedPath.toString();
    }
} 