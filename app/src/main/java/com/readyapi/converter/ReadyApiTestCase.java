package com.readyapi.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a ReadyAPI test case.
 */
public class ReadyApiTestCase {
    private String id;
    private String name;
    private Map<String, String> properties = new HashMap<>();
    private List<ReadyApiTestStep> testSteps = new ArrayList<>();
    
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
    
    public Map<String, String> getProperties() {
        return properties;
    }
    
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
    
    public void addProperty(String name, String value) {
        this.properties.put(name, value);
    }
    
    public String getProperty(String name) {
        return this.properties.get(name);
    }
    
    public List<ReadyApiTestStep> getTestSteps() {
        return testSteps;
    }
    
    public void setTestSteps(List<ReadyApiTestStep> testSteps) {
        this.testSteps = testSteps;
    }
    
    public void addTestStep(ReadyApiTestStep testStep) {
        this.testSteps.add(testStep);
    }
    
    @Override
    public String toString() {
        return "ReadyApiTestCase{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", properties=" + properties.size() +
                ", testSteps=" + testSteps.size() +
                '}';
    }
} 