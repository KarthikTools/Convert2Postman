package com.readyapi.converter;

/**
 * Helper class for converting XPath expressions to JavaScript equivalents.
 */
public class XPathHelper {
    
    /**
     * Convert an XPath expression to JavaScript code that evaluates it.
     * 
     * @param xpath The XPath expression
     * @param source The source variable name that contains the XML
     * @param targetVariable The variable to store the result in
     * @return JavaScript code that evaluates the XPath
     */
    public static String convertXPathToJavaScript(String xpath, String source, String targetVariable) {
        StringBuilder jsCode = new StringBuilder();
        
        jsCode.append("// XPath evaluation: ").append(xpath).append("\n");
        jsCode.append("(function() {\n");
        jsCode.append("    try {\n");
        jsCode.append("        // Parse XML\n");
        jsCode.append("        const parser = new DOMParser();\n");
        jsCode.append("        const xmlDoc = parser.parseFromString(").append(source).append(", \"text/xml\");\n\n");
        
        jsCode.append("        // Create namespace resolver if needed\n");
        jsCode.append("        const nsResolver = xmlDoc.createNSResolver(xmlDoc.documentElement);\n\n");
        
        jsCode.append("        // Evaluate XPath\n");
        jsCode.append("        const xpathResult = xmlDoc.evaluate(\"").append(escapeJavaScript(xpath))
            .append("\", xmlDoc, nsResolver, XPathResult.ANY_TYPE, null);\n\n");
        
        jsCode.append("        // Extract result based on type\n");
        jsCode.append("        let result;\n");
        jsCode.append("        switch (xpathResult.resultType) {\n");
        jsCode.append("            case XPathResult.STRING_TYPE:\n");
        jsCode.append("                result = xpathResult.stringValue;\n");
        jsCode.append("                break;\n");
        jsCode.append("            case XPathResult.NUMBER_TYPE:\n");
        jsCode.append("                result = xpathResult.numberValue;\n");
        jsCode.append("                break;\n");
        jsCode.append("            case XPathResult.BOOLEAN_TYPE:\n");
        jsCode.append("                result = xpathResult.booleanValue;\n");
        jsCode.append("                break;\n");
        jsCode.append("            case XPathResult.UNORDERED_NODE_ITERATOR_TYPE:\n");
        jsCode.append("            case XPathResult.ORDERED_NODE_ITERATOR_TYPE:\n");
        jsCode.append("                result = [];\n");
        jsCode.append("                let node;\n");
        jsCode.append("                while (node = xpathResult.iterateNext()) {\n");
        jsCode.append("                    result.push(node.textContent);\n");
        jsCode.append("                }\n");
        jsCode.append("                break;\n");
        jsCode.append("            case XPathResult.UNORDERED_NODE_SNAPSHOT_TYPE:\n");
        jsCode.append("            case XPathResult.ORDERED_NODE_SNAPSHOT_TYPE:\n");
        jsCode.append("                result = [];\n");
        jsCode.append("                for (let i = 0; i < xpathResult.snapshotLength; i++) {\n");
        jsCode.append("                    result.push(xpathResult.snapshotItem(i).textContent);\n");
        jsCode.append("                }\n");
        jsCode.append("                break;\n");
        jsCode.append("            case XPathResult.ANY_UNORDERED_NODE_TYPE:\n");
        jsCode.append("            case XPathResult.FIRST_ORDERED_NODE_TYPE:\n");
        jsCode.append("                if (xpathResult.singleNodeValue) {\n");
        jsCode.append("                    result = xpathResult.singleNodeValue.textContent;\n");
        jsCode.append("                } else {\n");
        jsCode.append("                    result = null;\n");
        jsCode.append("                }\n");
        jsCode.append("                break;\n");
        jsCode.append("            default:\n");
        jsCode.append("                result = null;\n");
        jsCode.append("        }\n\n");
        
        jsCode.append("        ").append(targetVariable).append(" = result;\n");
        jsCode.append("        console.log('XPath evaluated: ").append(escapeJavaScript(xpath))
            .append("', ").append(targetVariable).append(");\n");
        jsCode.append("    } catch (error) {\n");
        jsCode.append("        console.error('Error evaluating XPath expression: ").append(escapeJavaScript(xpath))
            .append("', error);\n");
        jsCode.append("        ").append(targetVariable).append(" = null;\n");
        jsCode.append("    }\n");
        jsCode.append("})();\n");
        
        return jsCode.toString();
    }
    
