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
    private String endpoint;
    private String method;
    private String mediaType;
    private String requestBody;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> queryParams = new HashMap<>();
    private Map<String, String> authSettings = new HashMap<>();
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
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
    
    public String getMediaType() {
        return mediaType;
    }
    
    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }
    
    public String getRequestBody() {
        return requestBody;
    }
    
    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }
    
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
    
    public void addHeader(String name, String value) {
        this.headers.put(name, value);
    }
    
    public Map<String, String> getQueryParams() {
        return queryParams;
    }
    
    public void setQueryParams(Map<String, String> queryParams) {
        this.queryParams = queryParams;
    }
    
    public void addQueryParam(String name, String value) {
        this.queryParams.put(name, value);
    }
    
    public Map<String, String> getAuthSettings() {
        return authSettings;
    }
    
    public void setAuthSettings(Map<String, String> authSettings) {
        this.authSettings = authSettings;
    }
    
    public void addAuthSetting(String name, String value) {
        this.authSettings.put(name, value);
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
    
    public Map<String, String> getRequestHeaders() {
        return headers;
    }
    
    public void addRequestHeader(String name, String value) {
        this.headers.put(name, value);
    }
    
    public String getBody() {
        return requestBody;
    }
    
    public void setBody(String body) {
        this.requestBody = body;
    }
    
    public String getContentType() {
        return mediaType;
    }
    
    /**
     * Parse authentication settings from XML element.
     * 
     * @param requestElement The request XML element
     */
    public void parseAuthentication(org.dom4j.Element requestElement) {
        org.dom4j.Element authElement = requestElement.element("authentication");
        if (authElement != null) {
            String authType = authElement.attributeValue("type");
            
            if (authType != null) {
                addAuthSetting("type", authType);
                
                // Handle different authentication types
                if ("Basic".equalsIgnoreCase(authType)) {
                    parseBasicAuth(authElement);
                } else if ("OAuth 2.0".equalsIgnoreCase(authType) || "OAuth2".equalsIgnoreCase(authType)) {
                    parseOAuth2Auth(authElement);
                } else if ("Bearer".equalsIgnoreCase(authType)) {
                    parseBearerAuth(authElement);
                } else if ("ApiKey".equalsIgnoreCase(authType)) {
                    parseApiKeyAuth(authElement);
                } else if ("NTLM".equalsIgnoreCase(authType)) {
                    parseNTLMAuth(authElement);
                } else if ("Kerberos".equalsIgnoreCase(authType)) {
                    parseKerberosAuth(authElement);
                } else if ("WSS".equalsIgnoreCase(authType)) {
                    parseWSSAuth(authElement);
                }
            }
        }
    }
    
    /**
     * Parse Basic authentication settings.
     * 
     * @param authElement The authentication XML element
     */
    private void parseBasicAuth(org.dom4j.Element authElement) {
        org.dom4j.Element basicElement = authElement.element("basic");
        if (basicElement != null) {
            String username = basicElement.elementText("username");
            String password = basicElement.elementText("password");
            
            if (username != null) {
                addAuthSetting("username", username);
            }
            
            if (password != null) {
                addAuthSetting("password", password);
            }
        }
    }
    
    /**
     * Parse OAuth 2.0 authentication settings.
     * 
     * @param authElement The authentication XML element
     */
    private void parseOAuth2Auth(org.dom4j.Element authElement) {
        org.dom4j.Element oauth2Element = authElement.element("oauth2");
        if (oauth2Element != null) {
            // Extract OAuth 2.0 settings
            String accessToken = oauth2Element.elementText("accessToken");
            String tokenUrl = oauth2Element.elementText("tokenUrl");
            String authUrl = oauth2Element.elementText("authorizationUrl");
            String clientId = oauth2Element.elementText("clientId");
            String clientSecret = oauth2Element.elementText("clientSecret");
            String scope = oauth2Element.elementText("scope");
            String grantType = oauth2Element.elementText("grantType");
            
            // Store OAuth 2.0 settings
            if (accessToken != null) addAuthSetting("accessToken", accessToken);
            if (tokenUrl != null) addAuthSetting("tokenUrl", tokenUrl);
            if (authUrl != null) addAuthSetting("authUrl", authUrl);
            if (clientId != null) addAuthSetting("clientId", clientId);
            if (clientSecret != null) addAuthSetting("clientSecret", clientSecret);
            if (scope != null) addAuthSetting("scope", scope);
            if (grantType != null) addAuthSetting("grantType", grantType);
        }
    }
    
    /**
     * Parse Bearer token authentication settings.
     * 
     * @param authElement The authentication XML element
     */
    private void parseBearerAuth(org.dom4j.Element authElement) {
        org.dom4j.Element bearerElement = authElement.element("bearer");
        if (bearerElement != null) {
            String token = bearerElement.elementText("token");
            
            if (token != null) {
                addAuthSetting("token", token);
            }
        }
    }
    
    /**
     * Parse API Key authentication settings.
     * 
     * @param authElement The authentication XML element
     */
    private void parseApiKeyAuth(org.dom4j.Element authElement) {
        org.dom4j.Element apiKeyElement = authElement.element("apiKey");
        if (apiKeyElement != null) {
            String key = apiKeyElement.elementText("key");
            String value = apiKeyElement.elementText("value");
            String location = apiKeyElement.elementText("location"); // header, query, etc.
            
            if (key != null) addAuthSetting("key", key);
            if (value != null) addAuthSetting("value", value);
            if (location != null) addAuthSetting("in", location);
        }
    }
    
    /**
     * Parse NTLM authentication settings.
     * 
     * @param authElement The authentication XML element
     */
    private void parseNTLMAuth(org.dom4j.Element authElement) {
        org.dom4j.Element ntlmElement = authElement.element("ntlm");
        if (ntlmElement != null) {
            String username = ntlmElement.elementText("username");
            String password = ntlmElement.elementText("password");
            String domain = ntlmElement.elementText("domain");
            String workstation = ntlmElement.elementText("workstation");
            
            if (username != null) addAuthSetting("username", username);
            if (password != null) addAuthSetting("password", password);
            if (domain != null) addAuthSetting("domain", domain);
            if (workstation != null) addAuthSetting("workstation", workstation);
        }
    }
    
    /**
     * Parse Kerberos authentication settings.
     * 
     * @param authElement The authentication XML element
     */
    private void parseKerberosAuth(org.dom4j.Element authElement) {
        org.dom4j.Element kerberosElement = authElement.element("kerberos");
        if (kerberosElement != null) {
            String username = kerberosElement.elementText("username");
            String password = kerberosElement.elementText("password");
            String realm = kerberosElement.elementText("realm");
            
            if (username != null) addAuthSetting("username", username);
            if (password != null) addAuthSetting("password", password);
            if (realm != null) addAuthSetting("realm", realm);
        }
    }
    
    /**
     * Parse WS-Security authentication settings.
     * 
     * @param authElement The authentication XML element
     */
    private void parseWSSAuth(org.dom4j.Element authElement) {
        org.dom4j.Element wssElement = authElement.element("wss");
        if (wssElement != null) {
            String username = wssElement.elementText("username");
            String password = wssElement.elementText("password");
            String passwordType = wssElement.elementText("passwordType");
            String timeToLive = wssElement.elementText("timeToLive");
            
            if (username != null) addAuthSetting("username", username);
            if (password != null) addAuthSetting("password", password);
            if (passwordType != null) addAuthSetting("passwordType", passwordType);
            if (timeToLive != null) addAuthSetting("timeToLive", timeToLive);
        }
    }
    
    /**
     * Parse client certificate settings.
     * 
     * @param requestElement The request XML element
     */
    public void parseClientCertificate(org.dom4j.Element requestElement) {
        org.dom4j.Element certElement = requestElement.element("clientCertificate");
        if (certElement != null) {
            String certFile = certElement.elementText("file");
            String password = certElement.elementText("password");
            
            if (certFile != null) {
                addAuthSetting("certFile", certFile);
            }
            
            if (password != null) {
                addAuthSetting("certPassword", password);
            }
        }
    }
    
    /**
     * Parse SOAP action header.
     * 
     * @param requestElement The request XML element
     */
    public void parseSoapAction(org.dom4j.Element requestElement) {
        org.dom4j.Element soapElement = requestElement.element("soapAction");
        if (soapElement != null) {
            String soapAction = soapElement.getTextTrim();
            
            if (soapAction != null && !soapAction.isEmpty()) {
                addHeader("SOAPAction", soapAction);
            }
        }
    }
}
