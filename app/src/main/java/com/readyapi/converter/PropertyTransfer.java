package com.readyapi.converter;

import org.dom4j.Element;

/**
 * Represents a property transfer in ReadyAPI.
 */
public class PropertyTransfer {
    private String name;
    private String sourcePath;
    private String targetName;
    private String targetPath;
    private String sourceStep;
    private String targetStep;
    private String transferType;

    public PropertyTransfer() {
    }

    public PropertyTransfer(Element element) {
        this.name = element.elementTextTrim("name");
        Element source = element.element("source");
        Element target = element.element("target");
        
        if (source != null) {
            this.sourceStep = source.elementTextTrim("step");
            this.sourcePath = source.elementTextTrim("path");
        }
        
        if (target != null) {
            this.targetStep = target.elementTextTrim("step");
            this.targetPath = target.elementTextTrim("path");
        }
        
        this.transferType = element.attributeValue("type", "JSONPATH");
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

    public String getSourceStep() {
        return sourceStep;
    }

    public String getTargetStep() {
        return targetStep;
    }

    public String getTransferType() {
        return transferType;
    }

    public String toPostmanPreRequestScript() {
        StringBuilder script = new StringBuilder();
        
        // Add comment to explain the transfer
        script.append("// Property transfer: ").append(name).append("\n");
        
        // Get source value based on transfer type
        if ("JSONPATH".equals(transferType)) {
            script.append(String.format("const sourceValue = pm.response.json()%s;\n", sourcePath));
        } else if ("XPATH".equals(transferType)) {
            script.append("const sourceValue = pm.xml2Json(pm.response.text())").append(sourcePath).append(";\n");
        } else {
            script.append(String.format("const sourceValue = pm.response.text()%s;\n", sourcePath));
        }
        
        // Set the target value
        script.append(String.format("pm.variables.set('%s', sourceValue);", name));
        
        return script.toString();
    }
} 