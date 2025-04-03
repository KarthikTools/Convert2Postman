package com.readyapi.converter;

import java.util.ArrayList;
import java.util.List;
import com.readyapi.converter.postman.PostmanRequest;
import com.readyapi.converter.postman.PostmanEvent;

/**
 * Represents an item in a Postman collection.
 */
public class PostmanItem {
    private String name;
    private String description;
    private List<PostmanItem> item;
    private PostmanRequest request;
    private List<PostmanEvent> event;
    
    public PostmanItem() {
        this.item = new ArrayList<>();
        this.event = new ArrayList<>();
    }
    
    /**
     * Get the item name.
     * 
     * @return The item name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Set the item name.
     * 
     * @param name The item name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Get the item description.
     * 
     * @return The item description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Set the item description.
     * 
     * @param description The item description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Get the child items.
     * 
     * @return The child items
     */
    public List<PostmanItem> getItem() {
        return item;
    }
    
    /**
     * Set the child items.
     * 
     * @param item The child items
     */
    public void setItem(List<PostmanItem> item) {
        this.item = item;
    }
    
    /**
     * Add a child item.
     * 
     * @param item The child item to add
     */
    public void addItem(PostmanItem item) {
        this.item.add(item);
    }
    
    /**
     * Get the request.
     * 
     * @return The request
     */
    public PostmanRequest getRequest() {
        return request;
    }
    
    /**
     * Set the request.
     * 
     * @param request The request
     */
    public void setRequest(PostmanRequest request) {
        this.request = request;
    }
    
    /**
     * Get the events.
     * 
     * @return The events
     */
    public List<PostmanEvent> getEvent() {
        return event;
    }
    
    /**
     * Set the events.
     * 
     * @param event The events
     */
    public void setEvent(List<PostmanEvent> event) {
        this.event = event;
    }
    
    /**
     * Add an event.
     * 
     * @param event The event to add
     */
    public void addEvent(PostmanEvent event) {
        this.event.add(event);
    }
    
    /**
     * Set the pre-request script.
     * 
     * @param script The pre-request script
     */
    public void setPreRequestScript(String script) {
        if (script == null || script.isEmpty()) {
            return;
        }
        
        PostmanEvent event = findOrCreateEvent("prerequest");
        
        // Create a script object and set the exec code
        PostmanEvent.PostmanScript scriptObj = event.getScript();
        if (scriptObj == null) {
            scriptObj = new PostmanEvent.PostmanScript();
            event.setScript(scriptObj);
        }
        
        // Set the script content
        List<String> exec = new ArrayList<>();
        for (String line : script.split("\n")) {
            exec.add(line);
        }
        scriptObj.setExec(exec);
    }
    
    /**
     * Set the test script.
     * 
     * @param script The test script
     */
    public void setTests(String script) {
        if (script == null || script.isEmpty()) {
            return;
        }
        
        PostmanEvent event = findOrCreateEvent("test");
        
        // Create a script object and set the exec code
        PostmanEvent.PostmanScript scriptObj = event.getScript();
        if (scriptObj == null) {
            scriptObj = new PostmanEvent.PostmanScript();
            event.setScript(scriptObj);
        }
        
        // Set the script content
        List<String> exec = new ArrayList<>();
        for (String line : script.split("\n")) {
            exec.add(line);
        }
        scriptObj.setExec(exec);
    }
    
    /**
     * Find an event by listen type, or create it if it doesn't exist.
     * 
     * @param listenType The listen type
     * @return The event
     */
    private PostmanEvent findOrCreateEvent(String listenType) {
        for (PostmanEvent evt : event) {
            if (listenType.equals(evt.getListen())) {
                return evt;
            }
        }
        
        PostmanEvent newEvent = new PostmanEvent();
        newEvent.setListen(listenType);
        
        PostmanEvent.PostmanScript script = new PostmanEvent.PostmanScript();
        newEvent.setScript(script);
        
        event.add(newEvent);
        return newEvent;
    }
} 