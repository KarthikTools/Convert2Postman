package com.readyapi.converter;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PostmanEnvironmentVariable {
    @JsonProperty("key")
    private String key;
    
    @JsonProperty("value")
    private String value;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("enabled")
    private boolean enabled;
    
    public PostmanEnvironmentVariable() {
        // Default constructor for Jackson
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