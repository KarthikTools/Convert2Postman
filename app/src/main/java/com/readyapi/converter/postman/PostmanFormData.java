package com.readyapi.converter.postman;

/**
 * Represents a form data parameter in a Postman request.
 */
public class PostmanFormData {
    private String key;
    private String value;
    private String type;
    
    public PostmanFormData() {
    }
    
    public PostmanFormData(String key, String value) {
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
} 