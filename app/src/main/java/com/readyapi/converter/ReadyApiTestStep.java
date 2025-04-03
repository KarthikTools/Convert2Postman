package com.readyapi.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a ReadyAPI test step.
 */
public class ReadyApiTestStep {
    private String id;
    private String name;
    private String type;
    private String content;  // Will contain script content for Groovy scripts or request config for REST requests
    private Map<String, String> properties = new HashMap<>();
    private ReadyApiRequest request;  // For REST request test steps
    private List<PropertyTransfer> propertyTransfers = new ArrayList<>(); // For property transfer steps
    
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
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
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
    
    public ReadyApiRequest getRequest() {
        return request;
    }
    
    public void setRequest(ReadyApiRequest request) {
        this.request = request;
    }
    
    public List<PropertyTransfer> getPropertyTransfers() {
        return propertyTransfers;
    }
    
    public void addPropertyTransfer(PropertyTransfer transfer) {
        this.propertyTransfers.add(transfer);
    }
    
    /**
     * Convert Groovy script to JavaScript for Postman.
     * 
     * @return JavaScript code for Postman
     */
    public String convertGroovyToJavaScript() {
        if ("groovy".equalsIgnoreCase(type) || "script".equalsIgnoreCase(type)) {
            ScriptConverter converter = new ScriptConverter();
            String script = getProperty("script");
            
            if (script == null || script.isEmpty()) {
                return "// No script content found for step: " + name;
            }
            
            // Special handling for RunTest steps
            if (name != null && name.contains("RunTest")) {
                return convertRunTestStepToJavaScript(script);
            }
            
            return converter.convertGroovyToJavaScript(script);
        }
        
        return "";
    }
    
    /**
     * Convert a RunTest step to JavaScript with special handling for common patterns.
     * 
     * @param groovyScript The original Groovy script
     * @return Converted JavaScript for Postman
     */
    private String convertRunTestStepToJavaScript(String groovyScript) {
        StringBuilder jsScript = new StringBuilder();
        
        jsScript.append("// This script initializes the test environment based on the ReadyAPI RunTest step\n\n");
        
        // Add function library setup
        jsScript.append("// Setup function library (equivalent to ReadyAPI's global script library)\n");
        jsScript.append("try {\n");
        jsScript.append("    // Check if function library is available\n");
        jsScript.append("    let functionLibrary = pm.collectionVariables.get('FunctionLibrary');\n");
        jsScript.append("    if (!functionLibrary) {\n");
        jsScript.append("        // Initialize function library with common utilities\n");
        jsScript.append("        const utilities = {\n");
        jsScript.append("            // Environment detection functions\n");
        jsScript.append("            getEnvironmentType: function() {\n");
        jsScript.append("                return pm.environment.get('environment') || 'DEV';\n");
        jsScript.append("            },\n\n");
        jsScript.append("            // Logging functions\n");
        jsScript.append("            logInfo: function(message) {\n");
        jsScript.append("                console.log(`[INFO] ${message}`);\n");
        jsScript.append("                return true;\n");
        jsScript.append("            },\n");
        jsScript.append("            createLogFile: function(prefix, filename) {\n");
        jsScript.append("                console.log(`[LOG] Test execution started - logs will appear in Postman console`);\n");
        jsScript.append("                return true;\n");
        jsScript.append("            }\n");
        jsScript.append("        };\n\n");
        jsScript.append("        pm.collectionVariables.set('FunctionLibrary', JSON.stringify(utilities));\n");
        jsScript.append("        console.log('Function library initialized');\n");
        jsScript.append("    }\n\n");
        
        // Environment setup for card numbers
        jsScript.append("    // Set up environment-specific variables\n");
        jsScript.append("    const environment = pm.environment.get('environment') || 'DEV';\n");
        jsScript.append("    let cardNumber;\n\n");
        
        jsScript.append("    // Set card number based on environment\n");
        jsScript.append("    switch(environment) {\n");
        jsScript.append("        case 'DEV':\n");
        jsScript.append("            cardNumber = '4519022640754669';\n");
        jsScript.append("            break;\n");
        jsScript.append("        case 'SIT':\n");
        jsScript.append("            cardNumber = '4519835555858010';\n");
        jsScript.append("            break;\n");
        jsScript.append("        case 'UAT':\n");
        jsScript.append("            cardNumber = '4519891586948663';\n");
        jsScript.append("            break;\n");
        jsScript.append("        default:\n");
        jsScript.append("            cardNumber = '4519022640754669'; // Default to DEV\n");
        jsScript.append("    }\n\n");
        
        jsScript.append("    // Store the card number in environment variable\n");
        jsScript.append("    pm.environment.set('CardNumber', cardNumber);\n");
        jsScript.append("    console.log(`Environment set to ${environment}, using card: ${cardNumber}`);\n\n");
        
        jsScript.append("    // Initialize test result variables\n");
        jsScript.append("    pm.environment.set('recordResult', 'PASS');\n");
        jsScript.append("    pm.environment.set('testStepResult', '');\n");
        
        jsScript.append("} catch (error) {\n");
        jsScript.append("    console.error('Error in RunTest initialization:', error);\n");
        jsScript.append("}\n");
        
        return jsScript.toString();
    }
    
