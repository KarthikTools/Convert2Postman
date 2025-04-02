package com.readyapi.converter;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents a Postman variable.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostmanVariable {
    private String key;
    private String value;
    private String type;
    private String description;
    private boolean disabled;
    
    public PostmanVariable() {
    }
    
    public PostmanVariable(String key, String value) {
        this.key = key;
        this.value = value;
        this.type = "string";
    }
    
    public PostmanVariable(String key, String value, String type) {
        this.key = key;
        this.value = value;
        this.type = type;
    }
    
    public PostmanVariable(String key, String value, String type, boolean disabled) {
        this.key = key;
        this.value = value;
        this.type = type;
        this.disabled = disabled;
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public boolean isDisabled() {
        return disabled;
    }
    
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
} 