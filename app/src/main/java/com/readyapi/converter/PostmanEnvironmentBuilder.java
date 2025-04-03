package com.readyapi.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dom4j.Element;

/**
 * Builder for creating Postman environments from ReadyAPI projects.
 */
public class PostmanEnvironmentBuilder {
    private static final Logger logger = LoggerFactory.getLogger(PostmanEnvironmentBuilder.class);
    
    private ReadyApiProject project;
    private Element rootElement;
    private List<PostmanEnvironment> environments = new ArrayList<>();
    private ConversionIssueReporter issueReporter;
    
    private final ObjectMapper objectMapper;
    private final FunctionLibraryConverter libraryConverter;
    
    /**
     * Create a new PostmanEnvironmentBuilder.
     * 
     * @param project The ReadyAPI project
     * @param rootElement The root XML element of the project
     * @param issueReporter Reporter for conversion issues
     */
    public PostmanEnvironmentBuilder(ReadyApiProject project, Element rootElement, ConversionIssueReporter issueReporter) {
        this.project = project;
        this.rootElement = rootElement;
        this.issueReporter = issueReporter;
        this.objectMapper = new ObjectMapper();
        this.libraryConverter = new FunctionLibraryConverter();
    }
    
    /**
     * Build Postman environments from the ReadyAPI project.
     * 
     * @return The main Postman environment
     */
    public PostmanEnvironment build() {
        // Create default environment
        PostmanEnvironment mainEnvironment = new PostmanEnvironment();
        mainEnvironment.setName(project.getName() + " Environment");
        
        // Extract project properties as variables
        extractProjectProperties(mainEnvironment);
        
        // Check if project has ReadyAPI environment configurations
        Element environmentsElement = rootElement.element("environments");
        if (environmentsElement != null) {
            // Create environment-specific Postman environments
            createEnvironmentSpecificConfigurations(environmentsElement);
        }
        
        // Add all composite project properties if this is a composite project
        if (!project.getReferencedProjects().isEmpty()) {
            addCompositeProjectProperties(mainEnvironment);
        }
        
        return mainEnvironment;
    }
    
    /**
     * Get all environments created during the build process.
     * 
     * @return List of all Postman environments
     */
    public List<PostmanEnvironment> getAllEnvironments() {
        return environments;
    }
    
    /**
     * Extract project properties and add them to the environment.
     * 
     * @param environment The Postman environment to populate
     */
    private void extractProjectProperties(PostmanEnvironment environment) {
        Map<String, String> properties = project.getProperties();
        
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            // Skip internal properties
            if (key.startsWith("__") || key.startsWith("file:")) {
                continue;
            }
            
            // Add as environment variable
            environment.addVariable(key, value);
            logger.debug("Added project property to environment: {}", key);
        }
        
        // Add project-specific variables
        environment.addVariable("projectName", project.getName());
        environment.addVariable("environment", "Default");
        
        // Add the main environment to our list
        environments.add(environment);
    }
    
    /**
     * Create environment-specific configurations based on ReadyAPI environments.
     * 
     * @param environmentsElement The XML element containing environment definitions
     */
    private void createEnvironmentSpecificConfigurations(Element environmentsElement) {
        List<Element> envElements = environmentsElement.elements("environment");
        
        for (Element envElement : envElements) {
            String envName = envElement.attributeValue("name");
            if (envName == null || envName.isEmpty()) {
                continue;
            }
            
            // Create a new environment
            PostmanEnvironment envSpecific = new PostmanEnvironment();
            envSpecific.setName(project.getName() + " - " + envName);
            
            // Add environment-specific setting
            envSpecific.addVariable("environment", envName);
            envSpecific.addVariable("projectName", project.getName());
            
            // Extract environment properties
            Element propertiesElement = envElement.element("properties");
            if (propertiesElement != null) {
                for (Element propertyElement : propertiesElement.elements("property")) {
                    Element nameElement = propertyElement.element("name");
                    Element valueElement = propertyElement.element("value");
                    
                    if (nameElement != null && valueElement != null) {
                        String name = nameElement.getTextTrim();
                        String value = valueElement.getTextTrim();
                        
                        if (name != null && !name.isEmpty()) {
                            envSpecific.addVariable(name, value);
                            logger.debug("Added environment-specific property: {} for {}", name, envName);
                        }
                    }
                }
            }
            
            // Handle specific environment settings based on name pattern
            if (envName.toLowerCase().contains("dev") || envName.toLowerCase().contains("development")) {
                // Add development-specific settings
                addDevelopmentSettings(envSpecific);
            } else if (envName.toLowerCase().contains("test") || envName.toLowerCase().contains("qa")) {
                // Add test/QA-specific settings
                addTestSettings(envSpecific);
            } else if (envName.toLowerCase().contains("prod") || envName.toLowerCase().contains("production")) {
                // Add production-specific settings
                addProductionSettings(envSpecific);
            }
            
            // Add to our list of environments
            environments.add(envSpecific);
        }
    }
    
    /**
     * Add development-specific environment settings.
     * 
     * @param environment The environment to update
     */
    private void addDevelopmentSettings(PostmanEnvironment environment) {
        // Set common development settings if not already present
        setIfMissing(environment, "enableMocks", "true");
        setIfMissing(environment, "logLevel", "debug");
        setIfMissing(environment, "timeoutMs", "60000");
    }
    
    /**
     * Add test/QA-specific environment settings.
     * 
     * @param environment The environment to update
     */
    private void addTestSettings(PostmanEnvironment environment) {
        // Set common test/QA settings if not already present
        setIfMissing(environment, "enableMocks", "false");
        setIfMissing(environment, "logLevel", "info");
        setIfMissing(environment, "timeoutMs", "30000");
    }
    
    /**
     * Add production-specific environment settings.
     * 
     * @param environment The environment to update
     */
    private void addProductionSettings(PostmanEnvironment environment) {
        // Set common production settings if not already present
        setIfMissing(environment, "enableMocks", "false");
        setIfMissing(environment, "logLevel", "error");
        setIfMissing(environment, "timeoutMs", "15000");
    }
    
    /**
     * Set a variable in the environment if it doesn't already exist.
     * 
     * @param environment The environment to update
     * @param name The variable name
     * @param value The variable value
     */
    private void setIfMissing(PostmanEnvironment environment, String name, String value) {
        // Check if variable already exists
        for (PostmanEnvironment.PostmanEnvironmentVariable var : environment.getVariables()) {
            if (var.getKey().equals(name)) {
                return; // Already exists, don't override
            }
        }
        
        // Add the variable
        environment.addVariable(name, value);
    }
    
    /**
     * Add properties from all referenced projects in a composite project.
     * 
     * @param environment The environment to update
     */
    private void addCompositeProjectProperties(PostmanEnvironment environment) {
        for (ReadyApiProject refProject : project.getReferencedProjects()) {
            Map<String, String> refProperties = refProject.getProperties();
            
            for (Map.Entry<String, String> entry : refProperties.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                
                // Skip internal properties
                if (key.startsWith("__") || key.startsWith("file:")) {
                    continue;
                }
                
                // Add namespace to avoid conflicts
                String namespacedKey = refProject.getName() + "_" + key;
                environment.addVariable(namespacedKey, value);
                
                logger.debug("Added referenced project property: {}", namespacedKey);
                issueReporter.addInfo("EnvironmentBuilder", 
                                     "Added property from referenced project: " + refProject.getName() + " with key: " + namespacedKey);
            }
        }
    }
} 