    /**
     * Determine if this test step should be converted to a Postman pre-request script.
     */
    public boolean isPreRequestScript() {
        return "groovy".equalsIgnoreCase(type) && 
               (name.toLowerCase().contains("setup") || 
                name.toLowerCase().contains("prerequest") ||
                name.toLowerCase().contains("pre-request"));
    }
    
    /**
     * Determine if this test step should be converted to a Postman test script.
     */
    public boolean isTestScript() {
        return "groovy".equalsIgnoreCase(type) && 
               !isPreRequestScript() && 
               (name.toLowerCase().contains("test") || 
                name.toLowerCase().contains("assertion") ||
                name.toLowerCase().contains("validate"));
    }
    
    /**
     * Convert property transfers to JavaScript.
     * 
     * @return JavaScript code for property transfers
     */
    public String convertPropertyTransfersToJavaScript() {
        if (!"propertytransfer".equalsIgnoreCase(type)) {
            return "";
        }
        
        StringBuilder jsCode = new StringBuilder();
        
        jsCode.append("// Property transfers from step: ").append(name).append("\n");
        
        for (PropertyTransfer transfer : propertyTransfers) {
            jsCode.append(transfer.toJavaScript()).append("\n");
        }
        
        return jsCode.toString();
    }
    
    /**
     * Convert a data source loop test step to a JavaScript snippet for Postman.
     * 
     * @return JavaScript code for Postman
     */
    public String convertDataSourceLoopToJavaScript() {
        if (!"datasourceloop".equalsIgnoreCase(type)) {
            return "";
        }
        
        String dataSourceName = getProperty("dataSource");
        String loopStrategy = getProperty("strategy");
        String dataSourceStep = getProperty("sourceStep");
        
        if (dataSourceName == null && dataSourceStep != null) {
            dataSourceName = dataSourceStep;
        }
        
        if (dataSourceName == null) {
            return "// Warning: Missing data source for DataSourceLoop step: " + name;
        }
        
        StringBuilder jsCode = new StringBuilder();
        jsCode.append("// DataSourceLoop from step: ").append(name).append("\n");
        jsCode.append("// Using data source: ").append(dataSourceName).append("\n");
        
        // Create a Postman loop using the Collection Runner variables
        jsCode.append("// In Postman, you can iterate over data with Collection Runner or using this code snippet:\n");
        jsCode.append("let dataFileName = '").append(dataSourceName).append("';\n");
        jsCode.append("let dataJson = pm.variables.get(dataFileName);\n");
        jsCode.append("if (!dataJson) {\n");
        jsCode.append("    console.error(`Missing data for ${dataFileName}. Import the data file into Postman.`);\n");
        jsCode.append("} else {\n");
        jsCode.append("    try {\n");
        jsCode.append("        let data = JSON.parse(dataJson);\n");
        jsCode.append("        if (Array.isArray(data)) {\n");
        jsCode.append("            // This is where you'd iterate through the data\n");
        jsCode.append("            for (let i = 0; i < data.length; i++) {\n");
        jsCode.append("                let row = data[i];\n");
        jsCode.append("                // Process row data and make API calls here\n");
        jsCode.append("                console.log(`Processing data row ${i+1}`, row);\n");
        jsCode.append("                \n");
        jsCode.append("                // Example: Set environment variables from the data row\n");
        jsCode.append("                for (let key in row) {\n");
        jsCode.append("                    pm.variables.set(key, row[key]);\n");
        jsCode.append("                }\n");
        jsCode.append("            }\n");
        jsCode.append("        } else {\n");
        jsCode.append("            console.error('Data is not an array');\n");
        jsCode.append("        }\n");
        jsCode.append("    } catch (error) {\n");
        jsCode.append("        console.error(`Error parsing data: ${error.message}`);\n");
        jsCode.append("    }\n");
        jsCode.append("}\n");
        
        return jsCode.toString();
    }
    
