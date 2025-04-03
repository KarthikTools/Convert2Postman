package com.readyapi.converter;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Parser for ReadyAPI project XML files.
 */
public class ReadyApiProjectParser {
    private static final Logger logger = LoggerFactory.getLogger(ReadyApiProjectParser.class);
    
    static {
        // Initialize logging without XML configuration
        System.setProperty("logback.configurationFile", "");
    }
    
    /**
     * Parse a ReadyAPI project XML file.
     * 
     * @param filePath Path to the ReadyAPI project XML file
     * @return A ReadyApiProject object with parsed project data
     * @throws DocumentException If there's an error parsing the XML
     */
    public ReadyApiProject parse(String filePath) throws DocumentException {
        logger.info("Parsing ReadyAPI project file: {}", filePath);
        
        SAXReader reader = new SAXReader();
        // Configure namespace handling
        Map<String, String> nsMap = new HashMap<>();
        nsMap.put("con", "http://eviware.com/soapui/config");
        reader.getDocumentFactory().setXPathNamespaceURIs(nsMap);
        
        try {
            Document document = reader.read(new File(filePath));
            Element rootElement = document.getRootElement();
            
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
        }
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
        
        // Parse test suites
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
     * Parse test suites.
     * 
     * @param rootElement The XML root element
     * @param project The project to populate
     */
    private void parseTestSuites(Element rootElement, ReadyApiProject project) {
        List<Element> testSuiteElements = rootElement.elements("testSuite");
        for (Element testSuiteElement : testSuiteElements) {
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
                    ReadyApiTestStep testStep = new ReadyApiTestStep();
                    testStep.setId(testStepElement.attributeValue("id"));
                    testStep.setName(testStepElement.attributeValue("name"));
                    testStep.setType(testStepElement.attributeValue("type"));
                    
                    // Parse test step configuration
                    Element configElement = testStepElement.element("config");
                    if (configElement != null) {
                        if ("groovy".equalsIgnoreCase(testStep.getType())) {
                            // Parse Groovy script
                            Element scriptElement = configElement.element("script");
                            if (scriptElement != null) {
                                testStep.setContent(scriptElement.getTextTrim());
                            }
                        } else if ("restrequest".equalsIgnoreCase(testStep.getType())) {
                            // Parse REST request
                            Element restRequestElement = configElement.element("restRequest");
                            if (restRequestElement != null) {
                                ReadyApiRequest request = new ReadyApiRequest();
                                request.setId(restRequestElement.attributeValue("id"));
                                request.setName(restRequestElement.attributeValue("name"));
                                request.setMediaType(restRequestElement.attributeValue("mediaType"));
                                
                                // Parse request settings (headers, etc.)
                                Element settingsElement = restRequestElement.element("settings");
                                if (settingsElement != null) {
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
                                
                                // Set endpoint
                                Element endpointElement = restRequestElement.element("endpoint");
                                if (endpointElement != null) {
                                    request.setEndpoint(endpointElement.getTextTrim());
                                }
                                
                                // Set request body
                                Element requestBodyElement = restRequestElement.element("request");
                                if (requestBodyElement != null) {
                                    request.setRequestBody(requestBodyElement.getTextTrim());
                                }
                                
                                // Parse assertions
                                List<Element> assertionElements = restRequestElement.elements("assertion");
                                for (Element assertionElement : assertionElements) {
                                    ReadyApiAssertion assertion = new ReadyApiAssertion();
                                    assertion.setId(assertionElement.attributeValue("id"));
                                    assertion.setName(assertionElement.attributeValue("name"));
                                    assertion.setType(assertionElement.attributeValue("type"));
                                    
                                    // Parse assertion configuration
                                    Element assertionConfigElement = assertionElement.element("configuration");
                                    if (assertionConfigElement != null) {
                                        List<Element> configChildElements = assertionConfigElement.elements();
                                        for (Element configChild : configChildElements) {
                                            assertion.addConfigurationProperty(configChild.getName(), configChild.getTextTrim());
                                        }
                                    }
                                    
                                    request.addAssertion(assertion);
                                }
                                
                                testStep.setRequest(request);
                            }
                        } else if ("properties".equalsIgnoreCase(testStep.getType())) {
                            // Parse properties test step
                            Element propertiesStepElement = configElement.element("properties");
                            if (propertiesStepElement != null) {
                                StringBuilder propertiesContent = new StringBuilder();
                                for (Element propElement : propertiesStepElement.elements("property")) {
                                    String propName = propElement.elementText("name");
                                    String propValue = propElement.elementText("value");
                                    if (propName != null && propValue != null) {
                                        propertiesContent.append(propName).append("=").append(propValue).append("\n");
                                        testStep.addProperty(propName, propValue);
                                    }
                                }
                                testStep.setContent(propertiesContent.toString());
                            }
                        } else if ("datasource".equalsIgnoreCase(testStep.getType())) {
                            // Parse data source test step
                            for (Element element : configElement.elements()) {
                                String elemName = element.getName();
                                String elemValue = element.getTextTrim();
                                if (elemValue != null && !elemValue.isEmpty()) {
                                    testStep.addProperty(elemName, elemValue);
                                }
                            }
                            
                            // Special handling for Excel data sources
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
                        } else if ("datasourceloop".equalsIgnoreCase(testStep.getType())) {
                            // Parse data source loop test step
                            for (Element element : configElement.elements()) {
                                String elemName = element.getName();
                                String elemValue = element.getTextTrim();
                                if (elemValue != null && !elemValue.isEmpty()) {
                                    testStep.addProperty(elemName, elemValue);
                                }
                            }
                        } else if ("propertytransfer".equalsIgnoreCase(testStep.getType())) {
                            // Parse property transfer test step
                            Element transfersElement = configElement.element("transfers");
                            if (transfersElement != null) {
                                for (Element transferElement : transfersElement.elements("transfer")) {
                                    ReadyApiTestStep.PropertyTransfer transfer = new ReadyApiTestStep.PropertyTransfer();
                                    
                                    // Set name if available
                                    String transferName = transferElement.attributeValue("name");
                                    if (transferName != null && !transferName.isEmpty()) {
                                        transfer.setName(transferName);
                                    }
                                    
                                    // Source settings
                                    Element sourceElement = transferElement.element("source");
                                    if (sourceElement != null) {
                                        String sourceName = sourceElement.attributeValue("stepName");
                                        if (sourceName == null || sourceName.isEmpty()) {
                                            sourceName = sourceElement.attributeValue("step");
                                        }
                                        
                                        if (sourceName != null && !sourceName.isEmpty()) {
                                            transfer.setSourceName(sourceName);
                                        }
                                        
                                        // Source property path handling
                                        String sourceType = sourceElement.attributeValue("type");
                                        String sourcePath = null;
                                        String sourcePathLanguage = null;
                                        
                                        if ("XPATH".equalsIgnoreCase(sourceType)) {
                                            sourcePath = sourceElement.elementText("path");
                                            sourcePathLanguage = "xpath";
                                        } else if ("JSONPATH".equalsIgnoreCase(sourceType)) {
                                            sourcePath = sourceElement.elementText("path");
                                            sourcePathLanguage = "jsonpath";
                                        } else if ("PROPERTY".equalsIgnoreCase(sourceType)) {
                                            sourcePath = sourceElement.elementText("property");
                                            sourcePathLanguage = "property";
                                        } else {
                                            // Try to guess based on available elements
                                            if (sourceElement.element("xpathExpression") != null) {
                                                sourcePath = sourceElement.elementText("xpathExpression");
                                                sourcePathLanguage = "xpath";
                                            } else if (sourceElement.element("jsonPath") != null) {
                                                sourcePath = sourceElement.elementText("jsonPath");
                                                sourcePathLanguage = "jsonpath";
                                            } else if (sourceElement.element("property") != null) {
                                                sourcePath = sourceElement.elementText("property");
                                                sourcePathLanguage = "property";
                                            }
                                        }
                                        
                                        if (sourcePath != null && !sourcePath.isEmpty()) {
                                            transfer.setSourcePath(sourcePath);
                                            transfer.setSourcePathLanguage(sourcePathLanguage);
                                        }
                                    }
                                    
                                    // Target settings
                                    Element targetElement = transferElement.element("target");
                                    if (targetElement != null) {
                                        String targetName = targetElement.attributeValue("stepName");
                                        if (targetName == null || targetName.isEmpty()) {
                                            targetName = targetElement.attributeValue("step");
                                        }
                                        
                                        if (targetName != null && !targetName.isEmpty()) {
                                            transfer.setTargetName(targetName);
                                        }
                                        
                                        // Target property path handling
                                        String targetType = targetElement.attributeValue("type");
                                        String targetPath = null;
                                        String targetPathLanguage = null;
                                        
                                        if ("XPATH".equalsIgnoreCase(targetType)) {
                                            targetPath = targetElement.elementText("path");
                                            targetPathLanguage = "xpath";
                                        } else if ("JSONPATH".equalsIgnoreCase(targetType)) {
                                            targetPath = targetElement.elementText("path");
                                            targetPathLanguage = "jsonpath";
                                        } else if ("PROPERTY".equalsIgnoreCase(targetType)) {
                                            targetPath = targetElement.elementText("property");
                                            targetPathLanguage = "property";
                                        } else {
                                            // Try to guess based on available elements
                                            if (targetElement.element("xpathExpression") != null) {
                                                targetPath = targetElement.elementText("xpathExpression");
                                                targetPathLanguage = "xpath";
                                            } else if (targetElement.element("jsonPath") != null) {
                                                targetPath = targetElement.elementText("jsonPath");
                                                targetPathLanguage = "jsonpath";
                                            } else if (targetElement.element("property") != null) {
                                                targetPath = targetElement.elementText("property");
                                                targetPathLanguage = "property";
                                            }
                                        }
                                        
                                        if (targetPath != null && !targetPath.isEmpty()) {
                                            transfer.setTargetPath(targetPath);
                                            transfer.setTargetPathLanguage(targetPathLanguage);
                                        }
                                    }
                                    
                                    testStep.addPropertyTransfer(transfer);
                                }
                            }
                        } else if ("datasink".equalsIgnoreCase(testStep.getType())) {
                            // Parse datasink test step
                            for (Element element : configElement.elements()) {
                                String elemName = element.getName();
                                String elemValue = element.getTextTrim();
                                if (elemValue != null && !elemValue.isEmpty()) {
                                    testStep.addProperty(elemName, elemValue);
                                }
                            }
                            
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
                    }
                    
                    testCase.addTestStep(testStep);
                }
                
                testSuite.addTestCase(testCase);
            }
            
            project.addTestSuite(testSuite);
        }
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
} 