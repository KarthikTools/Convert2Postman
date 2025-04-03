package com.readyapi.converter.postman;

/**
 * Represents a form parameter in a Postman request.
 */
public class PostmanFormParameter {
    private String key;
    private String value;
    private String type;
    
    public PostmanFormParameter() {
    }
    
    public PostmanFormParameter(String key, String value) {
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
} 