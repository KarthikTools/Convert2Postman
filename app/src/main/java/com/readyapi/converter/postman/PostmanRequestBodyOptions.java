package com.readyapi.converter.postman;

import java.util.Map;

/**
 * Represents options for a Postman request body.
 */
public class PostmanRequestBodyOptions {
    private Map<String, String> raw;
    
    public PostmanRequestBodyOptions() {
    }
    
    /**
     * Get the raw options.
     * 
     * @return The raw options
     */
    public Map<String, String> getRaw() {
        return raw;
    }
    
    /**
     * Set the raw options.
     * 
     * @param raw The raw options
     */
    public void setRaw(Map<String, String> raw) {
        this.raw = raw;
    }
} 