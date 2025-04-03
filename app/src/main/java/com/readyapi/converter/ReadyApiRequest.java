package com.readyapi.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a request in a ReadyAPI test step
 */
public class ReadyApiRequest {
    private String method;
    private String endpoint;
    private Map<String, String> headers;
    private String body;
    private String mediaType;
    private String id;
    private String name;
    private String requestBody;
    private String setupScript;
    private String teardownScript;
    private String authType;
    private String username;
    private String password;
    private String bearerToken;
    private String oAuthAccessToken;
    private String oAuthRefreshToken;
    private Map<String, String> parameters;
    private Map<String, String> queryParams;
    private Map<String, String> authSettings;
    private List<ReadyApiAssertion> assertions;
    
    public ReadyApiRequest() {
        this.headers = new HashMap<>();
        this.parameters = new HashMap<>();
        this.queryParams = new HashMap<>();
        this.authSettings = new HashMap<>();
        this.assertions = new ArrayList<>();
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
    
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
    
    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }
    
    public String getBody() {
        return body;
    }
    
    public void setBody(String body) {
        this.body = body;
    }
    
    public String getMediaType() {
        return mediaType;
    }
    
    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
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
    
    public String getRequestBody() {
        return requestBody;
    }
    
    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }
    
    public String getSetupScript() {
        return setupScript;
    }
    
    public void setSetupScript(String setupScript) {
        this.setupScript = setupScript;
    }
    
    public String getTeardownScript() {
        return teardownScript;
    }
    
    public void setTeardownScript(String teardownScript) {
        this.teardownScript = teardownScript;
    }
    
    public String getAuthType() {
        return authType;
    }
    
    public void setAuthType(String authType) {
        this.authType = authType;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getBearerToken() {
        return bearerToken;
    }
    
    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
    }
    
    public String getOAuthAccessToken() {
        return oAuthAccessToken;
    }
    
    public void setOAuthAccessToken(String oAuthAccessToken) {
        this.oAuthAccessToken = oAuthAccessToken;
    }
    
    public String getOAuthRefreshToken() {
        return oAuthRefreshToken;
    }
    
    public void setOAuthRefreshToken(String oAuthRefreshToken) {
        this.oAuthRefreshToken = oAuthRefreshToken;
    }
    
    public Map<String, String> getParameters() {
        return parameters;
    }
    
    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
    
    public Map<String, String> getQueryParams() {
        return queryParams;
    }
    
    public void setQueryParams(Map<String, String> queryParams) {
        this.queryParams = queryParams;
    }
    
    public Map<String, String> getAuthSettings() {
        return authSettings;
    }
    
    public void setAuthSettings(Map<String, String> authSettings) {
        this.authSettings = authSettings;
    }
    
    public List<ReadyApiAssertion> getAssertions() {
        return assertions;
    }
    
    public void setAssertions(List<ReadyApiAssertion> assertions) {
        this.assertions = assertions;
    }
    
    public void addParameter(String key, String value) {
        parameters.put(key, value);
    }
    
    public void addQueryParam(String key, String value) {
        queryParams.put(key, value);
    }
    
    public void addAuthSetting(String key, String value) {
        authSettings.put(key, value);
    }
    
    public void addAssertion(ReadyApiAssertion assertion) {
        assertions.add(assertion);
    }
    
    public String getRequestContent() {
        return requestBody;
    }
    
    public void setRequestContent(String requestContent) {
        this.requestBody = requestContent;
    }
    
    public Map<String, String> getRequestHeaders() {
        return headers;
    }
    
    public String getContentType() {
        return headers.get("Content-Type");
    }
}