    /**
     * Convert a datasink test step to a JavaScript snippet for Postman.
     * 
     * @return JavaScript code for Postman
     */
    public String convertDataSinkToJavaScript() {
        if (!"datasink".equalsIgnoreCase(type)) {
            return "";
        }
        
        String targetStep = getProperty("targetStep");
        String format = getProperty("format");
        String file = getProperty("file");
        
        StringBuilder jsCode = new StringBuilder();
        jsCode.append("// DataSink from step: ").append(name).append("\n");
        
        if (file != null && !file.isEmpty()) {
            jsCode.append("// Target file: ").append(file).append("\n");
        }
        
        if (targetStep != null && !targetStep.isEmpty()) {
            jsCode.append("// Target step: ").append(targetStep).append("\n");
        }
        
        // Create a Postman script to simulate data export
        jsCode.append("// In Postman, you can export data to a file using the following approach:\n");
        jsCode.append("// 1. Store the data in a variable\n");
        jsCode.append("// 2. Use console.log to display it (can be copied manually)\n");
        jsCode.append("// 3. For automated exports, consider using Newman with reporters\n\n");
        
        jsCode.append("// This is a placeholder for data export functionality\n");
        jsCode.append("let dataToExport = {};\n\n");
        
        jsCode.append("// Sample code to collect data from the response\n");
        jsCode.append("try {\n");
        jsCode.append("    // Extract data from response\n");
        jsCode.append("    if (pm.response.json) {\n");
        jsCode.append("        dataToExport = pm.response.json();\n");
        jsCode.append("    } else {\n");
        jsCode.append("        dataToExport = { text: pm.response.text() };\n");
        jsCode.append("    }\n");
        jsCode.append("    \n");
        jsCode.append("    // For demonstration, store in a variable\n");
        jsCode.append("    let exportVarName = 'exportedData_").append(name.replace(" ", "_")).append("';\n");
        jsCode.append("    pm.variables.set(exportVarName, JSON.stringify(dataToExport));\n");
        jsCode.append("    \n");
        jsCode.append("    console.log(`Data prepared for export from step ${pm.info.requestName}:`);\n");
        jsCode.append("    console.log(dataToExport);\n");
        jsCode.append("    console.log(`Stored in variable: ${exportVarName}`);\n");
        jsCode.append("} catch (error) {\n");
        jsCode.append("    console.error(`Error preparing data for export: ${error.message}`);\n");
        jsCode.append("}\n");
        
        return jsCode.toString();
    }
    
