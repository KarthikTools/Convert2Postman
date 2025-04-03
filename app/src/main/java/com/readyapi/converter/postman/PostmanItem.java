package com.readyapi.converter.postman;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Postman collection item (request/folder)
 */
public class PostmanItem {
    private String name;
    private PostmanRequest request;
    private List<PostmanEvent> event;
    private List<PostmanItem> item;
    
    public PostmanItem() {
        this.event = new ArrayList<>();
        this.item = new ArrayList<>();
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public PostmanRequest getRequest() {
        return request;
    }
    
    public void setRequest(PostmanRequest request) {
        this.request = request;
    }
    
    public List<PostmanEvent> getEvent() {
        return event;
    }
    
    public void setEvent(List<PostmanEvent> event) {
        this.event = event;
    }
    
    public void addEvent(PostmanEvent event) {
        this.event.add(event);
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
    
    /**
     * Represents a Postman event (pre-request script or test)
     */
    public static class PostmanEvent {
        private String listen;
        private PostmanScript script;
        
        public PostmanEvent() {
        }
        
        public String getListen() {
            return listen;
        }
        
        public void setListen(String listen) {
            this.listen = listen;
        }
        
        public PostmanScript getScript() {
            return script;
        }
        
        public void setScript(PostmanScript script) {
            this.script = script;
        }
    }
    
    /**
     * Represents a Postman script
     */
    public static class PostmanScript {
        private String type;
        private String exec;
        
        public PostmanScript(String type, String exec) {
            this.type = type;
            this.exec = exec;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public String getExec() {
            return exec;
        }
        
        public void setExec(String exec) {
            this.exec = exec;
        }
    }
} 