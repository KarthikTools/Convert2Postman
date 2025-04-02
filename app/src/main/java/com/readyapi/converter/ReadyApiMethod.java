package com.readyapi.converter;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a ReadyAPI method (HTTP method).
 */
public class ReadyApiMethod {
    private String id;
    private String name;
    private String httpMethod;
    private List<ReadyApiRequest> requests = new ArrayList<>();
    
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
    
    public String getHttpMethod() {
        return httpMethod;
    }
    
    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }
    
    public List<ReadyApiRequest> getRequests() {
        return requests;
    }
    
    public void setRequests(List<ReadyApiRequest> requests) {
        this.requests = requests;
    }
    
    public void addRequest(ReadyApiRequest request) {
        this.requests.add(request);
    }
    
    @Override
    public String toString() {
        return "ReadyApiMethod{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", httpMethod='" + httpMethod + '\'' +
                ", requests=" + requests.size() +
                '}';
    }
} 