package com.readyapi.converter.postman;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Postman collection.
 */
public class PostmanCollection {
    private PostmanInfo info;
    private List<PostmanItem> item;
    private List<PostmanVariable> variable;
    
    public PostmanCollection() {
        this.item = new ArrayList<>();
        this.variable = new ArrayList<>();
    }
    
    public PostmanInfo getInfo() {
        return info;
    }
    
    public void setInfo(PostmanInfo info) {
        this.info = info;
    }
    
    public List<PostmanItem> getItem() {
        return item;
    }
    
    public void setItem(List<PostmanItem> item) {
        this.item = item;
    }
    
    public void addItem(PostmanItem item) {
        this.item.add(item);
    }
    
    public List<PostmanVariable> getVariable() {
        return variable;
    }
    
    public void setVariable(List<PostmanVariable> variable) {
        this.variable = variable;
    }
    
    public void addVariable(PostmanVariable variable) {
        this.variable.add(variable);
    }
    
    /**
     * Represents collection information.
     */
    public static class PostmanInfo {
        private String name;
        private String schema;
        
        public PostmanInfo() {
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getSchema() {
            return schema;
        }
        
        public void setSchema(String schema) {
            this.schema = schema;
        }
    }
    
    /**
     * Represents a collection variable.
     */
    public static class PostmanVariable {
        private String key;
        private String value;
        private String type;
        private boolean disabled;
        
        public PostmanVariable() {
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
        
        public boolean isDisabled() {
            return disabled;
        }
        
        public void setDisabled(boolean disabled) {
            this.disabled = disabled;
        }
    }
} 