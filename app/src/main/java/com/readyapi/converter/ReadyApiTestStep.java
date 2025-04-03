package com.readyapi.converter;

import org.dom4j.Element;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Represents a test step in a ReadyAPI test case
 */
public class ReadyApiTestStep {
    private String id;
    private String name;
    private String type;
    private ReadyApiRequest request;
    private List<ReadyApiAssertion> assertions;
    private Map<String, String> properties;
    private List<PropertyTransfer> propertyTransfers;
    private boolean isPreRequestScript;
    private boolean isTestScript;
    private Element config;
    private String groovyScript;
    
    public ReadyApiTestStep() {
        this.assertions = new ArrayList<>();
        this.properties = new HashMap<>();
        this.propertyTransfers = new ArrayList<>();
    }
    
    public ReadyApiTestStep(Element element) {
        this.id = element.attributeValue("id");
        this.name = element.attributeValue("name");
        this.type = element.attributeValue("type");
        this.config = element.element("config");
        this.assertions = new ArrayList<>();
        this.properties = new HashMap<>();
        this.propertyTransfers = new ArrayList<>();
        
        // Parse assertions
        List<Element> assertionElements = element.elements("assertion");
        for (Element assertionElement : assertionElements) {
            assertions.add(new ReadyApiAssertion(assertionElement));
        }
        
        // Parse property transfers
        Element transfersElement = element.element("transfers");
        if (transfersElement != null) {
            List<Element> transferElements = transfersElement.elements("transfer");
            for (Element transferElement : transferElements) {
                propertyTransfers.add(new PropertyTransfer(transferElement));
            }
        }
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
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public ReadyApiRequest getRequest() {
        return request;
    }
    
    public void setRequest(ReadyApiRequest request) {
        this.request = request;
    }
    
    public Element getConfig() {
        return config;
    }
    
    public List<ReadyApiAssertion> getAssertions() {
        return assertions;
    }
    
    public void setAssertions(List<ReadyApiAssertion> assertions) {
        this.assertions = assertions;
    }
    
    public void addAssertion(ReadyApiAssertion assertion) {
        this.assertions.add(assertion);
    }
    
    public Map<String, String> getProperties() {
        return properties;
    }
    
    public void addProperty(String key, String value) {
        this.properties.put(key, value);
    }
    
    public String getProperty(String key) {
        return properties.get(key);
    }
    
    public List<PropertyTransfer> getPropertyTransfers() {
        return propertyTransfers;
    }
    
    public void setPropertyTransfers(List<PropertyTransfer> propertyTransfers) {
        this.propertyTransfers = propertyTransfers;
    }
    
    public void addPropertyTransfer(PropertyTransfer transfer) {
        this.propertyTransfers.add(transfer);
    }
    
    public boolean isPreRequestScript() {
        return isPreRequestScript;
    }
    
    public void setPreRequestScript(boolean preRequestScript) {
        isPreRequestScript = preRequestScript;
    }
    
    public boolean isTestScript() {
        return isTestScript;
    }
    
    public void setTestScript(boolean testScript) {
        isTestScript = testScript;
    }
    
    public String getMethod() {
        return request != null ? request.getMethod() : "";
    }
    
    public String getEndpoint() {
        return request != null ? request.getEndpoint() : "";
    }
    
    public Map<String, String> getHeaders() {
        return request != null ? request.getHeaders() : new HashMap<>();
    }
    
    public String getMediaType() {
        return request != null ? request.getMediaType() : null;
    }
    
    public String getRequestBody() {
        return request != null ? request.getBody() : null;
    }
    
    public String getContent() {
        if (config != null) {
            Element scriptElement = config.element("script");
            if (scriptElement != null) {
                return scriptElement.getTextTrim();
            }
        }
        return null;
    }
    
    public List<String> convertPropertiesToJavaScript() {
        List<String> jsLines = new ArrayList<>();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            jsLines.add(String.format("pm.variables.set('%s', '%s');", entry.getKey(), entry.getValue()));
        }
        return jsLines;
    }
    
    public List<String> convertPropertyTransfersToJavaScript() {
        List<String> jsLines = new ArrayList<>();
        for (PropertyTransfer transfer : propertyTransfers) {
            jsLines.add(transfer.toPostmanPreRequestScript());
        }
        return jsLines;
    }
    
