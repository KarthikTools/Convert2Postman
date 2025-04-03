package com.readyapi.converter;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Postman event (pre-request script or test).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostmanEvent {
    private String listen;
    private PostmanScript script;
    
    public PostmanEvent() {
    }
    
    public PostmanEvent(String listen, String scriptContent) {
        this.listen = listen;
        this.script = new PostmanScript();
        this.script.setType("text/javascript");
        if (scriptContent != null && !scriptContent.isEmpty()) {
            this.script.addExec(scriptContent);
        }
    }
    
    public String getListen() {
        return listen;
    }
    
    public void setListen(String listen) {
        this.listen = listen;
    }
    
    public PostmanScript getScript() {
        return script;
    }
    
    public void setScript(PostmanScript script) {
        this.script = script;
    }
    
    /**
     * Create a pre-request script event.
     * 
     * @param scriptContent The JavaScript content for the pre-request script
     * @return A new PostmanEvent configured as a pre-request script
     */
    public static PostmanEvent createPreRequestScript(String scriptContent) {
        PostmanEvent event = new PostmanEvent();
        event.setListen("prerequest");
        PostmanScript postmanScript = new PostmanScript();
        postmanScript.setType("text/javascript");
        postmanScript.setExec(new ArrayList<>());
        postmanScript.getExec().add(scriptContent);
        event.setScript(postmanScript);
        return event;
    }
    
    /**
     * Create a test script event.
     * 
     * @param scriptContent The JavaScript content for the test script
     * @return A new PostmanEvent configured as a test script
     */
    public static PostmanEvent createTest(String scriptContent) {
        PostmanEvent event = new PostmanEvent();
        event.setListen("test");
        PostmanScript postmanScript = new PostmanScript();
        postmanScript.setType("text/javascript");
        postmanScript.setExec(new ArrayList<>());
        postmanScript.getExec().add(scriptContent);
        event.setScript(postmanScript);
        return event;
    }
    
    /**
     * Add a line to the script content.
     * 
     * @param line The line to add
     */
    public void addScriptLine(String line) {
        if (script == null) {
            script = new PostmanScript();
            script.setType("text/javascript");
        }
        script.addExec(line);
    }
    
    /**
     * Nested class to represent a Postman script.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PostmanScript {
        private List<String> exec = new ArrayList<>();
        private String type;
        
        public List<String> getExec() {
            return exec;
        }
        
        public void setExec(List<String> exec) {
            this.exec = exec;
        }
        
        public void addExec(String line) {
            // Split the line by newlines to ensure proper formatting
            String[] lines = line.split("\\r?\\n");
            for (String l : lines) {
                this.exec.add(l);
            }
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
    }
} 