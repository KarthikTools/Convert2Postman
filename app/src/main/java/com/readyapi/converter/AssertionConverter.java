package com.readyapi.converter;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dom4j.Element;

/**
 * Converts ReadyAPI assertions to Postman test scripts.
 * Mirrors the Python implementation's functionality for assertion handling.
 */
public class AssertionConverter {
    private static final Logger logger = LoggerFactory.getLogger(AssertionConverter.class);
    
    /**
     * Convert a ReadyAPI assertion to a Postman test script.
     * 
     * @param assertion The ReadyAPI assertion to convert
     * @return The Postman test script line
     */
    public static String convertToPostmanTest(ReadyApiAssertion assertion) {
        if (assertion == null) {
            return null;
        }
        
        String assertionType = assertion.getType();
        String name = assertion.getName();
        
        try {
            if (assertionType == null) {
                return null;
            }
            
            // Handle different assertion types
            if ("Valid HTTP Status Codes".equals(assertionType)) {
                // Extract expected status codes
                String codes = assertion.getConfigurationProperty("codes");
                if (codes == null || codes.isEmpty()) {
                    codes = "200";
                }
                
                // Support multiple status codes
                if (codes.contains(",")) {
                    String[] statusCodes = codes.split(",");
                    StringBuilder test = new StringBuilder();
                    test.append(String.format("pm.test(\"%s\", function() {\n", name));
                    test.append("    var expectedCodes = [");
                    for (int i = 0; i < statusCodes.length; i++) {
                        test.append(statusCodes[i].trim());
                        if (i < statusCodes.length - 1) {
                            test.append(", ");
                        }
                    }
                    test.append("];\n");
                    test.append("    pm.expect(expectedCodes).to.include(pm.response.code);\n");
                    test.append("});");
                    return test.toString();
                } else {
                    return String.format("pm.test(\"%s\", function() { pm.response.to.have.status(%s); });", 
                                        name, codes.trim());
                }
            } else if ("JSONPath Match".equals(assertionType) || "JsonPath Match".equals(assertionType)) {
                String path = assertion.getConfigurationProperty("path");
                String expectedValue = assertion.getConfigurationProperty("expectedContent");
                
                if (path != null) {
                    // Format the path for JavaScript access
                    // Convert JsonPath dot notation to bracket notation where needed
                    // e.g., $.store.book[0].title to response.store.book[0].title
                    String formattedPath = formatJsonPath(path);
                    
                    if (expectedValue != null && !expectedValue.isEmpty()) {
                        return String.format(
                            "pm.test(\"%s\", function() {\n" +
                            "    var jsonData = pm.response.json();\n" +
                            "    pm.expect(%s).to.eql(%s);\n" +
                            "});", 
                            name, formattedPath, expectedValue);
                    } else {
                        return String.format(
                            "pm.test(\"%s\", function() {\n" +
                            "    var jsonData = pm.response.json();\n" +
                            "    pm.expect(%s).to.exist;\n" +
                            "});", 
                            name, formattedPath);
                    }
                }
            } else if ("XPath Match".equals(assertionType)) {
                String xpath = assertion.getConfigurationProperty("xpath");
                String expectedValue = assertion.getConfigurationProperty("expectedContent");
                
                if (xpath != null) {
                    if (expectedValue != null && !expectedValue.isEmpty()) {
                        return String.format(
                            "pm.test(\"%s\", function() {\n" +
                            "    var xml = xml2Json(pm.response.text());\n" +
                            "    // Note: XPath needs a proper XML parser in Postman\n" +
                            "    // Original XPath: %s\n" +
                            "    // For now, you might need to manually navigate the XML structure\n" +
                            "    // pm.expect(EXTRACTED_VALUE).to.eql(\"%s\");\n" +
                            "});", 
                            name, xpath, expectedValue);
                    } else {
                        return String.format(
                            "pm.test(\"%s\", function() {\n" +
                            "    var xml = xml2Json(pm.response.text());\n" +
                            "    // Note: XPath needs a proper XML parser in Postman\n" +
                            "    // Original XPath: %s\n" +
                            "    // For now, you might need to manually navigate the XML structure\n" +
                            "});", 
                            name, xpath);
                    }
                }
            } else if ("Contains".equals(assertionType)) {
                String token = assertion.getConfigurationProperty("token");
                boolean ignoreCase = "true".equals(assertion.getConfigurationProperty("ignoreCase"));
                
                if (token != null) {
                    String testCode;
                    if (ignoreCase) {
                        testCode = String.format(
                            "pm.test(\"%s\", function() {\n" +
                            "    pm.expect(pm.response.text().toLowerCase()).to.include(\"%s\".toLowerCase());\n" +
                            "});", 
                            name, token);
                    } else {
                        testCode = String.format(
                            "pm.test(\"%s\", function() {\n" +
                            "    pm.expect(pm.response.text()).to.include(\"%s\");\n" +
                            "});", 
                            name, token);
                    }
                    return testCode;
                }
            } else if ("Response SLA".equals(assertionType)) {
                String maxTime = assertion.getConfigurationProperty("maxTime");
                
                if (maxTime != null) {
                    return String.format(
                        "pm.test(\"%s\", function() {\n" +
                        "    pm.expect(pm.response.responseTime).to.be.below(%s);\n" +
                        "});", 
                        name, maxTime);
                }
            } else if ("Script Assertion".equals(assertionType)) {
                String script = assertion.getConfigurationProperty("scriptText");
                
                if (script != null) {
                    // Convert the Groovy script to JavaScript
                    String jsScript = ScriptConverter.convertToJavaScript(script, "assertion");
                    
                    return String.format(
                        "pm.test(\"%s\", function() {\n" +
                        "    // Converted from Groovy assertion script\n" +
                        "%s\n" +
                        "});",
                        name, 
                        jsScript.replaceAll("(?m)^", "    ")); // Add indentation
                }
            }
            
            // Default for unsupported assertion types
            logger.info("Unsupported assertion type: {}", assertionType);
            return String.format(
                "// Unsupported assertion type: %s\n" +
                "// Original name: %s\n" +
                "// Please implement this assertion manually", 
                assertionType, name);
                
        } catch (Exception e) {
            logger.error("Error converting assertion: {}", e.getMessage());
            return String.format(
                "// Error converting assertion: %s\n" +
                "// %s\n" +
                "// Please implement this assertion manually", 
                name, e.getMessage());
        }
    }
    
