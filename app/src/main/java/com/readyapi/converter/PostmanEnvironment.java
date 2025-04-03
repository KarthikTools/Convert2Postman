package com.readyapi.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Represents a Postman environment.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostmanEnvironment {
    private static final Logger logger = LoggerFactory.getLogger(PostmanEnvironment.class);
    
    private String id;
    private String name;
    private List<PostmanEnvironmentVariable> values = new ArrayList<>();
    
    public PostmanEnvironment() {
        this.id = UUID.randomUUID().toString();
    }
    
    public PostmanEnvironment(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    @JsonProperty("values")
    public List<PostmanEnvironmentVariable> getValues() {
        return values;
    }
    
    @JsonIgnore
    public List<PostmanEnvironmentVariable> getVariables() {
        return values;
    }
    
    public void setValues(List<PostmanEnvironmentVariable> values) {
        this.values = values;
    }
    
    /**
     * Add a variable to the environment.
     * 
     * @param variable The variable to add
     */
    public void addVariable(PostmanEnvironmentVariable variable) {
        this.values.add(variable);
    }
    
    /**
     * Add a variable to the environment.
     * 
     * @param key The variable key
     * @param value The variable value
     * @return The created environment variable
     */
    public PostmanEnvironmentVariable addVariable(String key, String value) {
        PostmanEnvironmentVariable variable = new PostmanEnvironmentVariable(key, value);
        this.values.add(variable);
        return variable;
    }
    
    /**
     * Check if the environment contains a variable with the given key.
     * 
     * @param key The variable key
     * @return True if the environment contains the variable
     */
    public boolean hasVariable(String key) {
        for (PostmanEnvironmentVariable variable : values) {
            if (variable.getKey().equals(key)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get a variable by key.
     * 
     * @param key The variable key
     * @return The variable, or null if not found
     */
    public PostmanEnvironmentVariable getVariable(String key) {
        for (PostmanEnvironmentVariable variable : values) {
            if (variable.getKey().equals(key)) {
                return variable;
            }
        }
        return null;
    }
    
    /**
     * Save the environment to a JSON file.
     * 
     * @param filePath Path to save the file
     * @throws IOException If there's an error writing the file
     */
    public void saveToFile(String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(new File(filePath), this);
        logger.info("Saved Postman environment to: {}", filePath);
    }
    
    /**
     * Represents a Postman environment variable.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PostmanEnvironmentVariable {
        private String key;
        private String value;
        private String type;
        private boolean enabled;
        
        public PostmanEnvironmentVariable() {
            this.enabled = true;
            this.type = "default";
        }
        
        /**
         * Create a new PostmanEnvironmentVariable.
         * 
         * @param key The variable key
         * @param value The variable value
         */
        public PostmanEnvironmentVariable(String key, String value) {
            this.key = key;
            this.value = value;
            this.type = "default";
            this.enabled = true;
        }
        
        /**
         * Create a new PostmanEnvironmentVariable.
         * 
         * @param key The variable key
         * @param value The variable value
         * @param type The variable type
         * @param enabled Whether the variable is enabled
         */
        public PostmanEnvironmentVariable(String key, String value, String type, boolean enabled) {
            this.key = key;
            this.value = value;
            this.type = type;
            this.enabled = enabled;
        }
        
        public String getKey() {
            return key;
        }
        
        public void setKey(String key) {
            this.key = key;
        }
        
        public String getValue() {
            return value;
        }
        
        public void setValue(String value) {
            this.value = value;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
} 