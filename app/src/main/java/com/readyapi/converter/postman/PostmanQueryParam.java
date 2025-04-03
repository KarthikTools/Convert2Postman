package com.readyapi.converter.postman;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents a query parameter in a Postman URL
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostmanQueryParam {
    private String key;
    private String value;
    private String type;
    private String description;
    private Boolean disabled;
    
    public PostmanQueryParam() {
        this.type = "text";
    }
    
    public PostmanQueryParam(String key, String value) {
        this();
        this.key = key;
        this.value = value;
    }
    
    /**
     * Get the parameter key.
     * 
     * @return The parameter key
     */
    public String getKey() {
        return key;
    }
    
    /**
     * Set the parameter key.
     * 
     * @param key The parameter key
     */
    public void setKey(String key) {
        this.key = key;
    }
    
    /**
     * Get the parameter value.
     * 
     * @return The parameter value
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Set the parameter value.
     * 
     * @param value The parameter value
     */
    public void setValue(String value) {
        this.value = value;
    }
    
    /**
     * Get the parameter type.
     * 
     * @return The parameter type
     */
    public String getType() {
        return type;
    }
    
    /**
     * Set the parameter type.
     * 
     * @param type The parameter type
     */
    public void setType(String type) {
        this.type = type;
    }
    
    /**
     * Get the parameter description.
     * 
     * @return The parameter description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Set the parameter description.
     * 
     * @param description The parameter description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Boolean getDisabled() {
        return disabled;
    }
    
    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }
} 