    /**
     * Convert multiple ReadyAPI assertions to Postman test scripts.
     * 
     * @param assertions The list of ReadyAPI assertions to convert
     * @return The Postman test script lines
     */
    public static List<String> convertToPostmanTests(List<ReadyApiAssertion> assertions) {
        List<String> testScripts = new ArrayList<>();
        
        if (assertions != null) {
            for (ReadyApiAssertion assertion : assertions) {
                String test = convertToPostmanTest(assertion);
                if (test != null && !test.isEmpty()) {
                    testScripts.add(test);
                }
            }
        }
        
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

    /**
     * Convert ReadyAPI assertion elements to Postman test scripts
     * 
     * @param assertionElements List of assertion elements from ReadyAPI
     * @return List of Postman test scripts
     */
    public static List<String> convertElementsToPostmanTests(List<Element> assertionElements) {
        List<String> tests = new ArrayList<>();
        
        for (Element assertion : assertionElements) {
            String assertionType = assertion.attributeValue("type");
            String name = assertion.attributeValue("name");
            
            if ("Valid HTTP Status Codes".equals(assertionType)) {
                tests.add(String.format("pm.test(\"%s\", function () { pm.response.to.have.status(200); });", name));
            } 
            else if ("JSONPath Match".equals(assertionType)) {
                Element config = assertion.element("configuration");
                if (config != null) {
                    Element pathElem = config.element("path");
                    if (pathElem != null) {
                        String path = pathElem.getText();
                        tests.add(String.format("pm.test(\"Check JSONPath %s\", function () { var jsonData = pm.response.json(); pm.expect(jsonData%s).to.exist; });", 
                            path, path));
                    }
                }
            }
            else if ("Contains".equals(assertionType)) {
                Element config = assertion.element("configuration");
                if (config != null) {
                    Element tokenElem = config.element("token");
                    if (tokenElem != null) {
                        String token = tokenElem.getText();
                        tests.add(String.format("pm.test(\"%s\", function () { pm.expect(pm.response.text()).to.include('%s'); });", 
                            name, token));
                    }
                }
            }
            else if ("Not Contains".equals(assertionType)) {
                Element config = assertion.element("configuration");
                if (config != null) {
                    Element tokenElem = config.element("token");
                    if (tokenElem != null) {
                        String token = tokenElem.getText();
                        tests.add(String.format("pm.test(\"%s\", function () { pm.expect(pm.response.text()).to.not.include('%s'); });", 
                            name, token));
                    }
                }
            }
            else if ("XPath Match".equals(assertionType)) {
                Element config = assertion.element("configuration");
                if (config != null) {
                    Element xpathElem = config.element("xpath");
                    if (xpathElem != null) {
                        String xpath = xpathElem.getText();
                        tests.add(String.format("pm.test(\"%s\", function () { var xmlDoc = pm.response.text(); pm.expect(xmlDoc).to.match(/%s/); });", 
                            name, xpath));
                    }
                }
            }
        }
        
        return tests;
    }
} 