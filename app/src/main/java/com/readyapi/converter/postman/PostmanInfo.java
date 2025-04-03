package com.readyapi.converter.postman;

/**
 * Represents Postman collection information.
 */
public class PostmanInfo {
    private String name;
    private String description;
    private String schema;
    
    public PostmanInfo() {
        this.schema = "https://schema.getpostman.com/json/collection/v2.1.0/collection.json";
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
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