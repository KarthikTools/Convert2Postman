package com.readyapi.converter;

import com.readyapi.converter.postman.PostmanItem;
import com.readyapi.converter.postman.PostmanRequest;
import com.readyapi.converter.postman.PostmanEvent;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.DOMReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.dom4j.io.DocumentSource;
import org.dom4j.io.SAXContentHandler;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.sax.SAXSource;
import org.xml.sax.InputSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.util.ArrayList;

/**
 * Parser for ReadyAPI project XML files.
 */
public class ReadyApiProjectParser {
    private static final Logger logger = LoggerFactory.getLogger(ReadyApiProjectParser.class);
    
    static {
        // Initialize logging without XML configuration
        System.setProperty("logback.configurationFile", "");
    }
    
    private Element rootElement;
    
    /**
     * Parse a ReadyAPI project XML file.
     * 
     * @param filePath Path to the ReadyAPI project XML file
     * @return A ReadyApiProject object with parsed project data
     * @throws DocumentException If there's an error parsing the XML
     */
    public ReadyApiProject parse(String filePath) throws DocumentException {
        logger.info("Parsing ReadyAPI project file: {}", filePath);
        
        try {
            Document document = null;
            
            // Try with SAX parser first
            try {
                // Configure Woodstox as the StAX implementation with larger buffers
                System.setProperty("javax.xml.stream.XMLInputFactory", "com.ctc.wstx.stax.WstxInputFactory");
                System.setProperty("javax.xml.stream.XMLOutputFactory", "com.ctc.wstx.stax.WstxOutputFactory");
                System.setProperty("javax.xml.stream.XMLEventFactory", "com.ctc.wstx.stax.WstxEventFactory");
                
                // Increase buffer sizes significantly
                System.setProperty("org.xml.sax.parser.bufferSize", "131072"); // 128KB
                System.setProperty("elementAttributeLimit", "131072");
                System.setProperty("org.apache.xerces.xni.parser.XMLInputBufferSize", "131072");
                System.setProperty("com.ctc.wstx.inputBufferSize", "131072");
                System.setProperty("com.ctc.wstx.outputBufferSize", "131072");
                
                // Use DOM4J with SAXReader for better error handling
                SAXReader reader = createConfiguredReader();
                
                // Configure namespace handling
                Map<String, String> nsMap = new HashMap<>();
                nsMap.put("con", "http://eviware.com/soapui/config");
                reader.getDocumentFactory().setXPathNamespaceURIs(nsMap);
                
                // Parse the document with SAX parser using streaming
                try (FileInputStream fis = new FileInputStream(new File(filePath))) {
                    // Use a buffered input stream for better performance
                    BufferedInputStream bis = new BufferedInputStream(fis, 131072);
                    document = reader.read(bis);
                } catch (IOException e) {
                    logger.error("IO error reading project file: {}", filePath, e);
                    throw new DocumentException("IO error reading project file: " + e.getMessage(), e);
                }
            } catch (Exception e) {
                // If SAX parsing fails, try DOM parsing which handles large files better
                logger.warn("SAX parsing failed, trying DOM parsing instead: {}", e.getMessage());
                
                try {
                    // Use DOM parser with optimized settings for large files
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    factory.setNamespaceAware(true);
                    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                    factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
                    factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                    
                    // Set larger buffer sizes for DOM parser
                    factory.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                    factory.setAttribute("http://xml.org/sax/features/validation", false);
                    factory.setAttribute("http://apache.org/xml/features/continue-after-fatal-error", true);
                    
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    
                    // Use a buffered input stream for better performance
                    try (FileInputStream fis = new FileInputStream(new File(filePath));
                         BufferedInputStream bis = new BufferedInputStream(fis, 131072)) {
                        org.w3c.dom.Document w3cDoc = builder.parse(bis);
                        
                        // Convert W3C DOM to DOM4J
                        DOMReader domReader = new DOMReader();
                        document = domReader.read(w3cDoc);
                        
                        logger.info("Successfully parsed with DOM parser");
                    }
                } catch (IOException ioEx) {
                    logger.error("IO error reading project file: {}", filePath, ioEx);
                    throw new DocumentException("IO error reading project file: " + ioEx.getMessage(), ioEx);
                } catch (Exception domEx) {
                    logger.error("DOM parsing also failed: {}", domEx.getMessage());
                    throw new DocumentException("Failed to parse XML with both SAX and DOM parsers", domEx);
                }
            }
            
            if (document == null) {
                throw new DocumentException("Failed to parse XML document: document is null");
            }
            
            rootElement = document.getRootElement();
            
            // Detect project version and structure
            String projectVersion = detectProjectVersion(rootElement);
            logger.info("Detected project version: {}", projectVersion);
            
            // Use adapter pattern to handle different project structures
            ProjectStructureAdapter adapter = ProjectStructureAdapterFactory.getAdapter(projectVersion);
            
            if (adapter.canHandle(rootElement)) {
                return adapter.parseProject(rootElement, filePath);
            } else {
                // Fallback to direct parsing
                return parseProjectElement(rootElement, filePath);
            }
        } catch (DocumentException e) {
            logger.error("Error parsing project file: {}", filePath, e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error parsing project file: {}", filePath, e);
            throw new DocumentException("Unexpected error: " + e.getMessage(), e);
        }
    }
    
    /**
     * Create a properly configured SAXReader
     * 
     * @return The configured SAXReader
     */
    private SAXReader createConfiguredReader() {
        SAXReader reader = new SAXReader();
        
        try {
            // Configure SAX parser features for security and compatibility
            reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
            reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            
            // Set larger buffer size via system property
            System.setProperty("org.apache.xerces.xni.parser.XMLInputBufferSize", "65536");
            
            // Disable DTD validation to improve performance
            reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            reader.setValidation(false);
            
        } catch (SAXException e) {
            logger.warn("Could not set recommended SAX parser feature", e);
        }
        
        return reader;
    }
    
    /**
     * Detect ReadyAPI project version.
     * 
     * @param rootElement The root XML element
     * @return The detected project version
     */
    private String detectProjectVersion(Element rootElement) {
        // Try different attributes to determine version
        String version = rootElement.attributeValue("created");
        if (version == null) {
            version = rootElement.attributeValue("soapui-version");
        }
        if (version == null) {
            version = "unknown";
        }
        return version;
    }
    
    /**
     * Parse a project element.
     * 
     * @param rootElement The root XML element
     * @param filePath Path to the project file
     * @return The parsed ReadyAPI project
     */
    public ReadyApiProject parseProjectElement(Element rootElement, String filePath) {
        ReadyApiProject project = new ReadyApiProject();
        project.setName(rootElement.attributeValue("name"));
        project.setFilePath(filePath);
        
        // Parse project properties
        parseProjectProperties(rootElement, project);
        
        // Parse project references for composite projects
        parseProjectReferences(rootElement, project);
        
        // Parse interfaces
        parseInterfaces(rootElement, project);
        
        // Parse test suites and direct test cases
        parseTestSuites(rootElement, project);
        
        // Parse script libraries
        parseScriptLibraries(rootElement, project);
        
        return project;
    }
    
    /**
     * Parse project references for composite projects.
     * 
     * @param rootElement The root XML element
     * @param project The ReadyAPI project to populate
     */
    private void parseProjectReferences(Element rootElement, ReadyApiProject project) {
        Element referencesElement = rootElement.element("projectReferences");
        if (referencesElement != null) {
            List<Element> referenceElements = referencesElement.elements("projectReference");
            for (Element referenceElement : referenceElements) {
                String refPath = referenceElement.attributeValue("path");
                if (refPath != null && !refPath.isEmpty()) {
                    logger.info("Found project reference: {}", refPath);
                    project.addProjectReference(refPath);
                    
                    // Try to load the referenced project
                    try {
                        File referencedFile = resolveReferencePath(refPath, project.getFilePath());
                        if (referencedFile.exists()) {
                            ReadyApiProject referencedProject = parse(referencedFile.getPath());
                            project.addReferencedProject(referencedProject);
                            logger.info("Successfully loaded referenced project: {}", refPath);
                        } else {
                            logger.warn("Referenced project file not found: {}", referencedFile.getPath());
                        }
                    } catch (Exception e) {
                        logger.error("Failed to load referenced project: {}", refPath, e);
                    }
                }
            }
        }
    }
    
    /**
     * Resolve a referenced project path relative to the main project.
     * 
     * @param referencePath The reference path from XML
     * @param mainProjectPath The main project file path
     * @return The resolved reference file
     */
    private File resolveReferencePath(String referencePath, String mainProjectPath) {
        File mainProjectFile = new File(mainProjectPath);
        File mainProjectDir = mainProjectFile.getParentFile();
        
        // Handle both absolute and relative paths
        if (new File(referencePath).isAbsolute()) {
            return new File(referencePath);
        } else {
            return new File(mainProjectDir, referencePath);
        }
    }
    
    /**
     * Parse project properties.
     * 
     * @param rootElement The XML root element
     * @param project The project to populate
     */
    private void parseProjectProperties(Element rootElement, ReadyApiProject project) {
        Element propertiesElement = rootElement.element("properties");
        if (propertiesElement != null) {
            List<Element> propertyElements = propertiesElement.elements("property");
            for (Element propertyElement : propertyElements) {
                String name = propertyElement.elementText("name");
                String value = propertyElement.elementText("value");
                if (name != null && value != null) {
                    project.addProperty(name, value);
                }
            }
        }
    }
    
    /**
     * Parse interfaces.
     * 
     * @param rootElement The XML root element
     * @param project The project to populate
     */
    private void parseInterfaces(Element rootElement, ReadyApiProject project) {
        List<Element> interfaceElements = rootElement.elements("interface");
        for (Element interfaceElement : interfaceElements) {
            ReadyApiInterface apiInterface = new ReadyApiInterface();
            apiInterface.setId(interfaceElement.attributeValue("id"));
            apiInterface.setName(interfaceElement.attributeValue("name"));
            
            // Parse resources
            List<Element> resourceElements = interfaceElement.elements("resource");
            for (Element resourceElement : resourceElements) {
                ReadyApiResource resource = new ReadyApiResource();
                resource.setId(resourceElement.attributeValue("id"));
                resource.setName(resourceElement.attributeValue("name"));
                resource.setPath(resourceElement.attributeValue("path"));
                
                // Parse methods
                List<Element> methodElements = resourceElement.elements("method");
                for (Element methodElement : methodElements) {
                    ReadyApiMethod method = new ReadyApiMethod();
                    method.setId(methodElement.attributeValue("id"));
                    method.setName(methodElement.attributeValue("name"));
                    method.setHttpMethod(methodElement.attributeValue("method"));
                    
                    // Parse requests
                    List<Element> requestElements = methodElement.elements("request");
                    for (Element requestElement : requestElements) {
                        ReadyApiRequest request = new ReadyApiRequest();
                        request.setId(requestElement.attributeValue("id"));
                        request.setName(requestElement.attributeValue("name"));
                        request.setMediaType(requestElement.attributeValue("mediaType"));
                        
                        // Extract endpoint and other properties
                        Element endpointElement = requestElement.element("endpoint");
                        if (endpointElement != null) {
                            request.setEndpoint(endpointElement.getTextTrim());
                        }
                        
                        Element requestMethodElement = requestElement.element("method");
                        if (requestMethodElement != null) {
                            request.setMethod(requestMethodElement.getTextTrim());
                        }
                        
                        // Extract request body
                        Element requestBodyElement = requestElement.element("request");
                        if (requestBodyElement != null) {
                            request.setRequestContent(requestBodyElement.getTextTrim());
                        }
                        
                        // Extract headers
                        Element headersElement = requestElement.element("headers");
                        if (headersElement != null) {
                            for (Element headerElement : headersElement.elements("header")) {
                                String headerName = headerElement.attributeValue("name");
                                String headerValue = headerElement.attributeValue("value");
                                if (headerName != null && headerValue != null) {
                                    request.addHeader(headerName, headerValue);
                                }
                            }
                        }
                        
                        // Parse assertions
                        List<Element> assertionElements = requestElement.elements("assertion");
                        for (Element assertionElement : assertionElements) {
                            ReadyApiAssertion assertion = parseAssertion(assertionElement);
                            request.addAssertion(assertion);
                        }
                        
                        method.addRequest(request);
                    }
                    
                    resource.addMethod(method);
                }
                
                apiInterface.addResource(resource);
            }
            
            project.addInterface(apiInterface);
        }
    }
    
    /**
     * Parse test suites and direct test cases.
     * 
     * @param rootElement The XML root element
     * @param project The project to populate
     */
    private void parseTestSuites(Element rootElement, ReadyApiProject project) {
        // First try to parse test suites
        List<Element> testSuiteElements = rootElement.elements("testSuite");
        if (!testSuiteElements.isEmpty()) {
            for (Element testSuiteElement : testSuiteElements) {
                ReadyApiTestSuite testSuite = parseTestSuite(testSuiteElement);
                project.addTestSuite(testSuite);
            }
        }
        
        // Look for direct test cases at the project level (for composite projects)
        List<Element> directTestCaseElements = rootElement.elements("testCase");
        if (!directTestCaseElements.isEmpty()) {
            logger.info("Found {} direct test cases at project level", directTestCaseElements.size());
            for (Element testCaseElement : directTestCaseElements) {
                ReadyApiTestCase testCase = parseTestCase(testCaseElement);
                project.addDirectTestCase(testCase);
            }
        }
    }
    
    /**
     * Parse a test suite element.
     * 
     * @param testSuiteElement The test suite element
     * @return The parsed test suite
     */
    private ReadyApiTestSuite parseTestSuite(Element testSuiteElement) {
        ReadyApiTestSuite testSuite = new ReadyApiTestSuite();
        testSuite.setId(testSuiteElement.attributeValue("id"));
        testSuite.setName(testSuiteElement.attributeValue("name"));
        testSuite.setRunType(testSuiteElement.attributeValue("runType"));
        
        // Parse test suite properties
        Element propertiesElement = testSuiteElement.element("properties");
        if (propertiesElement != null) {
            List<Element> propertyElements = propertiesElement.elements("property");
            for (Element propertyElement : propertyElements) {
                String name = propertyElement.elementText("name");
                String value = propertyElement.elementText("value");
                if (name != null && value != null) {
                    testSuite.addProperty(name, value);
                }
            }
        }
        
        // Parse test cases
        List<Element> testCaseElements = testSuiteElement.elements("testCase");
        for (Element testCaseElement : testCaseElements) {
            ReadyApiTestCase testCase = parseTestCase(testCaseElement);
            testSuite.addTestCase(testCase);
        }
        
        return testSuite;
    }
    
    /**
     * Parse a test case element.
     * 
     * @param testCaseElement The test case element
     * @return The parsed test case
     */
    private ReadyApiTestCase parseTestCase(Element testCaseElement) {
        ReadyApiTestCase testCase = new ReadyApiTestCase();
        testCase.setId(testCaseElement.attributeValue("id"));
        testCase.setName(testCaseElement.attributeValue("name"));
        
        // Parse test case properties
        Element testCasePropertiesElement = testCaseElement.element("properties");
        if (testCasePropertiesElement != null) {
            List<Element> testCasePropertyElements = testCasePropertiesElement.elements("property");
            for (Element propertyElement : testCasePropertyElements) {
                String name = propertyElement.elementText("name");
                String value = propertyElement.elementText("value");
                if (name != null && value != null) {
                    testCase.addProperty(name, value);
                }
            }
        }
        
        // Parse test steps
        List<Element> testStepElements = testCaseElement.elements("testStep");
        for (Element testStepElement : testStepElements) {
            ReadyApiTestStep testStep = parseTestStep(testStepElement);
            testCase.addTestStep(testStep);
        }
        
        return testCase;
    }
    
    /**
     * Parse a test step element.
     * 
     * @param stepElement The step element to parse
     * @return The parsed test step
     */
    private ReadyApiTestStep parseTestStep(Element stepElement) {
        ReadyApiTestStep step = new ReadyApiTestStep(stepElement);
        
        // Parse property transfers
        Element transfersElement = stepElement.element("transfers");
        if (transfersElement != null) {
            List<Element> transferElements = transfersElement.elements("transfer");
            for (Element transferElement : transferElements) {
                ReadyApiTestStep.PropertyTransfer transfer = new ReadyApiTestStep.PropertyTransfer(transferElement);
                step.addPropertyTransfer(transfer);
            }
        }
        
        return step;
    }
    
    /**
     * Parse script libraries.
     * 
     * @param rootElement The XML root element
     * @param project The project to populate
     */
    private void parseScriptLibraries(Element rootElement, ReadyApiProject project) {
        Element scriptLibraryElement = rootElement.element("scriptLibrary");
        if (scriptLibraryElement != null) {
            List<Element> libraryConfigElements = scriptLibraryElement.elements("libraryConfig");
            for (Element libraryConfigElement : libraryConfigElements) {
                ReadyApiScriptLibrary scriptLibrary = new ReadyApiScriptLibrary();
                scriptLibrary.setId(libraryConfigElement.attributeValue("id"));
                scriptLibrary.setName(libraryConfigElement.attributeValue("name"));
                
                // Parse script content
                Element groovyScriptElement = libraryConfigElement.element("groovyScript");
                if (groovyScriptElement != null) {
                    scriptLibrary.setContent(groovyScriptElement.getTextTrim());
                }
                
                project.addScriptLibrary(scriptLibrary);
            }
        }
    }
    
    /**
     * Parse assertion.
     * 
     * @param assertionElement The assertion element
     * @return The parsed assertion
     */
    private ReadyApiAssertion parseAssertion(Element assertionElement) {
        // Use the new constructor that takes an XML element
        return new ReadyApiAssertion(assertionElement);
    }
    
    /**
     * Get the root element of the parsed project.
     * 
     * @return The root element of the parsed project
     */
    public Element getRootElement() {
        return rootElement;
    }
    
    private ReadyApiRequest parseRequest(Element configElement) {
        ReadyApiRequest request = new ReadyApiRequest();
        
        // Extract method and endpoint
        request.setMethod(configElement.elementTextTrim("method"));
        request.setEndpoint(configElement.elementTextTrim("endpoint"));
        
        // Extract media type
        Element mediaTypeElement = configElement.element("mediaType");
        if (mediaTypeElement != null) {
            request.setMediaType(mediaTypeElement.getTextTrim());
        }
        
        // Extract request body
        Element requestElement = configElement.element("request");
        if (requestElement != null) {
            request.setBody(requestElement.getTextTrim());
        }
        
        // Extract headers
        Element headersElement = configElement.element("headers");
        if (headersElement != null) {
            for (Element headerElement : headersElement.elements("entry")) {
                String headerName = headerElement.attributeValue("key");
                String headerValue = headerElement.getTextTrim();
                request.addHeader(headerName, headerValue);
            }
        }
        
        return request;
    }
    
    /**
     * Convert a Groovy script to JavaScript
     * 
     * @param groovyScript The Groovy script to convert
     * @return The converted JavaScript code
     */
    private String convertGroovyToJavaScript(String groovyScript) {
        if (groovyScript == null || groovyScript.trim().isEmpty()) {
            return "";
        }
        
        StringBuilder jsCode = new StringBuilder();
        jsCode.append("// Converted from Groovy script\n");
        
        String[] lines = groovyScript.split("\n");
        for (String line : lines) {
            line = line.trim();
            
            if (line.isEmpty()) {
                jsCode.append("\n");
                continue;
            }
            
            if (line.startsWith("//")) {
                jsCode.append(line).append("\n");
                continue;
            }
            
            // Skip JsonSlurper lines as they're handled by pm.response.json()
            if (line.contains("new JsonSlurper().parseText(")) {
                continue;
            }
            
            // Convert log.info to console.log
            line = line.replaceAll("log\\.info\\((.+?)\\)", "console.log($1);");
            
            // Convert property value extraction
            line = line.replaceAll(
                "def (\\w+)\\s*=\\s*testRunner\\.testCase\\.testSteps\\[\"(.+?)\"\\]\\.getPropertyValue\\(\"(.+?)\"\\)",
                "let $1 = pm.collectionVariables.get('$2_$3');"
            );
            
            // Convert JSON parsing
            line = line.replaceAll(
                "def (\\w+)\\s*=\\s*parse_json\\.(\\w+)",
                "let $1 = pm.response.json().$2;"
            );
            
            // Convert assertions
            line = line.replaceAll(
                "assert (.+?)\\s*==\\s*(.+)",
                "pm.test('Assert $1 == $2', function () {\n    pm.expect($1).to.eql($2);\n});"
            );
            
            // Convert integer conversion
            line = line.replaceAll(
                "int (\\w+)\\s*=\\s*(\\w+)\\.toInteger\\(\\)",
                "let $1 = parseInt($2);"
            );
            
            jsCode.append(line).append("\n");
        }
        
        return jsCode.toString();
    }
    
    /**
     * Convert ReadyAPI assertions to Postman test scripts
     * 
     * @param assertions List of assertion elements from ReadyAPI
     * @return List of Postman test scripts
     */
    private List<String> convertAssertionsToTests(List<Element> assertions) {
        List<String> tests = new ArrayList<>();
        
        for (Element assertion : assertions) {
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
    
    /**
     * Convert a test step to a Postman item
     */
    public PostmanItem convertTestStep(Element testStep) {
        PostmanItem item = new PostmanItem();
        item.setName(testStep.attributeValue("name"));
        
        // Convert request
        Element requestElement = testStep.element("request");
        if (requestElement != null) {
            PostmanRequest request = convertRequest(requestElement);
            item.setRequest(request);
        }
        
        // Convert events (pre-request script and test)
        List<Element> eventElements = testStep.elements("event");
        for (Element eventElement : eventElements) {
            PostmanItem.PostmanEvent event = new PostmanItem.PostmanEvent();
            event.setListen(eventElement.attributeValue("type"));
            
            Element scriptElement = eventElement.element("script");
            if (scriptElement != null) {
                PostmanItem.PostmanScript script = new PostmanItem.PostmanScript(
                    "text/javascript",
                    scriptElement.getTextTrim()
                );
                event.setScript(script);
            }
            
            item.addEvent(event);
        }
        
        return item;
    }
    
    private PostmanRequest convertRequest(Element requestElement) {
        PostmanRequest request = new PostmanRequest();
        
        // Set method
        String method = requestElement.attributeValue("method");
        if (method != null) {
            request.setMethod(method.toUpperCase());
        }
        
        // Set URL
        Element urlElement = requestElement.element("url");
        if (urlElement != null) {
            PostmanRequest.PostmanUrl url = new PostmanRequest.PostmanUrl();
            url.setRaw(urlElement.getTextTrim());
            request.setUrl(url);
        }
        
        // Set headers
        Element headersElement = requestElement.element("headers");
        if (headersElement != null) {
            List<Element> headerElements = headersElement.elements("header");
            for (Element headerElement : headerElements) {
                String key = headerElement.attributeValue("name");
                String value = headerElement.attributeValue("value");
                if (key != null && value != null) {
                    PostmanRequest.PostmanHeader header = new PostmanRequest.PostmanHeader(key, value);
                    request.addHeader(header);
                }
            }
        }
        
        // Set body
        Element bodyElement = requestElement.element("body");
        if (bodyElement != null) {
            PostmanRequest.PostmanBody body = new PostmanRequest.PostmanBody();
            body.setMode("raw");
            body.setRaw(bodyElement.getTextTrim());
            request.setBody(body);
        }
        
        return request;
    }
} 
