package com.readyapi.converter.postman;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Postman event (pre-request script or test).
 */
public class PostmanEvent {
    private String listen;
    private PostmanScript script;
    
    public PostmanEvent() {
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
        PostmanScript script = new PostmanScript();
        script.setType("text/javascript");
        script.setExec(new ArrayList<>());
        script.getExec().add(scriptContent);
        event.setScript(script);
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
        PostmanScript script = new PostmanScript();
        script.setType("text/javascript");
        script.setExec(new ArrayList<>());
        script.getExec().add(scriptContent);
        event.setScript(script);
        return event;
    }
    
    /**
     * Represents a Postman script.
     */
    public static class PostmanScript {
        private String type;
        private List<String> exec;
        
        public PostmanScript() {
            this.exec = new ArrayList<>();
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public List<String> getExec() {
            return exec;
        }
        
        public void setExec(List<String> exec) {
            this.exec = exec;
        }
        
        public void addExec(String script) {
            this.exec.add(script);
        }
    }
} 