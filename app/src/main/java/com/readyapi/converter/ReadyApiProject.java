package com.readyapi.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a ReadyAPI project structure.
 */
public class ReadyApiProject {
    private String id;
    private String name;
    private Map<String, String> properties = new HashMap<>();
    private List<ReadyApiInterface> interfaces = new ArrayList<>();
    private List<ReadyApiTestSuite> testSuites = new ArrayList<>();
    private List<ReadyApiScriptLibrary> scriptLibraries = new ArrayList<>();
    
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
    
    public List<ReadyApiInterface> getInterfaces() {
        return interfaces;
    }
    
    public void setInterfaces(List<ReadyApiInterface> interfaces) {
        this.interfaces = interfaces;
    }
    
    public void addInterface(ReadyApiInterface apiInterface) {
        this.interfaces.add(apiInterface);
    }
    
    public List<ReadyApiTestSuite> getTestSuites() {
        return testSuites;
    }
    
    public void setTestSuites(List<ReadyApiTestSuite> testSuites) {
        this.testSuites = testSuites;
    }
    
    public void addTestSuite(ReadyApiTestSuite testSuite) {
        this.testSuites.add(testSuite);
    }
    
    public List<ReadyApiScriptLibrary> getScriptLibraries() {
        return scriptLibraries;
    }
    
    public void setScriptLibraries(List<ReadyApiScriptLibrary> scriptLibraries) {
        this.scriptLibraries = scriptLibraries;
    }
    
    public void addScriptLibrary(ReadyApiScriptLibrary scriptLibrary) {
        this.scriptLibraries.add(scriptLibrary);
    }
    
    @Override
    public String toString() {
        return "ReadyApiProject{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", properties=" + properties.size() +
                ", interfaces=" + interfaces.size() +
                ", testSuites=" + testSuites.size() +
                ", scriptLibraries=" + scriptLibraries.size() +
                '}';
    }
} 