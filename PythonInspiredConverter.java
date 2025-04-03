import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Python-inspired converter for ReadyAPI to Postman.
 * Based on the Python implementation provided.
 */
public class PythonInspiredConverter {
    private static final String NAMESPACE_URI = "http://eviware.com/soapui/config";
    private static final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java PythonInspiredConverter <input-xml> <output-json>");
            System.exit(1);
        }
        
        String inputFile = args[0];
        String outputFile = args[1];
        
        try {
            convertReadyApiToPostman(inputFile, outputFile);
            System.out.println("Conversion complete! Output written to: " + outputFile);
        } catch (Exception e) {
            System.err.println("Error during conversion: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Convert ReadyAPI project to Postman collection.
     * 
     * @param inputFile Input XML file path
     * @param outputFile Output JSON file path
     * @throws DocumentException If XML parsing fails
     * @throws IOException If file operations fail
     */
    public static void convertReadyApiToPostman(String inputFile, String outputFile) throws DocumentException, IOException {
        // Parse XML
        Document doc = parseXmlFile(inputFile);
        Element root = doc.getRootElement();
        
        // Create collection structure
        ObjectNode collection = objectMapper.createObjectNode();
        ObjectNode info = objectMapper.createObjectNode();
        ArrayNode items = objectMapper.createArrayNode();
        
        // Set collection info
        info.put("_postman_id", java.util.UUID.randomUUID().toString());
        info.put("name", root.attributeValue("name", "ReadyAPI Project"));
        info.put("schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json");
        collection.set("info", info);
        
        // Extract test suites
        processTestSuites(root, items);
        
        // Handle interfaces if they exist
        processInterfaces(root, items);
        
        // Add items to collection
        collection.set("item", items);
        
        // Write to file
        objectMapper.writeValue(new File(outputFile), collection);
    }
    
    /**
     * Parse XML file with proper namespace handling.
     * 
     * @param filePath The XML file path
     * @return Parsed Document
     * @throws DocumentException If parsing fails
     */
    private static Document parseXmlFile(String filePath) throws DocumentException {
        SAXReader reader = new SAXReader();
        
        // Configure namespace
        Map<String, String> nsMap = new HashMap<>();
        nsMap.put("con", NAMESPACE_URI);
        reader.getDocumentFactory().setXPathNamespaceURIs(nsMap);
        
        // Parse and return
        return reader.read(new File(filePath));
    }
    
    /**
     * Process test suites from ReadyAPI project.
     * 
     * @param root The project root element
     * @param items The Postman items array
     */
    private static void processTestSuites(Element root, ArrayNode items) {
        for (Element testSuiteElement : root.elements("con:testSuite")) {
            String suiteName = testSuiteElement.attributeValue("name", "Unknown Test Suite");
            
            // Create suite folder
            ObjectNode suiteFolder = objectMapper.createObjectNode();
            suiteFolder.put("name", suiteName);
            ArrayNode suiteItems = objectMapper.createArrayNode();
            
            // Process test cases
            for (Element testCaseElement : testSuiteElement.elements("con:testCase")) {
                String caseName = testCaseElement.attributeValue("name", "Unknown Test Case");
                
                // Create case folder
                ObjectNode caseFolder = objectMapper.createObjectNode();
                caseFolder.put("name", caseName);
                ArrayNode caseItems = objectMapper.createArrayNode();
                
                // Process test steps
                for (Element testStepElement : testCaseElement.elements("con:testStep")) {
                    processTestStep(testStepElement, caseItems);
                }
                
                // Add case items to case folder
                caseFolder.set("item", caseItems);
                suiteItems.add(caseFolder);
            }
            
            // Add suite items to suite folder
            suiteFolder.set("item", suiteItems);
            items.add(suiteFolder);
        }
    }
    
    /**
     * Process a single test step.
     * 
     * @param testStepElement The test step element
     * @param parentItems The parent items array
     */
    private static void processTestStep(Element testStepElement, ArrayNode parentItems) {
        String stepType = testStepElement.attributeValue("type");
        String stepName = testStepElement.attributeValue("name", "Unknown Step");
        
        // Only process REST requests, properties, and transfers for now
        if ("restrequest".equalsIgnoreCase(stepType)) {
            processRestRequest(testStepElement, stepName, parentItems);
        } else if ("properties".equalsIgnoreCase(stepType)) {
            processPropertiesStep(testStepElement, stepName, parentItems);
        } else if ("transfer".equalsIgnoreCase(stepType)) {
            processTransferStep(testStepElement, stepName, parentItems);
        } else if ("groovy".equalsIgnoreCase(stepType)) {
            processGroovyStep(testStepElement, stepName, parentItems);
        } else if ("datasource".equalsIgnoreCase(stepType)) {
            processDataSourceStep(testStepElement, stepName, parentItems);
        }
    }
    
    /**
     * Process a REST request test step.
     * 
     * @param testStepElement The test step element
     * @param stepName The step name
     * @param parentItems The parent items array
     */
    private static void processRestRequest(Element testStepElement, String stepName, ArrayNode parentItems) {
        ObjectNode item = objectMapper.createObjectNode();
        item.put("name", stepName);
        
        Element configElement = testStepElement.element("con:config");
        if (configElement == null) {
            return;
        }
        
        // Create request
        ObjectNode request = objectMapper.createObjectNode();
        
        // Set method
        String method = configElement.elementTextTrim("con:method");
        request.put("method", method != null ? method : "GET");
        
        // Set URL
        String endpoint = configElement.elementTextTrim("con:endpoint");
        if (endpoint != null) {
            ObjectNode url = objectMapper.createObjectNode();
            url.put("raw", endpoint);
            request.set("url", url);
        }
        
        // Process headers
        Element headersElement = configElement.element("con:headers");
        if (headersElement != null) {
            ArrayNode headers = objectMapper.createArrayNode();
            for (Element entryElement : headersElement.elements("con:entry")) {
                String key = entryElement.attributeValue("key");
                String value = entryElement.getTextTrim();
                
                if (key != null && value != null) {
                    ObjectNode header = objectMapper.createObjectNode();
                    header.put("key", key);
                    header.put("value", value);
                    header.put("type", "text");
                    headers.add(header);
                }
            }
            
            if (headers.size() > 0) {
                request.set("header", headers);
            }
        }
        
        // Process request body
        Element requestElement = configElement.element("con:request");
        if (requestElement != null) {
            String requestBody = requestElement.getTextTrim();
            if (requestBody != null && !requestBody.isEmpty()) {
                ObjectNode body = objectMapper.createObjectNode();
                body.put("mode", "raw");
                body.put("raw", requestBody);
                
                String mediaType = configElement.elementTextTrim("con:mediaType");
                if (mediaType != null) {
                    ObjectNode options = objectMapper.createObjectNode();
                    ObjectNode raw = objectMapper.createObjectNode();
                    
                    if (mediaType.contains("json")) {
                        raw.put("language", "json");
                    } else if (mediaType.contains("xml")) {
                        raw.put("language", "xml");
                    } else {
                        raw.put("language", "text");
                    }
                    
                    options.set("raw", raw);
                    body.set("options", options);
                }
                
                request.set("body", body);
            }
        }
        
        // Process assertions
        ArrayNode events = objectMapper.createArrayNode();
        List<String> testScripts = extractAssertions(configElement);
        
        // Add response storage for property transfers
        testScripts.add("// Store this response for later property transfers");
        testScripts.add(String.format("pm.variables.set('%s_response', pm.response.text());", stepName));
        
        if (!testScripts.isEmpty()) {
            ObjectNode testEvent = objectMapper.createObjectNode();
            testEvent.put("listen", "test");
            
            ObjectNode script = objectMapper.createObjectNode();
            script.put("type", "text/javascript");
            
            ArrayNode exec = objectMapper.createArrayNode();
            for (String test : testScripts) {
                exec.add(test);
            }
            
            script.set("exec", exec);
            testEvent.set("script", script);
            events.add(testEvent);
        }
        
        // Add events if any
        if (events.size() > 0) {
            item.set("event", events);
        }
        
        // Set request to item
        item.set("request", request);
        
        // Add to parent items
        parentItems.add(item);
    }
    
    /**
     * Extract assertions from a REST request.
     * 
     * @param configElement The config element
     * @return List of Postman test scripts
     */
    private static List<String> extractAssertions(Element configElement) {
        List<String> tests = new ArrayList<>();
        
        Element assertionsElement = configElement.element("con:assertions");
        if (assertionsElement == null) {
            return tests;
        }
        
        for (Element assertionElement : assertionsElement.elements("con:assertion")) {
            String type = assertionElement.attributeValue("type");
            String name = assertionElement.attributeValue("name");
            
            if ("Valid HTTP Status Codes".equals(type)) {
                Element configElem = assertionElement.element("con:configuration");
                String codes = configElem != null ? configElem.elementTextTrim("con:codes") : "200";
                
                if (codes == null || codes.isEmpty()) {
                    codes = "200";
                }
                
                tests.add(String.format("pm.test(\"%s\", function() { pm.response.to.have.status(%s); });", name, codes));
            } else if ("JSONPath Match".equals(type) || "JsonPath Match".equals(type)) {
                Element configElem = assertionElement.element("con:configuration");
                if (configElem != null) {
                    String path = configElem.elementTextTrim("con:path");
                    
                    if (path != null) {
                        // Format the JSONPath for JavaScript
                        String formattedPath = formatJsonPath(path);
                        
                        tests.add(String.format(
                            "pm.test(\"Check JSONPath %s\", function() {\n" +
                            "  var jsonData = pm.response.json();\n" +
                            "  pm.expect(%s).to.exist;\n" +
                            "});", path, formattedPath));
                    }
                }
            } else if ("Contains".equals(type)) {
                Element configElem = assertionElement.element("con:configuration");
                if (configElem != null) {
                    String token = configElem.elementTextTrim("con:token");
                    
                    if (token != null) {
                        tests.add(String.format(
                            "pm.test(\"%s\", function() {\n" +
                            "  pm.expect(pm.response.text()).to.include(\"%s\");\n" +
                            "});", name, token));
                    }
                }
            } else if ("Script Assertion".equals(type)) {
                Element configElem = assertionElement.element("con:configuration");
                if (configElem != null) {
                    String scriptText = configElem.elementTextTrim("con:scriptText");
                    
                    if (scriptText != null && !scriptText.isEmpty()) {
                        String jsScript = convertGroovyToJs(scriptText);
                        
                        tests.add(String.format(
                            "pm.test(\"%s\", function() {\n" +
                            "  // Converted from Groovy script\n" +
                            "  %s\n" +
                            "});", name, jsScript));
                    }
                }
            }
        }
        
        return tests;
    }
    
    /**
     * Format a JSONPath for use in JavaScript.
     * 
     * @param jsonPath The JSONPath
     * @return Formatted path for JavaScript
     */
    private static String formatJsonPath(String jsonPath) {
        if (jsonPath == null || jsonPath.isEmpty()) {
            return "jsonData";
        }
        
        // Handle $ root
        String path = jsonPath.trim();
        if (path.startsWith("$")) {
            path = path.substring(1);
        }
        
        // Replace dot notation for JavaScript access
        StringBuilder result = new StringBuilder("jsonData");
        String[] segments = path.split("\\.");
        
        for (String segment : segments) {
            if (segment.isEmpty()) continue;
            
            // Handle array access [n]
            if (segment.contains("[") && segment.contains("]")) {
                int bracketIndex = segment.indexOf("[");
                String property = segment.substring(0, bracketIndex);
                
                if (!property.isEmpty()) {
                    result.append(".").append(property);
                }
                
                result.append(segment.substring(bracketIndex));
            } else {
                result.append(".").append(segment);
            }
        }
        
        return result.toString();
    }
    
    /**
     * Process a Groovy script test step.
     * 
     * @param testStepElement The test step element
     * @param stepName The step name
     * @param parentItems The parent items array
     */
    private static void processGroovyStep(Element testStepElement, String stepName, ArrayNode parentItems) {
        ObjectNode item = objectMapper.createObjectNode();
        item.put("name", stepName);
        
        // Create request object (we'll use a dummy GET request)
        ObjectNode request = objectMapper.createObjectNode();
        request.put("method", "GET");
        
        ObjectNode url = objectMapper.createObjectNode();
        url.put("raw", "https://postman-echo.com/get?groovy_script=" + stepName);
        request.set("url", url);
        
        // Set request to item
        item.set("request", request);
        
        // Get the script content
        Element configElement = testStepElement.element("con:config");
        if (configElement != null) {
            String scriptText = configElement.elementTextTrim("con:script");
            
            if (scriptText != null && !scriptText.isEmpty()) {
                // Convert Groovy to JavaScript
                String jsScript = convertGroovyToJs(scriptText);
                
                // Create a test event
                ObjectNode testEvent = objectMapper.createObjectNode();
                testEvent.put("listen", "test");
                
                ObjectNode script = objectMapper.createObjectNode();
                script.put("type", "text/javascript");
                
                ArrayNode exec = objectMapper.createArrayNode();
                exec.add("// Converted from Groovy script");
                
                // Add each line of the script
                for (String line : jsScript.split("\n")) {
                    exec.add(line);
                }
                
                script.set("exec", exec);
                testEvent.set("script", script);
                
                // Add event to item
                ArrayNode events = objectMapper.createArrayNode();
                events.add(testEvent);
                item.set("event", events);
            }
        }
        
        // Add to parent items
        parentItems.add(item);
    }
    
    /**
     * Process a properties test step.
     * 
     * @param testStepElement The test step element
     * @param stepName The step name
     * @param parentItems The parent items array
     */
    private static void processPropertiesStep(Element testStepElement, String stepName, ArrayNode parentItems) {
        ObjectNode item = objectMapper.createObjectNode();
        item.put("name", stepName);
        
        // Create request object (we'll use a dummy GET request)
        ObjectNode request = objectMapper.createObjectNode();
        request.put("method", "GET");
        
        ObjectNode url = objectMapper.createObjectNode();
        url.put("raw", "https://postman-echo.com/get?properties_step=" + stepName);
        request.set("url", url);
        
        // Set request to item
        item.set("request", request);
        
        // Get properties
        Element configElement = testStepElement.element("con:config");
        if (configElement != null) {
            Element propertiesElement = configElement.element("con:properties");
            
            if (propertiesElement != null) {
                // Create a pre-request script to set properties as variables
                ObjectNode preRequestEvent = objectMapper.createObjectNode();
                preRequestEvent.put("listen", "prerequest");
                
                ObjectNode script = objectMapper.createObjectNode();
                script.put("type", "text/javascript");
                
                ArrayNode exec = objectMapper.createArrayNode();
                exec.add("// Property definitions from ReadyAPI");
                
                // Add each property as a variable
                for (Element propertyElement : propertiesElement.elements("con:property")) {
                    String name = propertyElement.attributeValue("name");
                    String value = propertyElement.attributeValue("value");
                    
                    if (name != null && value != null) {
                        exec.add(String.format("pm.variables.set('%s', '%s');", 
                            name, escapeJsString(value)));
                    }
                }
                
                script.set("exec", exec);
                preRequestEvent.set("script", script);
                
                // Add event to item
                ArrayNode events = objectMapper.createArrayNode();
                events.add(preRequestEvent);
                item.set("event", events);
            }
        }
        
        // Add to parent items
        parentItems.add(item);
    }
    
    /**
     * Process a transfer test step.
     * 
     * @param testStepElement The test step element
     * @param stepName The step name
     * @param parentItems The parent items array
     */
    private static void processTransferStep(Element testStepElement, String stepName, ArrayNode parentItems) {
        ObjectNode item = objectMapper.createObjectNode();
        item.put("name", stepName);
        
        // Create request object (we'll use a dummy GET request)
        ObjectNode request = objectMapper.createObjectNode();
        request.put("method", "GET");
        
        ObjectNode url = objectMapper.createObjectNode();
        url.put("raw", "https://postman-echo.com/get?property_transfer=" + stepName);
        request.set("url", url);
        
        // Set request to item
        item.set("request", request);
        
        // Get transfers
        Element configElement = testStepElement.element("con:config");
        if (configElement != null) {
            Element transfersElement = configElement.element("con:transfers");
            
            if (transfersElement != null) {
                // Create a pre-request script for property transfers
                ObjectNode preRequestEvent = objectMapper.createObjectNode();
                preRequestEvent.put("listen", "prerequest");
                
                ObjectNode script = objectMapper.createObjectNode();
                script.put("type", "text/javascript");
                
                ArrayNode exec = objectMapper.createArrayNode();
                exec.add("// Property transfers from ReadyAPI");
                
                // Process each transfer
                for (Element transferElement : transfersElement.elements("con:transfer")) {
                    String transferName = transferElement.elementTextTrim("con:name");
                    
                    Element sourceElement = transferElement.element("con:source");
                    Element targetElement = transferElement.element("con:target");
                    
                    if (sourceElement != null && targetElement != null) {
                        String sourceName = sourceElement.elementTextTrim("con:sourceName");
                        String sourcePath = sourceElement.elementTextTrim("con:sourcePath");
                        String sourcePathLanguage = sourceElement.elementTextTrim("con:pathLanguage");
                        
                        String targetName = targetElement.elementTextTrim("con:targetName");
                        String targetPath = targetElement.elementTextTrim("con:targetPath");
                        
                        if (sourceName != null && sourcePath != null && targetPath != null) {
                            if ("jsonpath".equalsIgnoreCase(sourcePathLanguage)) {
                                String formattedPath = formatJsonPath(sourcePath);
                                
                                exec.add("// Transfer from previous response");
                                exec.add("try {");
                                exec.add("    // Get value from previous response using JSONPath");
                                exec.add(String.format("    var sourceResponse = pm.variables.get('%s_response');", sourceName));
                                exec.add("    if (sourceResponse) {");
                                exec.add("        var sourceJson = JSON.parse(sourceResponse);");
                                exec.add(String.format("        var sourceValue = %s;", 
                                    formattedPath.replace("jsonData", "sourceJson")));
                                exec.add(String.format("        pm.variables.set('%s', sourceValue);", targetPath));
                                exec.add(String.format("        console.log('Transferred value to %s: ' + sourceValue);", targetPath));
                                exec.add("    }");
                                exec.add("} catch (err) {");
                                exec.add("    console.error('Error in property transfer: ' + err);");
                                exec.add("}");
                            } else {
                                exec.add(String.format("// Property transfer (type: %s)", sourcePathLanguage));
                                exec.add(String.format("// Source: %s, Path: %s", sourceName, sourcePath));
                                exec.add(String.format("// Target: %s, Path: %s", targetName, targetPath));
                                exec.add("// You may need to implement this transfer manually");
                            }
                        }
                    }
                }
                
                script.set("exec", exec);
                preRequestEvent.set("script", script);
                
                // Add event to item
                ArrayNode events = objectMapper.createArrayNode();
                events.add(preRequestEvent);
                item.set("event", events);
            }
        }
        
        // Add to parent items
        parentItems.add(item);
    }
    
    /**
     * Process a data source test step.
     * 
     * @param testStepElement The test step element
     * @param stepName The step name
     * @param parentItems The parent items array
     */
    private static void processDataSourceStep(Element testStepElement, String stepName, ArrayNode parentItems) {
        ObjectNode item = objectMapper.createObjectNode();
        item.put("name", stepName);
        
        // Create request object (we'll use a dummy GET request)
        ObjectNode request = objectMapper.createObjectNode();
        request.put("method", "GET");
        
        ObjectNode url = objectMapper.createObjectNode();
        url.put("raw", "https://postman-echo.com/get?datasource=" + stepName);
        request.set("url", url);
        
        // Set request to item
        item.set("request", request);
        
        // Get data source info
        Element configElement = testStepElement.element("con:config");
        if (configElement != null) {
            // Create a pre-request script with data source info
            ObjectNode preRequestEvent = objectMapper.createObjectNode();
            preRequestEvent.put("listen", "prerequest");
            
            ObjectNode script = objectMapper.createObjectNode();
            script.put("type", "text/javascript");
            
            ArrayNode exec = objectMapper.createArrayNode();
            exec.add("// DataSource step from ReadyAPI");
            exec.add("// Postman uses Collection Runner and CSV/JSON data for data-driven testing");
            
            String dataSourceType = configElement.elementTextTrim("con:type");
            if (dataSourceType != null) {
                exec.add("// Original data source type: " + dataSourceType);
                
                if ("Excel".equalsIgnoreCase(dataSourceType) || "CSV".equalsIgnoreCase(dataSourceType)) {
                    String file = configElement.elementTextTrim("con:file");
                    if (file != null) {
                        exec.add("// File: " + file);
                        exec.add("// To use in Postman: Convert this file to CSV and use with Collection Runner");
                    }
                }
            }
            
            script.set("exec", exec);
            preRequestEvent.set("script", script);
            
            // Add event to item
            ArrayNode events = objectMapper.createArrayNode();
            events.add(preRequestEvent);
            item.set("event", events);
        }
        
        // Add to parent items
        parentItems.add(item);
    }
    
    /**
     * Process interfaces from ReadyAPI project.
     * 
     * @param root The project root element
     * @param items The Postman items array
     */
    private static void processInterfaces(Element root, ArrayNode items) {
        Element interfacesElement = root.element("con:interface");
        if (interfacesElement == null) {
            return;
        }
        
        ObjectNode interfacesFolder = objectMapper.createObjectNode();
        interfacesFolder.put("name", "Interfaces");
        ArrayNode interfaceItems = objectMapper.createArrayNode();
        
        // Not fully implementing interface processing for brevity
        // A complete implementation would process resources and methods
        
        // Add interface items to interfaces folder if any
        if (interfaceItems.size() > 0) {
            interfacesFolder.set("item", interfaceItems);
            items.add(interfacesFolder);
        }
    }
    
    /**
     * Convert Groovy script to JavaScript.
     * Simplified version that handles common patterns.
     * 
     * @param groovyScript The Groovy script to convert
     * @return Converted JavaScript
     */
    private static String convertGroovyToJs(String groovyScript) {
        if (groovyScript == null || groovyScript.isEmpty()) {
            return "";
        }
        
        List<String> jsLines = new ArrayList<>();
        jsLines.add("// Converted from Groovy script");
        
        for (String line : groovyScript.split("\n")) {
            String original = line;
            line = line.trim();
            
            if (line.isEmpty()) {
                jsLines.add("");
                continue;
            }
            
            if (line.startsWith("//")) {
                jsLines.add(line);
                continue;
            }
            
            // Convert log.info to console.log
            line = line.replaceAll("log\\.info\\((.+?)\\)", "console.log($1);");
            
            // Skip JsonSlurper declarations
            if (line.contains("new JsonSlurper().parseText(")) {
                continue;
            }
            
            // Convert property access patterns
            Pattern propertyPattern = Pattern.compile("def (\\w+)\\s*=\\s*testRunner\\.testCase\\.testSteps\\[\"(.+?)\"\\]\\.getPropertyValue\\(\"(.+?)\"\\)");
            Matcher propertyMatcher = propertyPattern.matcher(line);
            if (propertyMatcher.find()) {
                String varName = propertyMatcher.group(1);
                String step = propertyMatcher.group(2);
                String prop = propertyMatcher.group(3);
                jsLines.add(String.format("let %s = pm.collectionVariables.get('%s_%s');", varName, step, prop));
                continue;
            }
            
            // Convert JSON field access
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
                String left = assertMatcher.group(1).trim();
                String right = assertMatcher.group(2).trim();
                jsLines.add(String.format("pm.test('Assert %s == %s', function () {", left, right));
                jsLines.add(String.format("    pm.expect(%s).to.eql(%s);", left, right));
                jsLines.add("});");
                continue;
            }
            
            // Add the line if no specific conversion found
            if (!jsLines.contains(line) && !line.isEmpty()) {
                jsLines.add(line);
            }
        }
        
        // Join lines
        StringBuilder jsScript = new StringBuilder();
        for (String line : jsLines) {
            jsScript.append(line).append("\n");
        }
        
        return jsScript.toString();
    }
    
    /**
     * Escape a string for use in JavaScript.
     * 
     * @param input The input string
     * @return Escaped string
     */
    private static String escapeJsString(String input) {
        if (input == null) {
            return "";
        }
        
        return input.replace("\\", "\\\\")
                  .replace("'", "\\'")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
} 