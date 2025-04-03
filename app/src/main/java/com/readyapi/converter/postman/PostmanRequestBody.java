package com.readyapi.converter.postman;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents a request body in a Postman request.
 */
public class PostmanRequestBody {
    private String mode;
    private String raw;
    private List<PostmanFormParameter> formdata;
    private List<PostmanFormParameter> urlencoded;
    private PostmanRequestBodyOptions options;
    
    public PostmanRequestBody() {
        this.formdata = new ArrayList<>();
        this.urlencoded = new ArrayList<>();
    }
    
    /**
     * Get the body mode.
     * 
     * @return The body mode
     */
    public String getMode() {
        return mode;
    }
    
    /**
     * Set the body mode.
     * 
     * @param mode The body mode
     */
    public void setMode(String mode) {
        this.mode = mode;
    }
    
    /**
     * Get the raw body content.
     * 
     * @return The raw body content
     */
    public String getRaw() {
        return raw;
    }
    
    /**
     * Set the raw body content.
     * 
     * @param raw The raw body content
     */
    public void setRaw(String raw) {
        this.raw = raw;
    }
    
    /**
     * Get the form data parameters.
     * 
     * @return The form data parameters
     */
    public List<PostmanFormParameter> getFormdata() {
        return formdata;
    }
    
    /**
     * Set the form data parameters.
     * 
     * @param formdata The form data parameters
     */
    public void setFormdata(List<PostmanFormParameter> formdata) {
        this.formdata = formdata;
    }
    
    /**
     * Get the URL encoded parameters.
     * 
     * @return The URL encoded parameters
     */
    public List<PostmanFormParameter> getUrlencoded() {
        return urlencoded;
    }
    
    /**
     * Set the URL encoded parameters.
     * 
     * @param urlencoded The URL encoded parameters
     */
    public void setUrlencoded(List<PostmanFormParameter> urlencoded) {
        this.urlencoded = urlencoded;
    }
    
    /**
     * Get the body options.
     * 
     * @return The body options
     */
    public PostmanRequestBodyOptions getOptions() {
        return options;
    }
    
    /**
     * Set the body options.
     * 
     * @param options The body options
     */
    public void setOptions(PostmanRequestBodyOptions options) {
        this.options = options;
    }
} 