package com.readyapi.converter.postman;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Postman request
 */
public class PostmanRequest {
    private String method;
    private List<PostmanHeader> header;
    private PostmanBody body;
    private PostmanUrl url;
    private String description;
    
    public PostmanRequest() {
        this.header = new ArrayList<>();
    }
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
    
    public List<PostmanHeader> getHeader() {
        return header;
    }
    
    public void setHeader(List<PostmanHeader> header) {
        this.header = header;
    }
    
    public void addHeader(PostmanHeader header) {
        this.header.add(header);
    }
    
    public PostmanBody getBody() {
        return body;
    }
    
    public void setBody(PostmanBody body) {
        this.body = body;
    }
    
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
     * Represents a Postman URL with query parameters
     */
    public static class PostmanUrl {
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
        
        public List<String> getPath() {
            return path;
        }
        
        public void setPath(List<String> path) {
            this.path = path;
        }
        
        public List<PostmanQueryParam> getQuery() {
            return query;
        }
        
        public void setQuery(List<PostmanQueryParam> query) {
            this.query = query;
        }
        
        /**
         * Represents a query parameter in a Postman URL
         */
        public static class PostmanQueryParam {
            private String key;
            private String value;
            private boolean disabled;
            
            public PostmanQueryParam() {
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
    
    /**
     * Represents a header in a Postman request
     */
    public static class PostmanHeader {
        private String key;
        private String value;
        private String type;
        
        public PostmanHeader() {
        }
        
        public PostmanHeader(String key, String value) {
            this.key = key;
            this.value = value;
            this.type = "text";
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
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
    }
    
    /**
     * Represents a request body in Postman
     */
    public static class PostmanBody {
        private String mode;
        private String raw;
        private PostmanBodyOptions options;
        
        public PostmanBody() {
        }
        
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
        
        public PostmanBodyOptions getOptions() {
            return options;
        }
        
        public void setOptions(PostmanBodyOptions options) {
            this.options = options;
        }
        
        /**
         * Options for the request body
         */
        public static class PostmanBodyOptions {
            private PostmanRawOptions raw;
            
            public PostmanBodyOptions() {
            }
            
            public PostmanRawOptions getRaw() {
                return raw;
            }
            
            public void setRaw(PostmanRawOptions raw) {
                this.raw = raw;
            }
            
            /**
             * Options for raw request body
             */
            public static class PostmanRawOptions {
                private String language;
                
                public PostmanRawOptions() {
                }
                
                public String getLanguage() {
                    return language;
                }
                
                public void setLanguage(String language) {
                    this.language = language;
                }
            }
        }
    }
}
