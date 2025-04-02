package com.readyapi.converter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a Postman environment.
 */
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
    
    @JsonProperty("id")
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    @JsonProperty("name")
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
    
    public void setValues(List<PostmanEnvironmentVariable> values) {
        this.values = values;
    }
    
    public void addVariable(String key, String value) {
        this.values.add(new PostmanEnvironmentVariable(key, value));
    }
    
    public void addVariable(String key, String value, String type) {
        this.values.add(new PostmanEnvironmentVariable(key, value, type));
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
     * Nested class to represent a Postman environment variable.
     */
    public static class PostmanEnvironmentVariable {
        private String key;
        private String value;
        private String type;
        private boolean enabled = true;
        
        public PostmanEnvironmentVariable() {
        }
        
        public PostmanEnvironmentVariable(String key, String value) {
            this.key = key;
            this.value = value;
            this.type = "default";
        }
        
        public PostmanEnvironmentVariable(String key, String value, String type) {
            this.key = key;
            this.value = value;
            this.type = type;
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