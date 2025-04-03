package com.readyapi.converter.postman;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Postman URL.
 */
public class PostmanUrl {
    private String raw;
    private String protocol;
    private List<String> host;
    private List<String> path;
    private List<PostmanQueryParam> query;
    
    public PostmanUrl() {
        this.host = new ArrayList<>();
        this.path = new ArrayList<>();
        this.query = new ArrayList<>();
    }
    
    public String getRaw() {
        return raw;
    }
    
    public void setRaw(String raw) {
        this.raw = raw;
    }
    
    public String getProtocol() {
        return protocol;
    }
    
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    
    public List<String> getHost() {
        return host;
    }
    
    public void setHost(List<String> host) {
        this.host = host;
    }
    
    public void addHost(String host) {
        this.host.add(host);
    }
    
    public List<String> getPath() {
        return path;
    }
    
    public void setPath(List<String> path) {
        this.path = path;
    }
    
    public void addPath(String path) {
        this.path.add(path);
    }
    
    public List<PostmanQueryParam> getQuery() {
        return query;
    }
    
    public void setQuery(List<PostmanQueryParam> query) {
        this.query = query;
    }
    
    public void addQuery(PostmanQueryParam query) {
        this.query.add(query);
    }
    
    /**
     * Parse a URL string into Postman URL components.
     * 
     * @param url The URL string to parse
     * @return A PostmanUrl object with parsed components
     */
    public static PostmanUrl parse(String url) {
        PostmanUrl postmanUrl = new PostmanUrl();
        postmanUrl.setRaw(url);
        
        try {
            java.net.URL parsedUrl = new java.net.URL(url);
            postmanUrl.setProtocol(parsedUrl.getProtocol());
            
            String host = parsedUrl.getHost();
            if (host != null && !host.isEmpty()) {
                postmanUrl.addHost(host);
            }
            
            String path = parsedUrl.getPath();
            if (path != null && !path.isEmpty()) {
                String[] pathParts = path.split("/");
                for (String part : pathParts) {
                    if (!part.isEmpty()) {
                        postmanUrl.addPath(part);
                    }
                }
            }
            
            String query = parsedUrl.getQuery();
            if (query != null && !query.isEmpty()) {
                String[] queryParts = query.split("&");
                for (String part : queryParts) {
                    String[] keyValue = part.split("=");
                    if (keyValue.length == 2) {
                        postmanUrl.addQuery(new PostmanQueryParam(keyValue[0], keyValue[1]));
                    }
                }
            }
        } catch (Exception e) {
            // If URL parsing fails, just set the raw URL
            postmanUrl.setRaw(url);
        }
        
        return postmanUrl;
    }
    
    /**
     * Represents a query parameter in a Postman URL.
     */
    public static class PostmanQueryParam {
        private String key;
        private String value;
        private boolean disabled;
        
        public PostmanQueryParam(String key, String value) {
            this.key = key;
            this.value = value;
        }
        
        public String getKey() {
            return key;
        }
        
        public void setKey(String key) {
            this.key = key;
        }
        
        public String getValue() {
            return value;
        }
        
        public void setValue(String value) {
            this.value = value;
        }
        
        public boolean isDisabled() {
            return disabled;
        }
        
        public void setDisabled(boolean disabled) {
            this.disabled = disabled;
        }
    }
} 