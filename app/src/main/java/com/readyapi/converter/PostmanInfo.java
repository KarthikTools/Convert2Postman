package com.readyapi.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.NullNode;

/**
 * Represents the info section of a Postman collection.
 */
public class PostmanInfo {
    private String name;
    private String description;
    private String schema;
    private String postmanId;
    
    public PostmanInfo(String name) {
        this.name = name;
        this.schema = "https://schema.getpostman.com/json/collection/v2.1.0/collection.json";
        this.postmanId = java.util.UUID.randomUUID().toString();
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
    
    public String getPostmanId() {
        return postmanId;
    }
    
    public void setPostmanId(String postmanId) {
        this.postmanId = postmanId;
    }
    
    public ObjectNode toJson() {
        ObjectNode info = new ObjectMapper().createObjectNode();
        info.set("name", new TextNode(name));
        info.set("description", description != null ? new TextNode(description) : NullNode.getInstance());
        info.set("schema", new TextNode(schema));
        info.set("_postman_id", new TextNode(postmanId));
        return info;
    }
} 