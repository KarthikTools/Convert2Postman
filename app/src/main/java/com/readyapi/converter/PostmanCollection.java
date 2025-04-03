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

import com.readyapi.converter.postman.PostmanInfo;
import com.readyapi.converter.postman.PostmanItem;

/**
 * Represents a Postman collection.
 */
public class PostmanCollection {
    private static final Logger logger = LoggerFactory.getLogger(PostmanCollection.class);
    
    private PostmanInfo info;
    private List<PostmanItem> items;
    private List<PostmanVariable> variables;
    private List<String> conversionIssues = new ArrayList<>();
    
    public PostmanCollection() {
        this.items = new ArrayList<>();
        this.variables = new ArrayList<>();
    }
    
    @JsonProperty("info")
    public PostmanInfo getInfo() {
        return info;
    }
    
    public void setInfo(PostmanInfo info) {
        this.info = info;
    }
    
    @JsonProperty("item")
    public List<PostmanItem> getItems() {
        return items;
    }
    
    public void setItems(List<PostmanItem> items) {
        this.items = items;
    }
    
    public void addItem(PostmanItem item) {
        this.items.add(item);
    }
    
    @JsonProperty("variable")
    public List<PostmanVariable> getVariables() {
        return variables;
    }
    
    public void setVariables(List<PostmanVariable> variables) {
        this.variables = variables;
    }
    
    public void addVariable(PostmanVariable variable) {
        this.variables.add(variable);
    }
    
    public List<String> getConversionIssues() {
        return conversionIssues;
    }
    
    public void setConversionIssues(List<String> conversionIssues) {
        this.conversionIssues = conversionIssues;
    }
    
    public void addConversionIssue(String issue) {
        this.conversionIssues.add(issue);
    }
    
    /**
     * Save the collection to a JSON file.
     * 
     * @param filePath Path to save the file
     * @throws IOException If there's an error writing the file
     */
    public void saveToFile(String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(new File(filePath), this);
        logger.info("Saved Postman collection to: {}", filePath);
    }
    
    /**
     * Nested class to represent Postman collection info.
     */
    public static class PostmanInfo {
        private String name;
        @JsonProperty("_postman_id")
        private String postmanId;
        private String description;
        private String schema = "https://schema.getpostman.com/json/collection/v2.1.0/collection.json";
        
        public PostmanInfo(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getPostmanId() {
            return postmanId;
        }
        
        public void setPostmanId(String postmanId) {
            this.postmanId = postmanId;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public String getSchema() {
            return schema;
        }
        
        public void setSchema(String schema) {
            this.schema = schema;
        }
    }

    public static class PostmanVariable {
        private String key;
        private String value;
        private String type;
        private boolean disabled;

        public PostmanVariable(String key, String value, String type, boolean disabled) {
            this.key = key;
            this.value = value;
            this.type = type;
            this.disabled = disabled;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public String getType() {
            return type;
        }

        public boolean isDisabled() {
            return disabled;
        }
    }
} 