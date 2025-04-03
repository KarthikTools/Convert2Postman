package com.readyapi.converter.postman;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Postman script (pre-request or test).
 */
public class PostmanScript {
    private String type;
    private List<String> exec;
    
    public PostmanScript() {
        this.type = "text/javascript";
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
    
    public void addExec(String exec) {
        this.exec.add(exec);
    }
} 