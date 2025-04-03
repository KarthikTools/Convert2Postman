package com.readyapi.converter.postman;

/**
 * Represents a Postman variable.
 */
public class PostmanVariable {
    private String key;
    private String value;
    private String type;
    private boolean disabled;
    
    public PostmanVariable() {
        this.type = "string";
    }
    
    public PostmanVariable(String key, String value) {
        this();
        this.key = key;
        this.value = value;
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
    
    public boolean isDisabled() {
        return disabled;
    }
    
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
} 