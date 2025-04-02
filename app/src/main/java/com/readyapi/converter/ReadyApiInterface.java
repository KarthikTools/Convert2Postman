package com.readyapi.converter;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a ReadyAPI interface (REST service).
 */
public class ReadyApiInterface {
    private String id;
    private String name;
    private String type;
    private List<String> endpoints = new ArrayList<>();
    private List<ReadyApiResource> resources = new ArrayList<>();
    
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
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public List<String> getEndpoints() {
        return endpoints;
    }
    
    public void setEndpoints(List<String> endpoints) {
        this.endpoints = endpoints;
    }
    
    public void addEndpoint(String endpoint) {
        this.endpoints.add(endpoint);
    }
    
    public String getDefaultEndpoint() {
        return endpoints.isEmpty() ? "" : endpoints.get(0);
    }
    
    public List<ReadyApiResource> getResources() {
        return resources;
    }
    
    public void setResources(List<ReadyApiResource> resources) {
        this.resources = resources;
    }
    
    public void addResource(ReadyApiResource resource) {
        this.resources.add(resource);
    }
    
    @Override
    public String toString() {
        return "ReadyApiInterface{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", endpoints=" + endpoints +
                ", resources=" + resources.size() +
                '}';
    }
} 