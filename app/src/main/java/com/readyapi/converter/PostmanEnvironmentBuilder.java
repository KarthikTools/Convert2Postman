package com.readyapi.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder for creating Postman environments from ReadyAPI projects.
 */
public class PostmanEnvironmentBuilder {
    private static final Logger logger = LoggerFactory.getLogger(PostmanEnvironmentBuilder.class);
    
    private final ReadyApiProject project;
    private final ObjectMapper objectMapper;
    private final FunctionLibraryConverter libraryConverter;
    
    public PostmanEnvironmentBuilder(ReadyApiProject project) {
        this.project = project;
        this.objectMapper = new ObjectMapper();
        this.libraryConverter = new FunctionLibraryConverter();
    }
    
    /**
     * Build a Postman environment from the ReadyAPI project.
     * 
     * @return A PostmanEnvironment
     */
    public PostmanEnvironment build() {
        logger.info("Building Postman environment from ReadyAPI project: {}", project.getName());
        
        PostmanEnvironment environment = new PostmanEnvironment();
        environment.setId(UUID.randomUUID().toString());
        environment.setName(project.getName() + " Environment");
        
        List<PostmanEnvironment.PostmanEnvironmentVariable> variables = new ArrayList<>();
        
        // Add project properties as environment variables
        for (Map.Entry<String, String> property : project.getProperties().entrySet()) {
            PostmanEnvironment.PostmanEnvironmentVariable variable = new PostmanEnvironment.PostmanEnvironmentVariable();
            variable.setKey(property.getKey());
            variable.setValue(property.getValue());
            variable.setType("default");
            variable.setEnabled(true);
            variables.add(variable);
        }
        
        // Add function libraries as environment variables
        for (ReadyApiScriptLibrary library : project.getScriptLibraries()) {
            try {
                String libraryJson = libraryConverter.convertLibraryToPostmanVariable(library.getName(), library.getContent());
                PostmanEnvironment.PostmanEnvironmentVariable variable = objectMapper.readValue(libraryJson, PostmanEnvironment.PostmanEnvironmentVariable.class);
                variables.add(variable);
            } catch (Exception e) {
                // Log error but continue with other variables
                logger.error("Failed to convert library: {}: {}", library.getName(), e.getMessage());
            }
        }
        
        // Add test suite properties to environment
        for (ReadyApiTestSuite testSuite : project.getTestSuites()) {
            for (Map.Entry<String, String> property : testSuite.getProperties().entrySet()) {
                String key = testSuite.getName() + "_" + property.getKey();
                PostmanEnvironment.PostmanEnvironmentVariable variable = new PostmanEnvironment.PostmanEnvironmentVariable();
                variable.setKey(key);
                variable.setValue(property.getValue());
                variable.setType("default");
                variable.setEnabled(true);
                variables.add(variable);
            }
            
            // Add test case properties to environment
            for (ReadyApiTestCase testCase : testSuite.getTestCases()) {
                for (Map.Entry<String, String> property : testCase.getProperties().entrySet()) {
                    String key = testSuite.getName() + "_" + testCase.getName() + "_" + property.getKey();
                    PostmanEnvironment.PostmanEnvironmentVariable variable = new PostmanEnvironment.PostmanEnvironmentVariable();
                    variable.setKey(key);
                    variable.setValue(property.getValue());
                    variable.setType("default");
                    variable.setEnabled(true);
                    variables.add(variable);
                }
            }
        }
        
        environment.setValues(variables);
        
        logger.info("Built Postman environment with {} variables", environment.getValues().size());
        
        return environment;
    }
} 