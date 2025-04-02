package com.readyapi.converter;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a ReadyAPI resource (endpoint).
 */
public class ReadyApiResource {
    private String id;
    private String name;
    private String path;
    private List<ReadyApiMethod> methods = new ArrayList<>();
    
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
                ", methods=" + methods.size() +
                '}';
    }
} 