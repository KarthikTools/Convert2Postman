package com.readyapi.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a ReadyAPI test suite.
 */
public class ReadyApiTestSuite {
    private String id;
    private String name;
    private String runType;
    private Map<String, String> properties;
    private List<ReadyApiTestCase> testCases;
    
    public ReadyApiTestSuite() {
        this.properties = new HashMap<>();
        this.testCases = new ArrayList<>();
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
    
    public String getRunType() {
        return runType;
    }
    
    public void setRunType(String runType) {
        this.runType = runType;
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
    
    public List<ReadyApiTestCase> getTestCases() {
        return testCases;
    }
    
    public void setTestCases(List<ReadyApiTestCase> testCases) {
        this.testCases = testCases;
    }
    
    public void addTestCase(ReadyApiTestCase testCase) {
        testCases.add(testCase);
    }
    
    @Override
    public String toString() {
        return "ReadyApiTestSuite{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", runType='" + runType + '\'' +
                ", properties=" + properties.size() +
                ", testCases=" + testCases.size() +
                '}';
    }
} 