package com.readyapi.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.URL;
import java.net.MalformedURLException;

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
        
        // Add description with composite project information if applicable
        if (!project.getReferencedProjects().isEmpty()) {
            StringBuilder description = new StringBuilder("Composite project including:");
            description.append("\n- ").append(project.getName()).append(" (Main)");
            
            for (ReadyApiProject referencedProject : project.getReferencedProjects()) {
                description.append("\n- ").append(referencedProject.getName());
            }
            
            info.setDescription(description.toString());
        }
        
        collection.setInfo(info);
        
        // Create main folder structure
        PostmanItem interfacesFolder = new PostmanItem();
        interfacesFolder.setName("Interfaces");
        
        PostmanItem testSuitesFolder = new PostmanItem();
        testSuitesFolder.setName("Test Suites");
        
        // Add interfaces - use getAllInterfaces to include referenced projects
        addInterfaces(interfacesFolder, project.getAllInterfaces());
        
        // Add test suites - use getAllTestSuites to include referenced projects
        addTestSuites(testSuitesFolder, project.getAllTestSuites(), project.getAllScriptLibraries());
        
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
                project.getAllInterfaces().size(), 
                project.getAllTestSuites().size());
        
        return collection;
    }
    
    /**
     * Add interfaces to the Postman collection.
     * 
     * @param interfacesFolder The interfaces folder item
     * @param apiInterfaces List of interfaces to add
     */
    private void addInterfaces(PostmanItem interfacesFolder, List<ReadyApiInterface> apiInterfaces) {
        for (ReadyApiInterface apiInterface : apiInterfaces) {
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
     * @param testSuites List of test suites to add
     * @param scriptLibraries List of script libraries to use
     */
    private void addTestSuites(PostmanItem testSuitesFolder, List<ReadyApiTestSuite> testSuites, 
                               List<ReadyApiScriptLibrary> scriptLibraries) {
        Map<String, String> scriptLibraryMap = new HashMap<>();
        
        // First, convert script libraries to JavaScript
        for (ReadyApiScriptLibrary scriptLibrary : scriptLibraries) {
            String jsLibrary = scriptLibrary.convertToJavaScript();
            scriptLibraryMap.put(scriptLibrary.getName(), jsLibrary);
        }
        
        // Convert test suites
        for (ReadyApiTestSuite testSuite : testSuites) {
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

    private PostmanItem createItemFromTestStep(ReadyApiTestStep testStep) {
        PostmanItem item = new PostmanItem();
        item.setName(testStep.getName());
        
        if (testStep.getRequest() != null) {
            // Create Postman request from ReadyAPI request
            PostmanRequest postmanRequest = new PostmanRequest();
            postmanRequest.setMethod(testStep.getRequest().getMethod());
            
            // Setup URL
            PostmanRequest.PostmanUrl url = new PostmanRequest.PostmanUrl();
            String endpoint = testStep.getRequest().getEndpoint();
            
            // Convert any ReadyAPI variables in the endpoint to Postman variables
            endpoint = convertReadyApiVariablesToPostman(endpoint);
            
            url.setRaw(endpoint);
            
            // Parse URL and extract host, path, query params
            parseUrl(endpoint, url);
            
            // Setup headers
            List<PostmanRequest.PostmanHeader> headers = new ArrayList<>();
            for (Map.Entry<String, String> header : testStep.getRequest().getHeaders().entrySet()) {
                PostmanRequest.PostmanHeader postmanHeader = new PostmanRequest.PostmanHeader();
                postmanHeader.setKey(header.getKey());
                postmanHeader.setValue(convertReadyApiVariablesToPostman(header.getValue()));
                headers.add(postmanHeader);
            }
            postmanRequest.setHeader(headers);
            
            // Setup request body
            if (testStep.getRequest().getBody() != null && !testStep.getRequest().getBody().isEmpty()) {
                PostmanRequest.PostmanBody body = new PostmanRequest.PostmanBody();
                PostmanRequest.PostmanBody.PostmanBodyOptions options = new PostmanRequest.PostmanBody.PostmanBodyOptions();
                
                String contentType = testStep.getRequest().getContentType();
                if (contentType == null || contentType.isEmpty() || 
                    contentType.contains("json") || 
                    contentType.contains("text") || 
                    contentType.contains("xml")) {
                    
                    body.setMode("raw");
                    String rawBody = testStep.getRequest().getBody();
                    
                    // Convert ReadyAPI variables in the body to Postman variables
                    rawBody = convertReadyApiVariablesToPostman(rawBody);
                    
                    body.setRaw(rawBody);
                    
                    PostmanRequest.PostmanBody.PostmanBodyOptions.PostmanRawOptions rawOptions = 
                        new PostmanRequest.PostmanBody.PostmanBodyOptions.PostmanRawOptions();
                    
                    if (contentType != null && contentType.contains("json")) {
                        rawOptions.setLanguage("json");
                    } else if (contentType != null && contentType.contains("xml")) {
                        rawOptions.setLanguage("xml");
                    } else {
                        rawOptions.setLanguage("text");
                    }
                    
                    options.setRaw(rawOptions);
                    body.setOptions(options);
                } else if (contentType.contains("form") || contentType.contains("urlencoded")) {
                    body.setMode("urlencoded");
                    // Handle form data
                    // TODO: Implement form data conversion
                }
                
                postmanRequest.setBody(body);
            }
            
            postmanRequest.setUrl(url);
            item.setRequest(postmanRequest);
        } else {
            // For non-request test steps, create a dummy request
            PostmanRequest dummyRequest = new PostmanRequest();
            dummyRequest.setMethod("GET");
            
            PostmanRequest.PostmanUrl url = new PostmanRequest.PostmanUrl();
            url.setRaw("http://example.com/" + testStep.getName().replaceAll("\\s+", "-"));
            url.setProtocol("http");
            url.setHost(Arrays.asList("example", "com"));
            url.setPath(Arrays.asList(testStep.getName().replaceAll("\\s+", "-")));
            
            dummyRequest.setUrl(url);
            item.setRequest(dummyRequest);
        }
        
        // Add pre-request script if needed
        addPreRequestScript(item, testStep);
        
        // Add test script if needed
        addTestScript(item, testStep);
        
        return item;
    }
    
    /**
     * Convert ReadyAPI variable syntax ${VariableName} to Postman variable syntax {{VariableName}}
     * 
     * @param input The input string with ReadyAPI variable syntax
     * @return The string with Postman variable syntax
     */
    private String convertReadyApiVariablesToPostman(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        // Handle ${VariableName} syntax 
        String result = input.replaceAll("\\$\\{([^}]+)\\}", "{{$1}}");
        
        // Handle XPath variable references
        Pattern xpathPattern = Pattern.compile("\\$\\([^)]+\\)");
        Matcher matcher = xpathPattern.matcher(result);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            // For XPath expressions, we'll use a variable with a similar name
            String match = matcher.group();
            String varName = "xpathVar_" + Math.abs(match.hashCode());
            conversionIssues.add("XPath expression " + match + " was converted to a Postman variable {{" + varName + "}}. " +
                                "You may need to set this variable in your pre-request script.");
            matcher.appendReplacement(sb, "{{" + varName + "}}");
        }
        matcher.appendTail(sb);
        result = sb.toString();
        
        // Handle property expansion with context.expand
        result = result.replaceAll("context\\.expand\\(\\s*'([^']+)'\\s*\\)", "{{$1}}");
        result = result.replaceAll("context\\.expand\\(\\s*\"([^\"]+)\"\\s*\\)", "{{$1}}");
        
        return result;
    }
    
    /**
     * Parse a URL into its components.
     * 
     * @param endpoint The URL endpoint
     * @param url The PostmanUrl to populate
     */
    private void parseUrl(String endpoint, PostmanRequest.PostmanUrl url) {
        try {
            URL parsedUrl = new URL(endpoint);
            
            url.setProtocol(parsedUrl.getProtocol());
            
            // Split host by dots
            String host = parsedUrl.getHost();
            if (host != null && !host.isEmpty()) {
                url.setHost(Arrays.asList(host.split("\\.")));
            }
            
            // Split path by slashes, removing empty elements
            String path = parsedUrl.getPath();
            if (path != null && !path.isEmpty()) {
                List<String> pathParts = new ArrayList<>();
                for (String part : path.split("/")) {
                    if (!part.isEmpty()) {
                        pathParts.add(part);
                    }
                }
                if (!pathParts.isEmpty()) {
                    url.setPath(pathParts);
                }
            }
            
            // Extract query parameters
            String query = parsedUrl.getQuery();
            if (query != null && !query.isEmpty()) {
                List<PostmanRequest.PostmanUrl.PostmanQueryParam> queryParams = new ArrayList<>();
                
                String[] params = query.split("&");
                for (String param : params) {
                    String[] keyValue = param.split("=", 2);
                    PostmanRequest.PostmanUrl.PostmanQueryParam queryParam = new PostmanRequest.PostmanUrl.PostmanQueryParam();
                    queryParam.setKey(keyValue[0]);
                    if (keyValue.length > 1) {
                        queryParam.setValue(convertReadyApiVariablesToPostman(keyValue[1]));
                    } else {
                        queryParam.setValue("");
                    }
                    queryParams.add(queryParam);
                }
                
                url.setQuery(queryParams);
            }
            
            // Extract port if non-standard
            int port = parsedUrl.getPort();
            if (port != -1) {
                url.setPort(String.valueOf(port));
            }
            
        } catch (MalformedURLException e) {
            // For malformed URLs, just keep the raw URL
            conversionIssues.add("Could not parse URL: " + endpoint + ". Error: " + e.getMessage());
        }
    }
    
    /**
     * Add a pre-request script to a Postman item.
     * 
     * @param item The Postman item
     * @param testStep The ReadyAPI test step
     */
    private void addPreRequestScript(PostmanItem item, ReadyApiTestStep testStep) {
        // Add pre-request script if this is a script step meant for pre-request
        if ("groovy".equalsIgnoreCase(testStep.getType()) && 
            (testStep.getName().toLowerCase().contains("setup") || 
             testStep.getName().toLowerCase().contains("prerequest") || 
             testStep.getName().toLowerCase().contains("pre-request"))) {
            
            String scriptContent = testStep.convertGroovyToJavaScript();
            if (!scriptContent.isEmpty()) {
                PostmanEvent preRequestEvent = new PostmanEvent();
                preRequestEvent.setListen("prerequest");
                
                PostmanEvent.PostmanScript script = new PostmanEvent.PostmanScript();
                script.setType("text/javascript");
                script.setExec(Arrays.asList(scriptContent.split("\n")));
                
                preRequestEvent.setScript(script);
                
                if (item.getEvent() == null) {
                    item.setEvent(new ArrayList<>());
                }
                item.getEvent().add(preRequestEvent);
            }
        }
    }
    
    /**
     * Add a test script to a Postman item.
     * 
     * @param item The Postman item
     * @param testStep The ReadyAPI test step
     */
    private void addTestScript(PostmanItem item, ReadyApiTestStep testStep) {
        // Check if this is a script step or property transfer
        if (("groovy".equalsIgnoreCase(testStep.getType()) && 
             !testStep.getName().toLowerCase().contains("prerequest") && 
             !testStep.getName().toLowerCase().contains("pre-request") && 
             !testStep.getName().toLowerCase().contains("setup")) ||
            "propertytransfer".equalsIgnoreCase(testStep.getType()) ||
            "datasourceloop".equalsIgnoreCase(testStep.getType()) ||
            "datasink".equalsIgnoreCase(testStep.getType()) ||
            "properties".equalsIgnoreCase(testStep.getType())) {
            
            String scriptContent = testStep.toJavaScript();
            if (!scriptContent.isEmpty()) {
                PostmanEvent testEvent = new PostmanEvent();
                testEvent.setListen("test");
                
                PostmanEvent.PostmanScript script = new PostmanEvent.PostmanScript();
                script.setType("text/javascript");
                script.setExec(Arrays.asList(scriptContent.split("\n")));
                
                testEvent.setScript(script);
                
                if (item.getEvent() == null) {
                    item.setEvent(new ArrayList<>());
                }
                item.getEvent().add(testEvent);
            }
        }
    }
} 