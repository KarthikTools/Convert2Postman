package com.readyapi.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a ReadyAPI project containing test cases.
 */
public class ReadyApiProject {
    private String id;
    private String name;
    private String description;
    private String filePath;
    private Map<String, String> properties;
    private List<ReadyApiInterface> interfaces;
    private List<ReadyApiTestSuite> testSuites;
    private List<ReadyApiScriptLibrary> scriptLibraries;
    private List<String> projectReferences;
    private List<ReadyApiProject> referencedProjects;
    private List<ReadyApiTestCase> testCases;
    private List<ReadyApiTestCase> directTestCases;
    
    public ReadyApiProject() {
        this.properties = new HashMap<>();
        this.interfaces = new ArrayList<>();
        this.testSuites = new ArrayList<>();
        this.scriptLibraries = new ArrayList<>();
        this.projectReferences = new ArrayList<>();
        this.referencedProjects = new ArrayList<>();
        this.testCases = new ArrayList<>();
        this.directTestCases = new ArrayList<>();
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
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
    
    public List<String> getProjectReferences() {
        return projectReferences;
    }
    
    public void setProjectReferences(List<String> projectReferences) {
        this.projectReferences = projectReferences;
    }
    
    public void addProjectReference(String reference) {
        this.projectReferences.add(reference);
    }
    
    public List<ReadyApiProject> getReferencedProjects() {
        return referencedProjects;
    }
    
    public void setReferencedProjects(List<ReadyApiProject> referencedProjects) {
        this.referencedProjects = referencedProjects;
    }
    
    public void addReferencedProject(ReadyApiProject referencedProject) {
        this.referencedProjects.add(referencedProject);
    }
    
    public List<ReadyApiTestCase> getTestCases() {
        return testCases;
    }
    
    public void setTestCases(List<ReadyApiTestCase> testCases) {
        this.testCases = testCases;
    }
    
    public void addTestCase(ReadyApiTestCase testCase) {
        this.testCases.add(testCase);
    }
    
    public List<ReadyApiTestCase> getDirectTestCases() {
        return directTestCases;
    }
    
    public void addDirectTestCase(ReadyApiTestCase testCase) {
        this.directTestCases.add(testCase);
    }
    
    public List<ReadyApiInterface> getAllInterfaces() {
        List<ReadyApiInterface> allInterfaces = new ArrayList<>(interfaces);
        for (ReadyApiProject referencedProject : referencedProjects) {
            allInterfaces.addAll(referencedProject.getInterfaces());
        }
        return allInterfaces;
    }
    
    public List<ReadyApiTestSuite> getAllTestSuites() {
        List<ReadyApiTestSuite> allTestSuites = new ArrayList<>(testSuites);
        for (ReadyApiProject referencedProject : referencedProjects) {
            allTestSuites.addAll(referencedProject.getTestSuites());
        }
        return allTestSuites;
    }
    
    public List<ReadyApiScriptLibrary> getAllScriptLibraries() {
        List<ReadyApiScriptLibrary> allScriptLibraries = new ArrayList<>(scriptLibraries);
        for (ReadyApiProject referencedProject : referencedProjects) {
            allScriptLibraries.addAll(referencedProject.getScriptLibraries());
        }
        return allScriptLibraries;
    }
    
    public Map<String, String> getAllProperties() {
        Map<String, String> allProperties = new HashMap<>(properties);
        for (ReadyApiProject referencedProject : referencedProjects) {
            allProperties.putAll(referencedProject.getProperties());
        }
        return allProperties;
    }
    
    @Override
    public String toString() {
        return "ReadyApiProject{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", interfaces=" + interfaces.size() +
                ", testSuites=" + testSuites.size() +
                ", scriptLibraries=" + scriptLibraries.size() +
                ", referencedProjects=" + referencedProjects.size() +
                ", testCases=" + testCases.size() +
                '}';
    }
} 