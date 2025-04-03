package com.readyapi.converter;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
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
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.dom4j.io.DOMReader;

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
            // Configure Woodstox as the StAX implementation
            System.setProperty("javax.xml.stream.XMLInputFactory", "com.ctc.wstx.stax.WstxInputFactory");
            System.setProperty("javax.xml.stream.XMLOutputFactory", "com.ctc.wstx.stax.WstxOutputFactory");
            System.setProperty("javax.xml.stream.XMLEventFactory", "com.ctc.wstx.stax.WstxEventFactory");
            
            // Use DOM4J with SAXReader for better error handling
            SAXReader reader = createConfiguredReader();
            
            // Configure namespace handling
            Map<String, String> nsMap = new HashMap<>();
            nsMap.put("con", "http://eviware.com/soapui/config");
            reader.getDocumentFactory().setXPathNamespaceURIs(nsMap);
            
            // Parse the document with proper error handling
            Document document;
            try (FileInputStream fis = new FileInputStream(new File(filePath))) {
                document = reader.read(fis);
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
        } catch (IOException e) {
            logger.error("IO error reading project file: {}", filePath, e);
            throw new DocumentException("IO error reading project file: " + e.getMessage(), e);
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
            
            // Set a validating reader to handle entity references properly
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
                        
                        // Set endpoint
                        Element endpointElement = requestElement.element("endpoint");
                        if (endpointElement != null) {
                            request.setEndpoint(endpointElement.getTextTrim());
                        }
                        
                        // Set request body
                        Element requestBodyElement = requestElement.element("request");
                        if (requestBodyElement != null) {
                            request.setRequestBody(requestBodyElement.getTextTrim());
                        }
                        
                        // Parse request headers
                        Element headersElement = requestElement.element("headers");
                        if (headersElement != null) {
                            for (Element headerElement : headersElement.elements("header")) {
                                String headerName = headerElement.attributeValue("name");
                                String headerValue = headerElement.attributeValue("value");
                                if (headerName != null && headerValue != null) {
                                    request.addRequestHeader(headerName, headerValue);
                                }
                            }
                        }
                        
                        // Parse assertions
                        List<Element> assertionElements = requestElement.elements("assertion");
                        for (Element assertionElement : assertionElements) {
                            ReadyApiAssertion assertion = new ReadyApiAssertion();
                            assertion.setId(assertionElement.attributeValue("id"));
                            assertion.setName(assertionElement.attributeValue("name"));
                            assertion.setType(assertionElement.attributeValue("type"));
                            
                            // Parse assertion configuration
                            Element configElement = assertionElement.element("configuration");
                            if (configElement != null) {
                                List<Element> configChildElements = configElement.elements();
                                for (Element configChild : configChildElements) {
                                    assertion.addConfigurationProperty(configChild.getName(), configChild.getTextTrim());
                                }
                            }
                            
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
     * @param testStepElement The test step element
     * @return The parsed test step
     */
    private ReadyApiTestStep parseTestStep(Element testStepElement) {
        ReadyApiTestStep testStep = new ReadyApiTestStep();
        testStep.setId(testStepElement.attributeValue("id"));
        testStep.setName(testStepElement.attributeValue("name"));
        testStep.setType(testStepElement.attributeValue("type"));
        
        // Parse test step configuration
        Element configElement = testStepElement.element("config");
        if (configElement != null) {
            parseTestStepConfig(configElement, testStep);
        }
        
        return testStep;
    }
    
    /**
     * Parse test step configuration.
     * 
     * @param configElement The config element
     * @param testStep The test step to populate
     */
    private void parseTestStepConfig(Element configElement, ReadyApiTestStep testStep) {
        if ("groovy".equalsIgnoreCase(testStep.getType())) {
            // Parse Groovy script
            Element scriptElement = configElement.element("script");
            if (scriptElement != null) {
                testStep.addProperty("script", scriptElement.getTextTrim());
                testStep.setContent(scriptElement.getTextTrim());
            }
        } else if ("restrequest".equalsIgnoreCase(testStep.getType())) {
            // Parse REST request
            Element restRequestElement = configElement.element("restRequest");
            if (restRequestElement != null) {
                parseRestRequest(restRequestElement, testStep);
            }
        } else if ("properties".equalsIgnoreCase(testStep.getType())) {
            // Parse properties test step
            Element propertiesElement = configElement.element("properties");
            if (propertiesElement != null) {
                parsePropertiesStep(propertiesElement, testStep);
            }
        } else if ("datasource".equalsIgnoreCase(testStep.getType())) {
            // Parse data source test step
            parseDataSourceStep(configElement, testStep);
        } else if ("datasourceloop".equalsIgnoreCase(testStep.getType())) {
            // Parse data source loop test step
            parseSimplePropertiesStep(configElement, testStep);
        } else if ("propertytransfer".equalsIgnoreCase(testStep.getType())) {
            // Parse property transfer test step
            parsePropertyTransferStep(configElement, testStep);
        } else if ("datasink".equalsIgnoreCase(testStep.getType())) {
            // Parse datasink test step
            parseDataSinkStep(configElement, testStep);
        } else {
            // For other types, just extract all properties
            parseSimplePropertiesStep(configElement, testStep);
        }
    }
    
    /**
     * Parse REST request.
     * 
     * @param restRequestElement The REST request element
     * @param testStep The test step to populate
     */
    private void parseRestRequest(Element restRequestElement, ReadyApiTestStep testStep) {
        ReadyApiRequest request = new ReadyApiRequest();
        request.setId(restRequestElement.attributeValue("id"));
        request.setName(restRequestElement.attributeValue("name"));
        request.setMediaType(restRequestElement.attributeValue("mediaType"));
        
        // Parse request settings (headers, etc.)
        Element settingsElement = restRequestElement.element("settings");
        if (settingsElement != null) {
            parseRequestSettings(settingsElement, request);
        }
        
        // Set endpoint
        Element endpointElement = restRequestElement.element("endpoint");
        if (endpointElement != null) {
            request.setEndpoint(endpointElement.getTextTrim());
        }
        
        // Set request body
        Element requestBodyElement = restRequestElement.element("request");
        if (requestBodyElement != null) {
            request.setBody(requestBodyElement.getTextTrim());
        }
        
        // Parse assertions
        List<Element> assertionElements = restRequestElement.elements("assertion");
        for (Element assertionElement : assertionElements) {
            ReadyApiAssertion assertion = parseAssertion(assertionElement);
            request.addAssertion(assertion);
        }
        
        testStep.setRequest(request);
    }
    
    /**
     * Parse a simple step with properties.
     * 
     * @param configElement The config element
     * @param testStep The test step to populate
     */
    private void parseSimplePropertiesStep(Element configElement, ReadyApiTestStep testStep) {
        // Extract all direct child elements as properties
        for (Element element : configElement.elements()) {
            String elemName = element.getName();
            String elemValue = element.getTextTrim();
            if (elemValue != null && !elemValue.isEmpty()) {
                testStep.addProperty(elemName, elemValue);
            }
        }
    }
    
    /**
     * Parse a properties step.
     * 
     * @param propertiesElement The properties element
     * @param testStep The test step to populate
     */
    private void parsePropertiesStep(Element propertiesElement, ReadyApiTestStep testStep) {
        StringBuilder propertiesContent = new StringBuilder();
        for (Element propElement : propertiesElement.elements("property")) {
            String propName = propElement.elementText("name");
            String propValue = propElement.elementText("value");
            if (propName != null && propValue != null) {
                propertiesContent.append(propName).append("=").append(propValue).append("\n");
                testStep.addProperty(propName, propValue);
            }
        }
        testStep.setContent(propertiesContent.toString());
    }
    
    /**
     * Parse a data source step.
     * 
     * @param configElement The config element
     * @param testStep The test step to populate
     */
    private void parseDataSourceStep(Element configElement, ReadyApiTestStep testStep) {
        // Extract all properties first
        parseSimplePropertiesStep(configElement, testStep);
        
        // Handle Excel-specific properties
        Element excelFileElement = configElement.element("file");
        if (excelFileElement != null) {
            String excelFile = excelFileElement.getTextTrim();
            testStep.addProperty("file", excelFile);
            
            Element worksheetElement = configElement.element("worksheet");
            if (worksheetElement != null) {
                testStep.addProperty("worksheet", worksheetElement.getTextTrim());
            }
            
            Element startRowElement = configElement.element("startRow");
            if (startRowElement != null) {
                testStep.addProperty("startRow", startRowElement.getTextTrim());
            }
            
            Element startCellElement = configElement.element("startCell");
            if (startCellElement != null) {
                testStep.addProperty("startCell", startCellElement.getTextTrim());
            }
            
            Element endRowElement = configElement.element("endRow");
            if (endRowElement != null) {
                testStep.addProperty("endRow", endRowElement.getTextTrim());
            }
            
            Element endCellElement = configElement.element("endCell");
            if (endCellElement != null) {
                testStep.addProperty("endCell", endCellElement.getTextTrim());
            }
        }
    }
    
    /**
     * Parse a data sink step.
     * 
     * @param configElement The config element
     * @param testStep The test step to populate
     */
    private void parseDataSinkStep(Element configElement, ReadyApiTestStep testStep) {
        // Extract all simple properties
        parseSimplePropertiesStep(configElement, testStep);
        
        // Extract specific datasink properties
        Element targetStepElement = configElement.element("targetStep");
        if (targetStepElement != null) {
            testStep.addProperty("targetStep", targetStepElement.getTextTrim());
        }
        
        Element formatElement = configElement.element("format");
        if (formatElement != null) {
            testStep.addProperty("format", formatElement.getTextTrim());
        }
        
        Element fileElement = configElement.element("file");
        if (fileElement != null) {
            testStep.addProperty("file", fileElement.getTextTrim());
        }
        
        // Handle Excel specific properties
        Element worksheetElement = configElement.element("worksheet");
        if (worksheetElement != null) {
            testStep.addProperty("worksheet", worksheetElement.getTextTrim());
        }
        
        // Handle column mappings
        parseColumnMappings(configElement, testStep);
    }
    
    /**
     * Parse column mappings for data steps.
     * 
     * @param configElement The config element
     * @param testStep The test step to populate
     */
    private void parseColumnMappings(Element configElement, ReadyApiTestStep testStep) {
        Element columnMappingsElement = configElement.element("columnMappings");
        if (columnMappingsElement != null) {
            StringBuilder mappings = new StringBuilder();
            List<Element> mappingElements = columnMappingsElement.elements("mapping");
            for (Element mappingElement : mappingElements) {
                String columnName = mappingElement.attributeValue("columnName");
                String propertyName = mappingElement.attributeValue("propertyName");
                if (columnName != null && propertyName != null) {
                    mappings.append(columnName).append("=").append(propertyName).append(";");
                    testStep.addProperty("mapping_" + columnName, propertyName);
                }
            }
            testStep.addProperty("columnMappings", mappings.toString());
        }
    }
    
    /**
     * Parse property transfer step.
     * 
     * @param configElement The config element
     * @param testStep The test step to populate
     */
    private void parsePropertyTransferStep(Element configElement, ReadyApiTestStep testStep) {
        Element transfersElement = configElement.element("transfers");
        if (transfersElement != null) {
            for (Element transferElement : transfersElement.elements("transfer")) {
                ReadyApiTestStep.PropertyTransfer transfer = parsePropertyTransfer(transferElement);
                testStep.addPropertyTransfer(transfer);
            }
        }
    }
    
    /**
     * Parse property transfer element.
     * 
     * @param transferElement The transfer element
     * @return The parsed property transfer
     */
    private ReadyApiTestStep.PropertyTransfer parsePropertyTransfer(Element transferElement) {
        ReadyApiTestStep.PropertyTransfer transfer = new ReadyApiTestStep.PropertyTransfer();
        
        // Set name if available
        String transferName = transferElement.attributeValue("name");
        if (transferName != null && !transferName.isEmpty()) {
            transfer.setName(transferName);
        }
        
        // Parse source
        Element sourceElement = transferElement.element("source");
        if (sourceElement != null) {
            parseTransferEndpoint(sourceElement, transfer, true);
        }
        
        // Parse target
        Element targetElement = transferElement.element("target");
        if (targetElement != null) {
            parseTransferEndpoint(targetElement, transfer, false);
        }
        
        return transfer;
    }
    
    /**
     * Parse transfer endpoint (source or target).
     * 
     * @param endpointElement The endpoint element
     * @param transfer The property transfer to update
     * @param isSource Whether this is a source endpoint (true) or target (false)
     */
    private void parseTransferEndpoint(Element endpointElement, ReadyApiTestStep.PropertyTransfer transfer, boolean isSource) {
        // Get step name
        String stepName = endpointElement.attributeValue("stepName");
        if (stepName == null || stepName.isEmpty()) {
            stepName = endpointElement.attributeValue("step");
        }
        
        if (stepName != null && !stepName.isEmpty()) {
            if (isSource) {
                transfer.setSourceName(stepName);
            } else {
                transfer.setTargetName(stepName);
            }
        }
        
        // Get property path and language
        String type = endpointElement.attributeValue("type");
        String path = null;
        String pathLanguage = null;
        
        // Try different elements based on type
        if ("XPATH".equalsIgnoreCase(type)) {
            path = endpointElement.elementText("path");
            pathLanguage = "xpath";
        } else if ("JSONPATH".equalsIgnoreCase(type)) {
            path = endpointElement.elementText("path");
            pathLanguage = "jsonpath";
        } else if ("PROPERTY".equalsIgnoreCase(type)) {
            path = endpointElement.elementText("property");
            pathLanguage = "property";
        } else {
            // Try to guess based on available elements
            if (endpointElement.element("xpathExpression") != null) {
                path = endpointElement.elementText("xpathExpression");
                pathLanguage = "xpath";
            } else if (endpointElement.element("jsonPath") != null) {
                path = endpointElement.elementText("jsonPath");
                pathLanguage = "jsonpath";
            } else if (endpointElement.element("property") != null) {
                path = endpointElement.elementText("property");
                pathLanguage = "property";
            }
        }
        
        if (path != null && !path.isEmpty()) {
            if (isSource) {
                transfer.setSourcePath(path);
                transfer.setSourcePathLanguage(pathLanguage);
            } else {
                transfer.setTargetPath(path);
                transfer.setTargetPathLanguage(pathLanguage);
            }
        }
    }
    
    /**
     * Parse request settings.
     * 
     * @param settingsElement The settings element
     * @param request The request to populate
     */
    private void parseRequestSettings(Element settingsElement, ReadyApiRequest request) {
        List<Element> settingElements = settingsElement.elements("setting");
        for (Element settingElement : settingElements) {
            String settingId = settingElement.attributeValue("id");
            
            if ("request-headers".equals(settingId)) {
                Element headersElement = settingElement.element("headers");
                if (headersElement != null) {
                    for (Element headerElement : headersElement.elements("header")) {
                        String headerName = headerElement.attributeValue("name");
                        String headerValue = headerElement.attributeValue("value");
                        if (headerName != null && headerValue != null) {
                            request.addRequestHeader(headerName, headerValue);
                        }
                    }
                }
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
        ReadyApiAssertion assertion = new ReadyApiAssertion();
        assertion.setId(assertionElement.attributeValue("id"));
        assertion.setName(assertionElement.attributeValue("name"));
        assertion.setType(assertionElement.attributeValue("type"));
        
        // Parse assertion configuration
        Element configElement = assertionElement.element("configuration");
        if (configElement != null) {
            List<Element> configChildElements = configElement.elements();
            for (Element configChild : configChildElements) {
                assertion.addConfigurationProperty(configChild.getName(), configChild.getTextTrim());
            }
        }
        
        return assertion;
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
     * Get the root element of the parsed project.
     * 
     * @return The root element of the parsed project
     */
    public Element getRootElement() {
        return rootElement;
    }
} 