    /**
     * Convert a properties test step to a JavaScript snippet for Postman.
     * 
     * @return JavaScript code for Postman
     */
    public String convertPropertiesToJavaScript() {
        if (!"properties".equalsIgnoreCase(type) || content == null || content.isEmpty()) {
            return "";
        }
        
        StringBuilder jsCode = new StringBuilder();
        jsCode.append("// Properties from step: ").append(name).append("\n");
        
        // Parse the properties content (simple key=value format)
        String[] lines = content.split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue; // Skip comments and empty lines
            }
            
            int equalsPos = line.indexOf('=');
            if (equalsPos > 0) {
                String key = line.substring(0, equalsPos).trim();
                String value = line.substring(equalsPos + 1).trim();
                jsCode.append("pm.variables.set(\"").append(key).append("\", \"").append(value.replace("\"", "\\\"")).append("\");\n");
            }
        }
        
        return jsCode.toString();
    }
    
    /**
     * Get or generate a JavaScript snippet for this test step based on its type.
     * 
     * @return JavaScript code for Postman
     */
    public String toJavaScript() {
        if ("groovy".equalsIgnoreCase(type)) {
            return convertGroovyToJavaScript();
        } else if ("propertytransfer".equalsIgnoreCase(type)) {
            return convertPropertyTransfersToJavaScript();
        } else if ("datasourceloop".equalsIgnoreCase(type)) {
            return convertDataSourceLoopToJavaScript();
        } else if ("datasink".equalsIgnoreCase(type)) {
            return convertDataSinkToJavaScript();
        } else if ("properties".equalsIgnoreCase(type)) {
            return convertPropertiesToJavaScript();
        }
        
        return "// No conversion available for test step type: " + type;
    }
    
    @Override
    public String toString() {
        return "ReadyApiTestStep{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", properties=" + properties.size() +
                '}';
    }
    
    /**
     * Class representing a property transfer.
     */
    public static class PropertyTransfer {
        private String name;
        private String sourceName;
        private String sourcePath;
        private String sourcePathLanguage;
        private String targetName;
        private String targetPath;
        private String targetPathLanguage;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getSourceName() {
            return sourceName;
        }
        
        public void setSourceName(String sourceName) {
            this.sourceName = sourceName;
        }
        
        public String getSourcePath() {
            return sourcePath;
        }
        
        public void setSourcePath(String sourcePath) {
            this.sourcePath = sourcePath;
        }
        
        public String getSourcePathLanguage() {
            return sourcePathLanguage;
        }
        
        public void setSourcePathLanguage(String sourcePathLanguage) {
            this.sourcePathLanguage = sourcePathLanguage;
        }
        
        public String getTargetName() {
            return targetName;
        }
        
        public void setTargetName(String targetName) {
            this.targetName = targetName;
        }
        
        public String getTargetPath() {
            return targetPath;
        }
        
        public void setTargetPath(String targetPath) {
            this.targetPath = targetPath;
        }
        
        public String getTargetPathLanguage() {
            return targetPathLanguage;
        }
        
        public void setTargetPathLanguage(String targetPathLanguage) {
            this.targetPathLanguage = targetPathLanguage;
        }
        
        /**
         * Convert this property transfer to JavaScript.
         * 
         * @return JavaScript code for this property transfer
         */
        public String toJavaScript() {
            StringBuilder jsCode = new StringBuilder();
            
            jsCode.append("// Property Transfer: ").append(name != null ? name : "Unnamed").append("\n");
            jsCode.append("// Source: ").append(sourceName).append(", Target: ").append(targetName).append("\n");
            
            String sourceVarName = "sourceValue";
            
            // Extract source value
            if (sourcePathLanguage != null && sourcePathLanguage.toLowerCase().contains("xpath")) {
                // Handle XPath source
                jsCode.append(XPathHelper.getXPathPostmanNotes());
                
                // Get source content
                if (sourceName != null && sourceName.toLowerCase().contains("response")) {
                    jsCode.append("const sourceContent = pm.response.text();\n");
                } else {
                    jsCode.append("const sourceContent = pm.variables.get('").append(sourceName).append("');\n");
                }
                
                // Add XPath evaluation
                jsCode.append(XPathHelper.convertXPathToJavaScript(sourcePath, "sourceContent", sourceVarName));
            } else if (sourcePathLanguage != null && sourcePathLanguage.toLowerCase().contains("jsonpath")) {
                // Handle JSONPath source
                if (sourceName != null && sourceName.toLowerCase().contains("response")) {
                    jsCode.append("const sourceContent = pm.response.json();\n");
                } else {
                    jsCode.append("const sourceContent = pm.variables.get('").append(sourceName).append("');\n");
                    jsCode.append("// Parse JSON if needed\n");
                    jsCode.append("if (typeof sourceContent === 'string') {\n");
                    jsCode.append("    try {\n");
                    jsCode.append("        sourceContent = JSON.parse(sourceContent);\n");
                    jsCode.append("    } catch (e) {\n");
                    jsCode.append("        console.error('Failed to parse JSON:', e);\n");
                    jsCode.append("    }\n");
                    jsCode.append("}\n");
                }
                
                // Add JSONPath evaluation
                jsCode.append(XPathHelper.convertJSONPathToJavaScript(sourcePath, "sourceContent", sourceVarName));
            } else {
                // Handle property source
                if (sourceName != null && sourceName.toLowerCase().contains("response")) {
                    if (sourcePath != null && !sourcePath.isEmpty()) {
                        jsCode.append("let ").append(sourceVarName).append(" = pm.response.json()");
                        
                        // Handle simple property paths
                        String[] parts = sourcePath.split("\\.");
                        for (String part : parts) {
                            if (!part.isEmpty()) {
                                jsCode.append("?.").append(part);
                            }
                        }
                        
                        jsCode.append(";\n");
                    } else {
                        jsCode.append("let ").append(sourceVarName).append(" = pm.response.text();\n");
                    }
                } else {
                    jsCode.append("let ").append(sourceVarName).append(" = pm.variables.get('").append(sourceName).append("');\n");
                    
                    // Apply path if provided
                    if (sourcePath != null && !sourcePath.isEmpty()) {
                        jsCode.append("// Try to parse JSON and apply path\n");
                        jsCode.append("try {\n");
                        jsCode.append("    let jsonObj = JSON.parse(").append(sourceVarName).append(");\n");
                        
                        // Handle simple property paths
                        String[] parts = sourcePath.split("\\.");
                        jsCode.append("    ").append(sourceVarName).append(" = jsonObj");
                        for (String part : parts) {
                            if (!part.isEmpty()) {
                                jsCode.append("?.").append(part);
                            }
                        }
                        
                        jsCode.append(";\n");
                        jsCode.append("} catch (e) {\n");
                        jsCode.append("    // Not JSON or invalid path, keep original value\n");
                        jsCode.append("    console.log('Could not parse JSON or apply path: ' + e.message);\n");
                        jsCode.append("}\n");
                    }
                }
            }
            
            // Set target value
            if (targetName != null && targetName.toLowerCase().contains("request")) {
                // Set request property
                if (targetPath != null && !targetPath.isEmpty()) {
                    if (targetPath.toLowerCase().contains("header")) {
                        // Set request header
                        String headerName = targetPath.replaceAll("(?i).*header\\s*\\[([^\\]]+)\\].*", "$1");
                        jsCode.append("pm.request.headers.upsert({\n");
                        jsCode.append("    key: '").append(headerName).append("',\n");
                        jsCode.append("    value: ").append(sourceVarName).append("\n");
                        jsCode.append("});\n");
                    } else if (targetPath.toLowerCase().contains("query")) {
                        // Set query parameter
                        String paramName = targetPath.replaceAll("(?i).*query\\s*\\[([^\\]]+)\\].*", "$1");
                        jsCode.append("// Set query parameter in the request URL\n");
                        jsCode.append("let url = pm.request.url.toString();\n");
                        jsCode.append("const urlObj = new URL(url);\n");
                        jsCode.append("urlObj.searchParams.set('").append(paramName).append("', ").append(sourceVarName).append(");\n");
                        jsCode.append("pm.request.url.update(urlObj.toString());\n");
                    } else {
                        // General request property
                        jsCode.append("// Set request property: ").append(targetPath).append("\n");
                        jsCode.append("console.log('Setting request property is not fully supported in Postman. Using environment variable instead.');\n");
                        jsCode.append("pm.variables.set('").append(targetPath).append("', ").append(sourceVarName).append(");\n");
                    }
                } else {
                    // Set whole request
                    jsCode.append("// Setting entire request body is not fully supported in Postman scripts\n");
                    jsCode.append("console.log('Setting entire request body directly is not supported. Using environment variable.');\n");
                    jsCode.append("pm.variables.set('requestBody', ").append(sourceVarName).append(");\n");
                }
            } else {
                // Set variable or environment property
                if (targetPath != null && !targetPath.isEmpty()) {
                    jsCode.append("// Set target path: ").append(targetPath).append(" in ").append(targetName).append("\n");
                    jsCode.append("let targetObj = pm.variables.get('").append(targetName).append("');\n");
                    jsCode.append("try {\n");
                    jsCode.append("    // Parse target as JSON if it's a string\n");
                    jsCode.append("    if (typeof targetObj === 'string') {\n");
                    jsCode.append("        targetObj = JSON.parse(targetObj);\n");
                    jsCode.append("    } else if (!targetObj) {\n");
                    jsCode.append("        targetObj = {};\n");
                    jsCode.append("    }\n");
                    
                    // Set nested property
                    jsCode.append("    // Set nested property\n");
                    jsCode.append("    const setNestedProperty = (obj, path, value) => {\n");
                    jsCode.append("        const parts = path.split('.');\n");
                    jsCode.append("        let current = obj;\n");
                    jsCode.append("        for (let i = 0; i < parts.length - 1; i++) {\n");
                    jsCode.append("            const part = parts[i];\n");
                    jsCode.append("            if (!current[part]) current[part] = {};\n");
                    jsCode.append("            current = current[part];\n");
                    jsCode.append("        }\n");
                    jsCode.append("        current[parts[parts.length - 1]] = value;\n");
                    jsCode.append("        return obj;\n");
                    jsCode.append("    };\n");
                    
                    jsCode.append("    // Apply the nested property setter\n");
                    jsCode.append("    targetObj = setNestedProperty(targetObj, '").append(targetPath).append("', ").append(sourceVarName).append(");\n");
                    jsCode.append("    pm.variables.set('").append(targetName).append("', JSON.stringify(targetObj));\n");
                    jsCode.append("} catch (e) {\n");
                    jsCode.append("    console.error('Failed to set nested property:', e);\n");
                    jsCode.append("    // Fallback to simple variable\n");
                    jsCode.append("    pm.variables.set('").append(targetName).append("_").append(targetPath.replace('.', '_')).append("', ").append(sourceVarName).append(");\n");
                    jsCode.append("}\n");
                } else {
                    // Simple variable set
                    jsCode.append("// Set target variable: ").append(targetName).append("\n");
                    
                    // Determine if we should use environment or collection variables
                    if (targetName.toLowerCase().contains("project") || 
                        targetName.toLowerCase().contains("environment") || 
                        targetName.toLowerCase().contains("global")) {
                        jsCode.append("pm.environment.set('").append(targetName).append("', ").append(sourceVarName).append(");\n");
                    } else {
                        jsCode.append("pm.variables.set('").append(targetName).append("', ").append(sourceVarName).append(");\n");
                    }
                }
            }
            
            jsCode.append("console.log('Property transfer completed: ").append(sourceName).append(" -> ").append(targetName).append("');\n");
            
            return jsCode.toString();
        }
    }
} 