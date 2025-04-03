package com.readyapi.converter;

import java.util.ArrayList;
import java.util.List;

public class ReadyApiOperation {
    private String name;
    private String method;
    private String endpoint;
    private List<ReadyApiHeader> headers;
    private String requestBody;

    public ReadyApiOperation() {
        this.headers = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public List<ReadyApiHeader> getHeaders() {
        return headers;
    }

    public void setHeaders(List<ReadyApiHeader> headers) {
        this.headers = headers;
    }

    public void addHeader(ReadyApiHeader header) {
        this.headers.add(header);
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }
} 