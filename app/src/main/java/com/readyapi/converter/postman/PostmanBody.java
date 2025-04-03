package com.readyapi.converter.postman;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents a Postman request body.
 */
public class PostmanBody {
    private String mode;
    private String raw;
    private String contentType;
    private List<PostmanFormParameter> urlencoded;
    private List<PostmanFormData> formdata;
    private List<PostmanBodyOption> options;
    
    public PostmanBody() {
        this.mode = "raw";
        this.options = new ArrayList<>();
    }
    
    public String getMode() {
        return mode;
    }
    
    public void setMode(String mode) {
        this.mode = mode;
    }
    
    public String getRaw() {
        return raw;
    }
    
    public void setRaw(String raw) {
        this.raw = raw;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    public List<PostmanFormParameter> getUrlencoded() {
        return urlencoded;
    }
    
    public void setUrlencoded(List<PostmanFormParameter> urlencoded) {
        this.urlencoded = urlencoded;
    }
    
    public List<PostmanFormData> getFormdata() {
        return formdata;
    }
    
    public void setFormdata(List<PostmanFormData> formdata) {
        this.formdata = formdata;
    }
    
    public List<PostmanBodyOption> getOptions() {
        return options;
    }
    
    public void setOptions(List<PostmanBodyOption> options) {
        this.options = options;
    }
    
    public void addOption(PostmanBodyOption option) {
        this.options.add(option);
    }
    
    /**
     * Represents Postman body options.
     */
    public static class PostmanBodyOptions {
        private PostmanRawOptions raw;
        
        public PostmanRawOptions getRaw() {
            return raw;
        }
        
        public void setRaw(PostmanRawOptions raw) {
            this.raw = raw;
        }
    }
    
    /**
     * Represents Postman raw body options.
     */
    public static class PostmanRawOptions {
        private String language;
        
        public String getLanguage() {
            return language;
        }
        
        public void setLanguage(String language) {
            this.language = language;
        }
    }
} 