package com.readyapi.converter;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a ReadyAPI resource.
 */
public class ReadyApiResource {
    private String id;
    private String name;
    private String path;
    private String description;
    private List<ReadyApiMethod> methods;
    
    public ReadyApiResource() {
        this.methods = new ArrayList<>();
    }
    
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
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<ReadyApiMethod> getMethods() {
        return methods;
    }
    
    public void setMethods(List<ReadyApiMethod> methods) {
        this.methods = methods;
    }
    
    public void addMethod(ReadyApiMethod method) {
        this.methods.add(method);
    }

    @Override
    public String toString() {
        return "ReadyApiResource{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", description='" + description + '\'' +
                ", methods=" + methods.size() +
                '}';
    }
} 