    /**
     * Provides a note about XPath limitations in Postman.
     * 
     * @return A string with information about XPath in Postman
     */
    public static String getXPathPostmanNotes() {
        StringBuilder note = new StringBuilder();
        note.append("// NOTE: XPath in Postman requires the DOMParser and XPathResult objects\n");
        note.append("// which are available in browser environments but not in the Postman sandbox.\n");
        note.append("// For Postman Collection Runner, you may need to use an alternative approach\n");
        note.append("// such as XML to JSON conversion and JSONPath, or use a custom external service.\n");
        note.append("// The code below will work in the Postman UI as it uses browser capabilities.\n\n");
        
        return note.toString();
    }
    
    /**
     * Convert a JSONPath expression to JavaScript code that evaluates it.
     * 
     * @param jsonPath The JSONPath expression
     * @param source The source variable name that contains the JSON
     * @param targetVariable The variable to store the result in
     * @return JavaScript code that evaluates the JSONPath
     */
    public static String convertJSONPathToJavaScript(String jsonPath, String source, String targetVariable) {
        StringBuilder jsCode = new StringBuilder();
        
        jsCode.append("// JSONPath evaluation: ").append(jsonPath).append("\n");
        jsCode.append("(function() {\n");
        jsCode.append("    try {\n");
        jsCode.append("        // Parse JSON if needed\n");
        jsCode.append("        const jsonObj = typeof ").append(source).append(" === 'string' ? ");
        jsCode.append("JSON.parse(").append(source).append(") : ").append(source).append(";\n\n");
        
        jsCode.append("        // Convert JSONPath to JavaScript property access\n");
        jsCode.append("        ").append(convertJSONPathExpression(jsonPath, "jsonObj", targetVariable));
        
        jsCode.append("        console.log('JSONPath evaluated: ").append(escapeJavaScript(jsonPath))
            .append("', ").append(targetVariable).append(");\n");
        jsCode.append("    } catch (error) {\n");
        jsCode.append("        console.error('Error evaluating JSONPath expression: ").append(escapeJavaScript(jsonPath))
            .append("', error);\n");
        jsCode.append("        ").append(targetVariable).append(" = null;\n");
        jsCode.append("    }\n");
        jsCode.append("})();\n");
        
        return jsCode.toString();
    }
    
    /**
     * Convert a JSONPath expression to JavaScript property access.
     * 
     * @param jsonPath The JSONPath expression
     * @param sourceVar The source variable name
     * @param targetVar The target variable name
     * @return JavaScript code for property access
     */
    private static String convertJSONPathExpression(String jsonPath, String sourceVar, String targetVar) {
        StringBuilder code = new StringBuilder();
        
        // Remove leading $ if present
        if (jsonPath.startsWith("$")) {
            jsonPath = jsonPath.substring(1);
        }
        
        // Handle simple property access paths
        if (jsonPath.matches("^(\\.[a-zA-Z0-9_]+)+$")) {
            code.append(targetVar).append(" = ").append(sourceVar);
            String[] parts = jsonPath.split("\\.");
            for (String part : parts) {
                if (!part.isEmpty()) {
                    code.append("?.").append(part);
                }
            }
            code.append(";\n");
            return code.toString();
        }
        
        // Handle array indexing
        if (jsonPath.contains("[")) {
            code.append(targetVar).append(" = ").append(sourceVar);
            
            // Replace .property with ['property'] for consistency
            jsonPath = jsonPath.replaceAll("\\.([a-zA-Z0-9_]+)", "['$1']");
            
            // Replace [*] with forEach iteration
            if (jsonPath.contains("[*]")) {
                code = new StringBuilder();
                code.append(targetVar).append(" = [];\n");
                
                String[] parts = jsonPath.split("\\[\\*\\]");
                String arrayPath = parts[0];
                
                code.append("        const array = ").append(sourceVar).append(arrayPath).append(";\n");
                code.append("        if (Array.isArray(array)) {\n");
                code.append("            array.forEach(item => {\n");
                
                if (parts.length > 1) {
                    code.append("                const value = item").append(parts[1]).append(";\n");
                    code.append("                if (value !== undefined) ").append(targetVar).append(".push(value);\n");
                } else {
                    code.append("                ").append(targetVar).append(".push(item);\n");
                }
                
                code.append("            });\n");
                code.append("        }\n");
            } else {
                // Process normal bracket notation
                code.append(jsonPath).append(";\n");
            }
        } else {
            code.append(targetVar).append(" = ").append(sourceVar).append(jsonPath).append(";\n");
        }
        
        return code.toString();
    }
    
    /**
     * Escape a string for JavaScript.
     * 
     * @param input The input string
     * @return The escaped string
     */
    private static String escapeJavaScript(String input) {
        if (input == null) {
            return "";
        }
        
        return input.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
} 