package com.readyapi.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a ReadyAPI project.
 */
public class ReadyApiProject {
    private String id;
    private String name;
    private String filePath;
    private Map<String, String> properties = new HashMap<>();
    private List<ReadyApiInterface> interfaces = new ArrayList<>();
    private List<ReadyApiTestSuite> testSuites = new ArrayList<>();
    private List<ReadyApiScriptLibrary> scriptLibraries = new ArrayList<>();
    private List<String> projectReferences = new ArrayList<>();
    private List<ReadyApiProject> referencedProjects = new ArrayList<>();
    private List<ReadyApiTestCase> directTestCases = new ArrayList<>();
    
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
    
    public List<ReadyApiTestCase> getDirectTestCases() {
        return directTestCases;
    }
    
    public void setDirectTestCases(List<ReadyApiTestCase> directTestCases) {
        this.directTestCases = directTestCases;
    }
    
    public void addDirectTestCase(ReadyApiTestCase testCase) {
        this.directTestCases.add(testCase);
    }
    
    /**
     * Get all interfaces including those from referenced projects.
     * 
     * @return List of all interfaces 
     */
    public List<ReadyApiInterface> getAllInterfaces() {
        List<ReadyApiInterface> allInterfaces = new ArrayList<>(interfaces);
        
        // Add interfaces from referenced projects
        for (ReadyApiProject referencedProject : referencedProjects) {
            allInterfaces.addAll(referencedProject.getInterfaces());
        }
        
        return allInterfaces;
    }
    
    /**
     * Get all test suites including those from referenced projects.
     * 
     * @return List of all test suites
     */
    public List<ReadyApiTestSuite> getAllTestSuites() {
        List<ReadyApiTestSuite> allTestSuites = new ArrayList<>(testSuites);
        
        // Add test suites from referenced projects
        for (ReadyApiProject referencedProject : referencedProjects) {
            allTestSuites.addAll(referencedProject.getTestSuites());
        }
        
        return allTestSuites;
    }
    
    /**
     * Get all script libraries including those from referenced projects.
     * 
     * @return List of all script libraries
     */
    public List<ReadyApiScriptLibrary> getAllScriptLibraries() {
        List<ReadyApiScriptLibrary> allScriptLibraries = new ArrayList<>(scriptLibraries);
        
        // Add script libraries from referenced projects
        for (ReadyApiProject referencedProject : referencedProjects) {
            allScriptLibraries.addAll(referencedProject.getScriptLibraries());
        }
        
        return allScriptLibraries;
    }
    
    /**
     * Get all properties including those from referenced projects.
     * 
     * @return Map of all properties
     */
    public Map<String, String> getAllProperties() {
        Map<String, String> allProperties = new HashMap<>(properties);
        
        // Add properties from referenced projects
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
                ", interfaces=" + interfaces.size() +
                ", testSuites=" + testSuites.size() +
                ", scriptLibraries=" + scriptLibraries.size() +
                ", referencedProjects=" + referencedProjects.size() +
                '}';
    }
} 