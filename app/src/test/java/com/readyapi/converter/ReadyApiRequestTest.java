package com.readyapi.converter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Test class for ReadyApiRequest functionality.
 * Provides test coverage for request parsing and authentication methods.
 */
public class ReadyApiRequestTest {
    
    private ReadyApiRequest request;
    
    @BeforeEach
    public void setUp() {
        request = new ReadyApiRequest();
        request.setName("Test Request");
        request.setEndpoint("https://api.example.com/test");
        request.setMethod("POST");
    }
    
    @Test
    @DisplayName("Test basic request properties")
    public void testBasicProperties() {
        assertEquals("Test Request", request.getName());
        assertEquals("https://api.example.com/test", request.getEndpoint());
        assertEquals("POST", request.getMethod());
    }
    
    @Test
    @DisplayName("Test headers management")
    public void testHeaders() {
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Accept", "application/json");
        
        Map<String, String> headers = request.getHeaders();
        assertEquals(2, headers.size());
        assertEquals("application/json", headers.get("Content-Type"));
        assertEquals("application/json", headers.get("Accept"));
    }
    
    @Test
    @DisplayName("Test query parameters")
    public void testQueryParams() {
        request.addQueryParam("param1", "value1");
        request.addQueryParam("param2", "value2");
        
        Map<String, String> params = request.getQueryParams();
        assertEquals(2, params.size());
        assertEquals("value1", params.get("param1"));
        assertEquals("value2", params.get("param2"));
    }
    
    @Test
    @DisplayName("Test basic authentication")
    public void testBasicAuth() {
        request.addAuthSetting("type", "Basic");
        request.addAuthSetting("username", "testuser");
        request.addAuthSetting("password", "testpass");
        
        Map<String, String> authSettings = request.getAuthSettings();
        assertEquals(3, authSettings.size());
        assertEquals("Basic", authSettings.get("type"));
        assertEquals("testuser", authSettings.get("username"));
        assertEquals("testpass", authSettings.get("password"));
    }
    
    @Test
    @DisplayName("Test OAuth2 authentication")
    public void testOAuth2Auth() {
        request.addAuthSetting("type", "OAuth 2.0");
        request.addAuthSetting("accessToken", "test-token");
        request.addAuthSetting("tokenUrl", "https://auth.example.com/token");
        request.addAuthSetting("clientId", "client123");
        request.addAuthSetting("clientSecret", "secret456");
        request.addAuthSetting("scope", "read write");
        
        Map<String, String> authSettings = request.getAuthSettings();
        assertEquals(6, authSettings.size());
        assertEquals("OAuth 2.0", authSettings.get("type"));
        assertEquals("test-token", authSettings.get("accessToken"));
        assertEquals("https://auth.example.com/token", authSettings.get("tokenUrl"));
        assertEquals("client123", authSettings.get("clientId"));
        assertEquals("secret456", authSettings.get("clientSecret"));
        assertEquals("read write", authSettings.get("scope"));
    }
    
    @Test
    @DisplayName("Test API Key authentication")
    public void testApiKeyAuth() {
        request.addAuthSetting("type", "ApiKey");
        request.addAuthSetting("key", "X-API-Key");
        request.addAuthSetting("value", "api-key-value");
        request.addAuthSetting("in", "header");
        
        Map<String, String> authSettings = request.getAuthSettings();
        assertEquals(4, authSettings.size());
        assertEquals("ApiKey", authSettings.get("type"));
        assertEquals("X-API-Key", authSettings.get("key"));
        assertEquals("api-key-value", authSettings.get("value"));
        assertEquals("header", authSettings.get("in"));
    }
    
    @Test
    @DisplayName("Test Bearer token authentication")
    public void testBearerAuth() {
        request.addAuthSetting("type", "Bearer");
        request.addAuthSetting("token", "bearer-token-value");
        
        Map<String, String> authSettings = request.getAuthSettings();
        assertEquals(2, authSettings.size());
        assertEquals("Bearer", authSettings.get("type"));
        assertEquals("bearer-token-value", authSettings.get("token"));
    }
    
    @Test
    @DisplayName("Test SOAP action header")
    public void testSoapAction() {
        request.addHeader("SOAPAction", "http://example.com/GetData");
        
        Map<String, String> headers = request.getHeaders();
        assertEquals(1, headers.size());
        assertEquals("http://example.com/GetData", headers.get("SOAPAction"));
    }
}
