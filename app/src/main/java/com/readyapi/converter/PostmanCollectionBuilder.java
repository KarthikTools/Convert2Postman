package com.readyapi.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.NullNode;

/**
 * Builder for creating Postman collections from ReadyAPI projects.
 */
public class PostmanCollectionBuilder {
    private static final Logger logger = LoggerFactory.getLogger(PostmanCollectionBuilder.class);
    
    private final ReadyApiProject project;
    private final List<String> conversionIssues = new ArrayList<>();
    private final ObjectMapper objectMapper;
    
    public PostmanCollectionBuilder(ReadyApiProject project) {
        this.project = project;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Build a Postman collection from the ReadyAPI project.
     * 
     * @return A PostmanCollection
     */
    public PostmanCollection build() {
        logger.info("Building Postman collection from ReadyAPI project: {}", project.getName());
        
        PostmanCollection collection = new PostmanCollection();
        PostmanCollection.PostmanInfo info = new PostmanCollection.PostmanInfo();
        info.setName(project.getName());
        collection.setInfo(info);
        
        // Create main folder structure
        PostmanItem interfacesFolder = new PostmanItem();
        interfacesFolder.setName("Interfaces");
        
        PostmanItem testSuitesFolder = new PostmanItem();
        testSuitesFolder.setName("Test Suites");
        
        // Add interfaces
        addInterfaces(interfacesFolder);
        
        // Add test suites
        addTestSuites(testSuitesFolder);
        
        // Add variables
        addVariables(collection);
        
        // Add main folders to collection
        if (interfacesFolder.getItem() != null && !interfacesFolder.getItem().isEmpty()) {
            collection.addItem(interfacesFolder);
        }
        
        if (testSuitesFolder.getItem() != null && !testSuitesFolder.getItem().isEmpty()) {
            collection.addItem(testSuitesFolder);
        }
        
        logger.info("Built Postman collection with {} interfaces and {} test suites", 
                project.getInterfaces().size(), 
                project.getTestSuites().size());
        
        return collection;
    }
    
    /**
     * Add interfaces to the Postman collection.
     * 
     * @param interfacesFolder The interfaces folder item
     */
    private void addInterfaces(PostmanItem interfacesFolder) {
        for (ReadyApiInterface apiInterface : project.getInterfaces()) {
            PostmanItem interfaceFolder = new PostmanItem();
            interfaceFolder.setName(apiInterface.getName());
            
            for (ReadyApiResource resource : apiInterface.getResources()) {
                for (ReadyApiMethod method : resource.getMethods()) {
                    for (ReadyApiRequest request : method.getRequests()) {
                        PostmanItem requestItem = new PostmanItem();
                        requestItem.setName(request.getName());
                        
                        // Create Postman request
                        PostmanRequest postmanRequest = new PostmanRequest();
                        
                        // Determine method
                        String httpMethod = method.getHttpMethod();
                        if (httpMethod != null && !httpMethod.isEmpty()) {
                            postmanRequest.setMethod(httpMethod.toUpperCase());
                        } else if (request.getEndpoint() != null && request.getEndpoint().contains("service")) {
                            // Typically a POST endpoint
                            postmanRequest.setMethod("POST");
                        } else {
                            // Default to GET
                            postmanRequest.setMethod("GET");
                        }
                        
                        // Set URL
                        String endpoint = request.getEndpoint();
                        
                        postmanRequest.setUrl(PostmanRequest.PostmanUrl.parse(endpoint));
                        
                        // Set headers
                        for (Map.Entry<String, String> header : request.getRequestHeaders().entrySet()) {
                            postmanRequest.addHeader(header.getKey(), header.getValue());
                        }
                        
                        // Set body
                        if (request.getRequestBody() != null && !request.getRequestBody().isEmpty()) {
                            PostmanRequest.PostmanBody body = new PostmanRequest.PostmanBody();
                            body.setMode("raw");
                            body.setRaw(request.getRequestBody());
                            
                            // Set body options based on media type
                            PostmanRequest.PostmanBody.PostmanBodyOptions options = 
                                    new PostmanRequest.PostmanBody.PostmanBodyOptions();
                            
                            PostmanRequest.PostmanBody.PostmanBodyOptions.PostmanRawOptions rawOptions = 
                                    new PostmanRequest.PostmanBody.PostmanBodyOptions.PostmanRawOptions();
                            
                            if (request.getMediaType() != null) {
                                if (request.getMediaType().contains("json")) {
                                    rawOptions.setLanguage("json");
                                } else if (request.getMediaType().contains("xml")) {
                                    rawOptions.setLanguage("xml");
                                } else {
                                    rawOptions.setLanguage("text");
                                }
                            } else {
                                rawOptions.setLanguage("text");
                            }
                            
                            options.setRaw(rawOptions);
                            body.setOptions(options);
                            
                            postmanRequest.setBody(body);
                        }
                        
                        // Add assertions as test scripts
                        if (!request.getAssertions().isEmpty()) {
                            StringBuilder testScript = new StringBuilder();
                            
                            // Add each assertion
                            for (ReadyApiAssertion assertion : request.getAssertions()) {
                                String assertionScript = assertion.toPostmanTest();
                                testScript.append(assertionScript).append("\n");
                            }
                            
                            PostmanEvent testEvent = PostmanEvent.createTest(testScript.toString());
                            requestItem.addEvent(testEvent);
                        }
                        
                        requestItem.setRequest(postmanRequest);
                        interfaceFolder.addItem(requestItem);
                    }
                }
            }
            
            if (interfaceFolder.getItem() != null && !interfaceFolder.getItem().isEmpty()) {
                interfacesFolder.addItem(interfaceFolder);
            }
        }
    }
    
    /**
     * Add test suites to the Postman collection.
     * 
     * @param testSuitesFolder The test suites folder item
     */
    private void addTestSuites(PostmanItem testSuitesFolder) {
        Map<String, String> scriptLibraryMap = new HashMap<>();
        
        // First, convert script libraries to JavaScript
        for (ReadyApiScriptLibrary scriptLibrary : project.getScriptLibraries()) {
            String jsLibrary = scriptLibrary.convertToJavaScript();
            scriptLibraryMap.put(scriptLibrary.getName(), jsLibrary);
        }
        
        // Convert test suites
        for (ReadyApiTestSuite testSuite : project.getTestSuites()) {
            PostmanItem testSuiteFolder = new PostmanItem();
            testSuiteFolder.setName(testSuite.getName());
            
            for (ReadyApiTestCase testCase : testSuite.getTestCases()) {
                PostmanItem testCaseFolder = new PostmanItem();
                testCaseFolder.setName(testCase.getName());
                
                // Add setup script with library imports
                StringBuilder setupScript = new StringBuilder();
                setupScript.append("// Import script libraries\n");
                
                for (Map.Entry<String, String> libraryEntry : scriptLibraryMap.entrySet()) {
                    setupScript.append("// Include ").append(libraryEntry.getKey()).append("\n");
                    setupScript.append("let ").append(libraryEntry.getKey()).append(" = pm.collectionVariables.get(\"")
                            .append(libraryEntry.getKey()).append("\");\n");
                    setupScript.append("if (").append(libraryEntry.getKey()).append(" !== null) {\n");
                    setupScript.append("    ").append(libraryEntry.getKey()).append(" = JSON.parse(")
                            .append(libraryEntry.getKey()).append(");\n");
                    setupScript.append("}\n\n");
                }
                
                // Process test steps by categorizing them
                Map<String, List<ReadyApiTestStep>> testStepsByType = categorizeTestSteps(testCase.getTestSteps());
                
                // Handle data sources first (to register data)
                List<ReadyApiTestStep> dataSourceSteps = testStepsByType.getOrDefault("datasource", new ArrayList<>());
                StringBuilder dataSourceScript = new StringBuilder();
                
                for (ReadyApiTestStep dataSourceStep : dataSourceSteps) {
                    String dataSourceName = dataSourceStep.getName();
                    dataSourceScript.append("// Data source: ").append(dataSourceName).append("\n");
                    dataSourceScript.append("// Note: Make sure to import the corresponding data file in Postman\n");
                    dataSourceScript.append("// and set it as a collection or environment variable\n\n");
                }
                
                // Include pre-request scripts
                List<ReadyApiTestStep> preRequestScriptSteps = testStepsByType.getOrDefault("prerequest", new ArrayList<>());
                List<ReadyApiTestStep> propertiesSteps = testStepsByType.getOrDefault("properties", new ArrayList<>());
                
                // Add properties first as they often define variables needed by scripts
                for (ReadyApiTestStep propertiesStep : propertiesSteps) {
                    setupScript.append("// From properties step: ").append(propertiesStep.getName()).append("\n");
                    setupScript.append(propertiesStep.convertPropertiesToJavaScript()).append("\n\n");
                }
                
                // Add pre-request scripts
                for (ReadyApiTestStep scriptStep : preRequestScriptSteps) {
                    setupScript.append("// From test step: ").append(scriptStep.getName()).append("\n");
                    setupScript.append(scriptStep.convertGroovyToJavaScript()).append("\n\n");
                }
                
                // Add property transfers that happen before requests
                List<ReadyApiTestStep> propertyTransferSteps = testStepsByType.getOrDefault("propertytransfer", new ArrayList<>());
                for (ReadyApiTestStep transferStep : propertyTransferSteps) {
                    // Only include transfers intended for pre-request
                    if (isPreRequestTransfer(transferStep)) {
                        setupScript.append("// From property transfer step: ").append(transferStep.getName()).append("\n");
                        setupScript.append(transferStep.convertPropertyTransfersToJavaScript()).append("\n\n");
                    }
                }
                
                // Add data source loop setup if present
                List<ReadyApiTestStep> dataSourceLoopSteps = testStepsByType.getOrDefault("datasourceloop", new ArrayList<>());
                if (!dataSourceLoopSteps.isEmpty()) {
                    for (ReadyApiTestStep loopStep : dataSourceLoopSteps) {
                        setupScript.append("// From data source loop step: ").append(loopStep.getName()).append("\n");
                        setupScript.append(loopStep.convertDataSourceLoopToJavaScript()).append("\n\n");
                    }
                }
                
                // Process test request steps
                List<ReadyApiTestStep> restRequestSteps = testStepsByType.getOrDefault("restrequest", new ArrayList<>());
                
                // Add test scripts
                List<ReadyApiTestStep> testScriptSteps = testStepsByType.getOrDefault("test", new ArrayList<>());
                StringBuilder testScript = new StringBuilder();
                
                // Add test scripts
                for (ReadyApiTestStep scriptStep : testScriptSteps) {
                    testScript.append("// From test step: ").append(scriptStep.getName()).append("\n");
                    testScript.append(scriptStep.convertGroovyToJavaScript()).append("\n\n");
                }
                
                // Add property transfers that happen after responses
                for (ReadyApiTestStep transferStep : propertyTransferSteps) {
                    // Only include transfers intended for post-response
                    if (!isPreRequestTransfer(transferStep)) {
                        testScript.append("// From property transfer step: ").append(transferStep.getName()).append("\n");
                        testScript.append(transferStep.convertPropertyTransfersToJavaScript()).append("\n\n");
                    }
                }
                
                // Add datasink steps (typically executed after receiving a response)
                List<ReadyApiTestStep> dataSinkSteps = testStepsByType.getOrDefault("datasink", new ArrayList<>());
                for (ReadyApiTestStep dataSinkStep : dataSinkSteps) {
                    testScript.append("// From data sink step: ").append(dataSinkStep.getName()).append("\n");
                    testScript.append(dataSinkStep.convertDataSinkToJavaScript()).append("\n\n");
                }
                
                // If we have no REST requests, add a dummy one to hold scripts
                if (restRequestSteps.isEmpty()) {
                    conversionIssues.add("No REST requests found in test case: " + testCase.getName());
                    
                    // Create a dummy request to hold the test scripts
                    PostmanItem dummyRequestItem = new PostmanItem();
                    dummyRequestItem.setName(testCase.getName() + "_Scripts");
                    
                    PostmanRequest dummyRequest = new PostmanRequest();
                    dummyRequest.setMethod("GET");
                    
                    // Create a placeholder URL (can be updated by user)
                    String baseUrl = project.getProperty("ProjectBaseURL");
                    if (baseUrl == null || baseUrl.isEmpty()) {
                        baseUrl = "https://example.com";
                    }
                    
                    dummyRequest.setUrl(PostmanRequest.PostmanUrl.parse(baseUrl));
                    
                    // Add pre-request event
                    if (setupScript.length() > 0) {
                        PostmanEvent preRequestEvent = PostmanEvent.createPreRequestScript(setupScript.toString());
                        dummyRequestItem.addEvent(preRequestEvent);
                    }
                    
                    // Add test event if we have test scripts
                    if (testScript.length() > 0) {
                        PostmanEvent testEvent = PostmanEvent.createTest(testScript.toString());
                        dummyRequestItem.addEvent(testEvent);
                    }
                    
                    dummyRequestItem.setRequest(dummyRequest);
                    testCaseFolder.addItem(dummyRequestItem);
                } else {
                    // Add REST request steps
                    for (ReadyApiTestStep restStep : restRequestSteps) {
                        if (restStep.getRequest() == null) {
                            conversionIssues.add("REST request step without request: " + restStep.getName() + 
                                    " in test case: " + testCase.getName());
                            continue;
                        }
                        
                        PostmanItem requestItem = new PostmanItem();
                        requestItem.setName(restStep.getName());
                        
                        // Create Postman request
                        ReadyApiRequest readyRequest = restStep.getRequest();
                        
                        PostmanRequest postmanRequest = new PostmanRequest();
                        
                        // Determine method
                        if (readyRequest.getEndpoint() != null && readyRequest.getEndpoint().contains("service")) {
                            // Typically a POST endpoint
                            postmanRequest.setMethod("POST");
                        } else {
                            // Default to GET
                            postmanRequest.setMethod("GET");
                        }
                        
                        // Set URL
                        String endpoint = readyRequest.getEndpoint();
                        
                        postmanRequest.setUrl(PostmanRequest.PostmanUrl.parse(endpoint));
                        
                        // Set headers
                        for (Map.Entry<String, String> header : readyRequest.getRequestHeaders().entrySet()) {
                            postmanRequest.addHeader(header.getKey(), header.getValue());
                        }
                        
                        // Set body
                        if (readyRequest.getRequestBody() != null && !readyRequest.getRequestBody().isEmpty()) {
                            PostmanRequest.PostmanBody body = new PostmanRequest.PostmanBody();
                            body.setMode("raw");
                            body.setRaw(readyRequest.getRequestBody());
                            
                            // Set body options based on media type
                            PostmanRequest.PostmanBody.PostmanBodyOptions options = 
                                    new PostmanRequest.PostmanBody.PostmanBodyOptions();
                            
                            PostmanRequest.PostmanBody.PostmanBodyOptions.PostmanRawOptions rawOptions = 
                                    new PostmanRequest.PostmanBody.PostmanBodyOptions.PostmanRawOptions();
                            
                            if (readyRequest.getMediaType() != null) {
                                if (readyRequest.getMediaType().contains("json")) {
                                    rawOptions.setLanguage("json");
                                } else if (readyRequest.getMediaType().contains("xml")) {
                                    rawOptions.setLanguage("xml");
                                } else {
                                    rawOptions.setLanguage("text");
                                }
                            } else {
                                rawOptions.setLanguage("text");
                            }
                            
                            options.setRaw(rawOptions);
                            body.setOptions(options);
                            
                            postmanRequest.setBody(body);
                        }
                        
                        // Add pre-request script
                        PostmanEvent preRequestEvent = PostmanEvent.createPreRequestScript(setupScript.toString());
                        requestItem.addEvent(preRequestEvent);
                        
                        // Create combined test script with assertions and test scripts
                        StringBuilder combinedTestScript = new StringBuilder();
                        
                        // Add basic status check
                        combinedTestScript.append("pm.test(\"Status code is 200\", function() {\n");
                        combinedTestScript.append("    pm.response.to.have.status(200);\n");
                        combinedTestScript.append("});\n\n");
                        
                        // Add assertions from the request
                        for (ReadyApiAssertion assertion : readyRequest.getAssertions()) {
                            String assertionScript = assertion.toPostmanTest();
                            combinedTestScript.append(assertionScript).append("\n");
                        }
                        
                        // Add the rest of the test scripts
                        combinedTestScript.append(testScript);
                        
                        // Add test event
                        PostmanEvent testEvent = PostmanEvent.createTest(combinedTestScript.toString());
                        requestItem.addEvent(testEvent);
                        
                        requestItem.setRequest(postmanRequest);
                        testCaseFolder.addItem(requestItem);
                    }
                }
                
                // Log unsupported step types
                for (Map.Entry<String, List<ReadyApiTestStep>> entry : testStepsByType.entrySet()) {
                    if (!entry.getKey().equals("prerequest") && 
                        !entry.getKey().equals("test") && 
                        !entry.getKey().equals("restrequest") && 
                        !entry.getKey().equals("properties") && 
                        !entry.getKey().equals("propertytransfer") && 
                        !entry.getKey().equals("datasource") && 
                        !entry.getKey().equals("datasink") && 
                        !entry.getKey().equals("datasourceloop")) {
                        
                        for (ReadyApiTestStep step : entry.getValue()) {
                            conversionIssues.add("Unsupported test step type: " + step.getType() + 
                                    " for step: " + step.getName() + " in test case: " + testCase.getName());
                        }
                    }
                }
                
                if (testCaseFolder.getItem() != null && !testCaseFolder.getItem().isEmpty()) {
                    testSuiteFolder.addItem(testCaseFolder);
                }
            }
            
            if (testSuiteFolder.getItem() != null && !testSuiteFolder.getItem().isEmpty()) {
                testSuitesFolder.addItem(testSuiteFolder);
            }
        }
    }
    
    /**
     * Categorize test steps by their type or purpose.
     * 
     * @param testSteps List of test steps to categorize
     * @return Map of categorized test steps
     */
    private Map<String, List<ReadyApiTestStep>> categorizeTestSteps(List<ReadyApiTestStep> testSteps) {
        Map<String, List<ReadyApiTestStep>> categorizedSteps = new HashMap<>();
        
        for (ReadyApiTestStep step : testSteps) {
            String category;
            
            if ("groovy".equalsIgnoreCase(step.getType())) {
                if (step.isPreRequestScript()) {
                    category = "prerequest";
                } else if (step.isTestScript()) {
                    category = "test";
                } else {
                    category = "groovy";
                }
            } else {
                category = step.getType().toLowerCase();
            }
            
            categorizedSteps.computeIfAbsent(category, k -> new ArrayList<>()).add(step);
        }
        
        return categorizedSteps;
    }
    
    /**
     * Determine if a property transfer step should be included in pre-request script.
     * 
     * @param step Property transfer step
     * @return True if the step should be included in pre-request script
     */
    private boolean isPreRequestTransfer(ReadyApiTestStep step) {
        if (!"propertytransfer".equalsIgnoreCase(step.getType())) {
            return false;
        }
        
        // Check if the target of the transfer is used in the request
        // If the transfer is used to set up a request parameter, include it in pre-request
        for (ReadyApiTestStep.PropertyTransfer transfer : step.getPropertyTransfers()) {
            String targetName = transfer.getTargetName();
            if (targetName != null && 
                (targetName.toLowerCase().contains("request") || 
                 targetName.toLowerCase().contains("header") ||
                 targetName.toLowerCase().contains("endpoint"))) {
                return true;
            }
        }
        
        // By default, include in post-request scripts
        return false;
    }
    
    /**
     * Add variables to the Postman collection.
     * 
     * @param collection The Postman collection
     */
    private void addVariables(PostmanCollection collection) {
        // Add project properties as variables
        for (Map.Entry<String, String> entry : project.getProperties().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            collection.addVariable(new PostmanVariable(key, value, "string", false));
        }
        
        // Add script libraries as variables
        for (ReadyApiScriptLibrary library : project.getScriptLibraries()) {
            String name = library.getName();
            String content = library.getContent();
            
            try {
                String jsLibrary = FunctionLibraryConverter.convertLibraryToPostmanVariable(name, content);
                collection.addVariable(new PostmanVariable(name, jsLibrary, "string", false));
            } catch (Exception e) {
                logger.error("Error converting script library to Postman variable: {}", name, e);
                conversionIssues.add("Failed to convert script library: " + name + " - " + e.getMessage());
            }
        }
    }
    
    /**
     * Get the list of conversion issues.
     * 
     * @return List of conversion issues
     */
    public List<String> getConversionIssues() {
        return conversionIssues;
    }
} 