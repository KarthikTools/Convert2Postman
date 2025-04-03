package com.readyapi.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReadyApiRestRequest extends ReadyApiTestStep {
    private String method;
    private String endpoint;
    private List<ReadyApiHeader> headers;
    private String requestBody;
    private List<ReadyApiAssertion> assertions;

    public ReadyApiRestRequest() {
        super();
        this.headers = new ArrayList<>();
        this.assertions = new ArrayList<>();
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers.stream()
                .collect(Collectors.toMap(
                    ReadyApiHeader::getKey,
                    ReadyApiHeader::getValue,
                    (existing, replacement) -> existing,
                    HashMap::new
                ));
    }

    public List<ReadyApiHeader> getHeaderList() {
        return headers;
    }

    public void setHeaderList(List<ReadyApiHeader> headers) {
        this.headers = headers;
    }

    public void addHeader(ReadyApiHeader header) {
        this.headers.add(header);
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

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
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
} 