    public List<String> convertDataSourceLoopToJavaScript() {
        List<String> jsLines = new ArrayList<>();
        String dataSourceType = getProperty("dataSourceType");
        if (dataSourceType != null) {
            jsLines.add("// Data source loop");
            jsLines.add("let dataSource = pm.variables.get('dataSource');");
            jsLines.add("if (dataSource && dataSource.length > 0) {");
            jsLines.add("    pm.variables.set('currentRow', dataSource[0]);");
            jsLines.add("}");
        }
        return jsLines;
    }
    
    public List<String> convertDataSinkToJavaScript() {
        List<String> jsLines = new ArrayList<>();
        jsLines.add("// Data sink");
        jsLines.add("let responseData = pm.response.json();");
        jsLines.add("pm.variables.set('dataSink', responseData);");
        return jsLines;
    }
    
    public List<String> convertToPreRequestScript() {
        List<String> script = new ArrayList<>();
        
        // Convert property transfers to pre-request script
        for (PropertyTransfer transfer : propertyTransfers) {
            script.add(transfer.toPostmanPreRequestScript());
        }
        
        // Convert Groovy script to JavaScript if present
        String groovyScript = getGroovyScript();
        if (groovyScript != null && !groovyScript.trim().isEmpty()) {
            script.addAll(Arrays.asList(convertGroovyToJavaScript(groovyScript).split("\n")));
        }
        
        return script;
    }
    
    public String getGroovyScript() {
        if (config != null) {
            Element scriptElement = config.element("script");
            if (scriptElement != null) {
                return scriptElement.getTextTrim();
            }
        }
        return null;
    }
    
    public void setGroovyScript(String groovyScript) {
        this.groovyScript = groovyScript;
    }
    
    public String convertGroovyToJavaScript(String groovyScript) {
        if (groovyScript == null || groovyScript.trim().isEmpty()) {
            return "";
        }

        StringBuilder jsScript = new StringBuilder();
        String[] lines = groovyScript.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                jsScript.append("\n");
                continue;
            }

            // Convert Groovy syntax to JavaScript
            if (line.startsWith("//")) {
                jsScript.append(line).append("\n");
                continue;
            }

            // Convert log.info to console.log
            line = line.replaceAll("log\\.info\\((.+?)\\)", "console.log($1);");

            // Convert property access
            line = line.replaceAll("testRunner\\.testCase\\.testSteps\\[\"(.+?)\"\\]\\.getPropertyValue\\(\"(.+?)\"\\)",
                    "pm.collectionVariables.get('$1_$2')");

            // Convert JSON parsing
            line = line.replaceAll("new JsonSlurper\\(\\)\\.parseText\\((.+?)\\)", "$1");

            // Convert assertions
            line = line.replaceAll("assert (.+?)\\s*==\\s*(.+)",
                    "pm.test('Assert $1 == $2', function () {\n    pm.expect($1).to.eql($2);\n});");

            jsScript.append(line).append("\n");
        }

        return jsScript.toString();
    }
    
    public String toJavaScript() {
        StringBuilder js = new StringBuilder();
        
        // Add property transfers
        for (PropertyTransfer transfer : propertyTransfers) {
            js.append(String.format("pm.variables.set('%s', pm.response.json()%s);\n", 
                transfer.getName(), transfer.getSourcePath()));
        }
        
        // Add script if present
        if (groovyScript != null && !groovyScript.isEmpty()) {
            js.append(convertGroovyToJavaScript(groovyScript));
        }
        
        return js.toString();
    }

    /**
     * Represents a property transfer in a ReadyAPI test step
     */
    public static class PropertyTransfer {
        private String name;
        private String sourcePath;
        private String targetPath;

        public PropertyTransfer() {
        }

        public PropertyTransfer(Element element) {
            this.name = element.attributeValue("name");
            this.sourcePath = element.attributeValue("sourcePath");
            this.targetPath = element.attributeValue("targetPath");
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSourcePath() {
            return sourcePath;
        }

        public void setSourcePath(String sourcePath) {
            this.sourcePath = sourcePath;
        }

        public String getTargetPath() {
            return targetPath;
        }

        public void setTargetPath(String targetPath) {
            this.targetPath = targetPath;
        }

        public String toPostmanPreRequestScript() {
            return String.format("pm.variables.set('%s', pm.response.json()%s);", name, sourcePath);
        }
    }
} 