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
 * Represents a Postman collection.
 */
public class PostmanCollection {
    private static final Logger logger = LoggerFactory.getLogger(PostmanCollection.class);
    
    private PostmanInfo info;
    private List<PostmanItem> item = new ArrayList<>();
    private List<PostmanVariable> variable = new ArrayList<>();
    private List<String> conversionIssues = new ArrayList<>();
    
    public PostmanCollection() {
        // Initialize info with a random UUID
        this.info = new PostmanInfo();
        this.info.setPostmanId(UUID.randomUUID().toString());
    }
    
    @JsonProperty("info")
    public PostmanInfo getInfo() {
        return info;
    }
    
    public void setInfo(PostmanInfo info) {
        this.info = info;
    }
    
    @JsonProperty("item")
    public List<PostmanItem> getItem() {
        return item;
    }
    
    public void setItem(List<PostmanItem> item) {
        this.item = item;
    }
    
    public void addItem(PostmanItem item) {
        this.item.add(item);
    }
    
    @JsonProperty("variable")
    public List<PostmanVariable> getVariable() {
        return variable;
    }
    
    public void setVariable(List<PostmanVariable> variable) {
        this.variable = variable;
    }
    
    public void addVariable(PostmanVariable variable) {
        this.variable.add(variable);
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
} 