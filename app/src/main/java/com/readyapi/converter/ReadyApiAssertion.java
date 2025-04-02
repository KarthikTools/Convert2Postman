package com.readyapi.converter;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a ReadyAPI assertion.
 */
public class ReadyApiAssertion {
    private String id;
    private String name;
    private String type;
    private Map<String, String> configuration = new HashMap<>();
    
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
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Map<String, String> getConfiguration() {
        return configuration;
    }
    
    public void setConfiguration(Map<String, String> configuration) {
        this.configuration = configuration;
    }
    
    public void addConfigurationProperty(String name, String value) {
        this.configuration.put(name, value);
    }
    
    public String getConfigurationProperty(String name) {
        return this.configuration.get(name);
    }
    
    /**
     * Convert the assertion to a Postman test script.
     * 
     * @return JavaScript code for Postman tests
     */
    public String toPostmanTest() {
        StringBuilder js = new StringBuilder();
        
        switch (type) {
            case "Valid HTTP Status Codes":
                String statusCodes = getConfigurationProperty("codes");
                if (statusCodes != null) {
                    String[] codes = statusCodes.split(",");
                    for (String code : codes) {
                        js.append("pm.test(\"Status code is ").append(code.trim()).append("\", function() {\n");
                        js.append("    pm.response.to.have.status(").append(code.trim()).append(");\n");
                        js.append("});\n");
                    }
                }
                break;
                
            case "Response SLA":
                String sla = getConfigurationProperty("SLA");
                if (sla != null) {
                    js.append("pm.test(\"Response time is less than ").append(sla).append("ms\", function() {\n");
                    js.append("    pm.expect(pm.response.responseTime).to.be.below(").append(sla).append(");\n");
                    js.append("});\n");
                }
                break;
                
            case "XPath Match":
                String path = getConfigurationProperty("path");
                String content = getConfigurationProperty("content");
                if (path != null && content != null) {
                    js.append("pm.test(\"XPath Match for ").append(path).append("\", function() {\n");
                    js.append("    const responseXml = xml2Json(pm.response.text());\n");
                    js.append("    // This is a simplified XPath. For complex XPath, you may need a proper XML parser library\n");
                    js.append("    pm.expect(responseXml).to.have.nested.property(\"").append(path.replace("/", ".")).append("\");\n");
                    js.append("});\n");
                }
                break;
                
            case "JSON Path Match":
                String jsonPath = getConfigurationProperty("path");
                String jsonContent = getConfigurationProperty("content");
                if (jsonPath != null && jsonContent != null) {
                    js.append("pm.test(\"JSON Path Match for ").append(jsonPath).append("\", function() {\n");
                    js.append("    const jsonData = pm.response.json();\n");
                    js.append("    pm.expect(jsonData).to.have.nested.property(\"").append(jsonPath).append("\");\n");
                    js.append("    pm.expect(jsonData.").append(jsonPath).append(").to.equal(").append(jsonContent).append(");\n");
                    js.append("});\n");
                }
                break;
                
            case "Contains":
                String token = getConfigurationProperty("token");
                if (token != null) {
                    js.append("pm.test(\"Response contains ").append(token).append("\", function() {\n");
                    js.append("    pm.expect(pm.response.text()).to.include(\"").append(token).append("\");\n");
                    js.append("});\n");
                }
                break;
                
            default:
                js.append("// Unsupported assertion type: ").append(type);
                break;
        }
        
        return js.toString();
    }
    
    @Override
    public String toString() {
        return "ReadyApiAssertion{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", configuration=" + configuration +
                '}';
    }
} 