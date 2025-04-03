package com.readyapi.converter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Postman item (folder or request).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostmanItem {
    private String name;
    private String description;
    private List<PostmanItem> item;
    private PostmanRequest request;
    private List<PostmanEvent> event;
    private Object response;  // Could be a complex structure, using Object for simplicity
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @JsonProperty("item")
    public List<PostmanItem> getItem() {
        return item;
    }
    
    public void setItem(List<PostmanItem> item) {
        this.item = item;
    }
    
    public void addItem(PostmanItem item) {
        if (this.item == null) {
            this.item = new ArrayList<>();
        }
        this.item.add(item);
    }
    
    @JsonProperty("request")
    public PostmanRequest getRequest() {
        return request;
    }
    
    public void setRequest(PostmanRequest request) {
        this.request = request;
    }
    
    @JsonProperty("event")
    public List<PostmanEvent> getEvent() {
        return event;
    }
    
    public void setEvent(List<PostmanEvent> event) {
        this.event = event;
    }
    
    public void addEvent(PostmanEvent event) {
        if (this.event == null) {
            this.event = new ArrayList<>();
        }
        this.event.add(event);
    }
    
    @JsonProperty("response")
    public Object getResponse() {
        return response;
    }
    
    public void setResponse(Object response) {
        this.response = response;
    }
    
    /**
     * Check if this is a folder (has items, no request).
     * 
     * @return true if this is a folder, false if it's a request
     */
    public boolean isFolder() {
        return item != null && !item.isEmpty() && request == null;
    }
    
    /**
     * Find a child item by name.
     * 
     * @param name The name to search for
     * @return The found item or null if not found
     */
    public PostmanItem findItemByName(String name) {
        if (this.item == null) {
            return null;
        }
        
        for (PostmanItem child : this.item) {
            if (name.equals(child.getName())) {
                return child;
            }
        }
        
        return null;
    }
} 