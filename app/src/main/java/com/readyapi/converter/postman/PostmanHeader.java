package com.readyapi.converter.postman;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents a Postman request header.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostmanHeader {
    private String key;
    private String value;
    private String description;
    private String type;
    private boolean disabled;
    
    public PostmanHeader() {
    }
    
    public PostmanHeader(String key, String value) {
        this.key = key;
        this.value = value;
    }
    
    /**
     * Get the header key.
     * 
     * @return The header key
     */
    public String getKey() {
        return key;
    }
    
    /**
     * Set the header key.
     * 
     * @param key The header key
     */
    public void setKey(String key) {
        this.key = key;
    }
    
    /**
     * Get the header value.
     * 
     * @return The header value
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Set the header value.
     * 
     * @param value The header value
     */
    public void setValue(String value) {
        this.value = value;
    }
    
    /**
     * Get the header description.
     * 
     * @return The header description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Set the header description.
     * 
     * @param description The header description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Get the header type.
     * 
     * @return The header type
     */
    public String getType() {
        return type;
    }
    
    /**
     * Set the header type.
     * 
     * @param type The header type
     */
    public void setType(String type) {
        this.type = type;
    }
    
    public boolean isDisabled() {
        return disabled;
    }
    
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
} 