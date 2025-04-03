package com.readyapi.converter;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import org.dom4j.Element;

/**
 * Represents an assertion in a ReadyAPI test step
 */
public class ReadyApiAssertion {
    private String type;
    private String name;
    private Map<String, String> configuration;
    
    public ReadyApiAssertion() {
        this.configuration = new HashMap<>();
    }
    
    public ReadyApiAssertion(String type, String name) {
        this.type = type;
        this.name = name;
        this.configuration = new HashMap<>();
    }
    
    public ReadyApiAssertion(Element element) {
        this.type = element.attributeValue("type");
        this.name = element.attributeValue("name");
        this.configuration = new HashMap<>();
        
        Element configElement = element.element("configuration");
        if (configElement != null) {
            for (Element entry : configElement.elements("entry")) {
                String key = entry.attributeValue("key");
                String value = entry.attributeValue("value");
                if (key != null && value != null) {
                    configuration.put(key, value);
                }
            }
        }
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Map<String, String> getConfiguration() {
        return configuration;
    }
    
    public void setConfiguration(Map<String, String> configuration) {
        this.configuration = configuration;
    }
    
    public String getConfigurationProperty(String key) {
        return configuration.get(key);
    }
    
    public void setConfigurationProperty(String key, String value) {
        configuration.put(key, value);
    }
    
    public String toPostmanTest() {
        if ("Valid HTTP Status Codes".equals(type)) {
            return String.format("pm.test(\"%s\", function () { pm.response.to.have.status(200); });", name);
        } else if ("JSONPath Match".equals(type)) {
            String path = getConfigurationProperty("path");
            if (path != null) {
                return String.format("pm.test(\"Check JSONPath %s\", function () { var jsonData = pm.response.json(); pm.expect(jsonData%s).to.exist; });", path, path);
            }
        } else if ("Contains".equals(type)) {
            String content = getConfigurationProperty("content");
            if (content != null) {
                return String.format("pm.test(\"%s\", function () { pm.expect(pm.response.text()).to.include('%s'); });", name, content);
            }
        } else if ("Not Contains".equals(type)) {
            String content = getConfigurationProperty("content");
            if (content != null) {
                return String.format("pm.test(\"%s\", function () { pm.expect(pm.response.text()).to.not.include('%s'); });", name, content);
            }
        } else if ("XPath Match".equals(type)) {
            String xpath = getConfigurationProperty("xpath");
            if (xpath != null) {
                return String.format("pm.test(\"%s\", function () { var xmlDoc = pm.response.text(); var xpathResult = xmlDoc.evaluate('%s', xmlDoc, null, XPathResult.BOOLEAN_TYPE, null); pm.expect(xpathResult.booleanValue).to.be.true; });", name, xpath);
            }
        } else if ("Response SLA".equals(type)) {
            String maxTime = getConfigurationProperty("maxTime");
            if (maxTime != null) {
                return String.format("pm.test(\"%s\", function () { pm.expect(pm.response.responseTime).to.be.below(%s); });", name, maxTime);
            }
        } else if ("Script Assertion".equals(type)) {
            String script = getConfigurationProperty("script");
            if (script != null) {
                return String.format("pm.test(\"%s\", function () { %s });", name, script);
            }
        }
        
        return String.format("pm.test(\"%s\", function () { /* Unsupported assertion type: %s */ });", name, type);
    }
    
    public List<String> convertToPostmanTest() {
        List<String> tests = new ArrayList<>();
        tests.add(toPostmanTest());
        return tests;
    }
} 