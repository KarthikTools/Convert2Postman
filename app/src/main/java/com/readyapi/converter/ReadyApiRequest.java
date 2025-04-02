package com.readyapi.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a ReadyAPI request.
 */
public class ReadyApiRequest {
    private String id;
    private String name;
    private String mediaType;
    private String endpoint;
    private String requestBody;
    private Map<String, String> requestHeaders = new HashMap<>();
    private Map<String, String> queryParameters = new HashMap<>();
    private Map<String, String> pathParameters = new HashMap<>();
    private List<ReadyApiAssertion> assertions = new ArrayList<>();
    
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
    
    public String getMediaType() {
        return mediaType;
    }
    
    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    
    public String getRequestBody() {
        return requestBody;
    }
    
    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }
    
    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }
    
    public void setRequestHeaders(Map<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }
    
    public void addRequestHeader(String name, String value) {
        this.requestHeaders.put(name, value);
    }
    
    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }
    
    public void setQueryParameters(Map<String, String> queryParameters) {
        this.queryParameters = queryParameters;
    }
    
    public void addQueryParameter(String name, String value) {
        this.queryParameters.put(name, value);
    }
    
    public Map<String, String> getPathParameters() {
        return pathParameters;
    }
    
    public void setPathParameters(Map<String, String> pathParameters) {
        this.pathParameters = pathParameters;
    }
    
    public void addPathParameter(String name, String value) {
        this.pathParameters.put(name, value);
    }
    
    public List<ReadyApiAssertion> getAssertions() {
        return assertions;
    }
    
    public void setAssertions(List<ReadyApiAssertion> assertions) {
        this.assertions = assertions;
    }
    
    public void addAssertion(ReadyApiAssertion assertion) {
        this.assertions.add(assertion);
    }
    
    @Override
    public String toString() {
        return "ReadyApiRequest{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", mediaType='" + mediaType + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", headers=" + requestHeaders.size() +
                ", queryParams=" + queryParameters.size() +
                ", pathParams=" + pathParameters.size() +
                ", assertions=" + assertions.size() +
                '}';
    }
} 