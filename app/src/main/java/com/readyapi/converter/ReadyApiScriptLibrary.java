package com.readyapi.converter;

/**
 * Represents a ReadyAPI script library.
 */
public class ReadyApiScriptLibrary {
    private String id;
    private String name;
    private String content;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    /**
     * Convert Groovy script library to JavaScript for Postman.
     * 
     * @return JavaScript code for Postman
     */
    public String convertToJavaScript() {
        return ScriptConverter.convertToJavaScript(content, "library");
    }
    
    @Override
    public String toString() {
        return "ReadyApiScriptLibrary{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", contentLength=" + (content != null ? content.length() : 0) +
                '}';
    }
} 