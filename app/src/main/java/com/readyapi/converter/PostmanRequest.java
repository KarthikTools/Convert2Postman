package com.readyapi.converter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Postman request.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostmanRequest {
    private String method;
    private List<PostmanHeader> header;
    private PostmanBody body;
    private PostmanUrl url;
    private String description;
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
    
    @JsonProperty("header")
    public List<PostmanHeader> getHeader() {
        return header;
    }
    
    public void setHeader(List<PostmanHeader> header) {
        this.header = header;
    }
    
    public void addHeader(PostmanHeader header) {
        if (this.header == null) {
            this.header = new ArrayList<>();
        }
        this.header.add(header);
    }
    
    public void addHeader(String key, String value) {
        if (this.header == null) {
            this.header = new ArrayList<>();
        }
        PostmanHeader header = new PostmanHeader();
        header.setKey(key);
        header.setValue(value);
        this.header.add(header);
    }
    
    @JsonProperty("body")
    public PostmanBody getBody() {
        return body;
    }
    
    public void setBody(PostmanBody body) {
        this.body = body;
    }
    
    @JsonProperty("url")
    public PostmanUrl getUrl() {
        return url;
    }
    
    public void setUrl(PostmanUrl url) {
        this.url = url;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Find a header by key.
     * 
     * @param key The header key to find
     * @return The matching header or null if not found
     */
    public PostmanHeader findHeader(String key) {
        if (this.header == null) {
            return null;
        }
        
        for (PostmanHeader h : this.header) {
            if (key.equalsIgnoreCase(h.getKey())) {
                return h;
            }
        }
        
        return null;
    }
    
    /**
     * Nested class to represent a Postman header.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PostmanHeader {
        private String key;
        private String value;
        private String description;
        private String type;
        private boolean disabled;
        
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
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public boolean isDisabled() {
            return disabled;
        }
        
        public void setDisabled(boolean disabled) {
            this.disabled = disabled;
        }
    }
    
    /**
     * Nested class to represent a Postman body.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PostmanBody {
        private String mode;
        private String raw;
        private PostmanBodyOptions options;
        
        public String getMode() {
            return mode;
        }
        
        public void setMode(String mode) {
            this.mode = mode;
        }
        
        public String getRaw() {
            return raw;
        }
        
        public void setRaw(String raw) {
            this.raw = raw;
        }
        
        @JsonProperty("options")
        public PostmanBodyOptions getOptions() {
            return options;
        }
        
        public void setOptions(PostmanBodyOptions options) {
            this.options = options;
        }
        
        /**
         * Nested class to represent Postman body options.
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class PostmanBodyOptions {
            private PostmanRawOptions raw;
            
            @JsonProperty("raw")
            public PostmanRawOptions getRaw() {
                return raw;
            }
            
            public void setRaw(PostmanRawOptions raw) {
                this.raw = raw;
            }
            
            /**
             * Nested class to represent Postman raw body options.
             */
            @JsonInclude(JsonInclude.Include.NON_NULL)
            public static class PostmanRawOptions {
                private String language;
                
                public String getLanguage() {
                    return language;
                }
                
                public void setLanguage(String language) {
                    this.language = language;
                }
            }
        }
    }
    
    /**
     * Nested class to represent a Postman URL.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PostmanUrl {
        private String raw;
        private String protocol;
        private List<String> host;
        private List<String> path;
        private List<PostmanQueryParam> query;
        
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
        
        @JsonProperty("host")
        public List<String> getHost() {
            return host;
        }
        
        public void setHost(List<String> host) {
            this.host = host;
        }
        
        @JsonProperty("path")
        public List<String> getPath() {
            return path;
        }
        
        public void setPath(List<String> path) {
            this.path = path;
        }
        
        @JsonProperty("query")
        public List<PostmanQueryParam> getQuery() {
            return query;
        }
        
        public void setQuery(List<PostmanQueryParam> query) {
            this.query = query;
        }
        
        public void addQueryParam(String key, String value) {
            if (this.query == null) {
                this.query = new ArrayList<>();
            }
            PostmanQueryParam param = new PostmanQueryParam();
            param.setKey(key);
            param.setValue(value);
            this.query.add(param);
        }
        
        /**
         * Parse a URL string into Postman URL components.
         * 
         * @param urlString The URL string to parse
         * @return A PostmanUrl object with parsed components
         */
        public static PostmanUrl parse(String urlString) {
            PostmanUrl url = new PostmanUrl();
            url.setRaw(urlString);
            
            try {
                java.net.URI uri = java.net.URI.create(urlString);
                
                // Set protocol
                url.setProtocol(uri.getScheme());
                
                // Set host
                String[] hostParts = uri.getHost().split("\\.");
                url.setHost(java.util.Arrays.asList(hostParts));
                
                // Set path
                String path = uri.getPath();
                if (path != null && !path.isEmpty()) {
                    if (path.startsWith("/")) {
                        path = path.substring(1);
                    }
                    url.setPath(java.util.Arrays.asList(path.split("/")));
                }
                
                // Set query params
                String query = uri.getQuery();
                if (query != null && !query.isEmpty()) {
                    List<PostmanQueryParam> queryParams = new ArrayList<>();
                    String[] params = query.split("&");
                    for (String param : params) {
                        String[] parts = param.split("=", 2);
                        PostmanQueryParam queryParam = new PostmanQueryParam();
                        queryParam.setKey(parts[0]);
                        queryParam.setValue(parts.length > 1 ? parts[1] : "");
                        queryParams.add(queryParam);
                    }
                    url.setQuery(queryParams);
                }
            } catch (Exception e) {
                // If URI parsing fails, we'll just set the raw URL
            }
            
            return url;
        }
        
        /**
         * Nested class to represent a Postman query parameter.
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class PostmanQueryParam {
            private String key;
            private String value;
            private boolean disabled;
            
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
} 