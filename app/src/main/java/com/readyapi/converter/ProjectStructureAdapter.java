package com.readyapi.converter;

import org.dom4j.Element;

/**
 * Interface for adapting to different ReadyAPI project structures.
 */
public interface ProjectStructureAdapter {
    
    /**
     * Parse a ReadyAPI project structure.
     * 
     * @param rootElement The root XML element
     * @param filePath Path to the project file
     * @return The parsed ReadyAPI project
     */
    ReadyApiProject parseProject(Element rootElement, String filePath);
    
    /**
     * Check if this adapter can handle the given project structure.
     * 
     * @param rootElement The root XML element
     * @return True if this adapter can handle the project structure
     */
    boolean canHandle(Element rootElement);
}

/**
 * Factory for creating ProjectStructureAdapter instances.
 */
class ProjectStructureAdapterFactory {
    
    /**
     * Get an adapter for the given project version.
     * 
     * @param projectVersion The project version
     * @return An appropriate adapter
     */
    public static ProjectStructureAdapter getAdapter(String projectVersion) {
        if (projectVersion != null) {
            if (projectVersion.contains("2.")) {
                return new ReadyApi2Adapter();
            } else if (projectVersion.contains("3.")) {
                return new ReadyApi3Adapter();
            }
        }
        
        // Default to latest version
        return new ReadyApi3Adapter();
    }
}

/**
 * Adapter for ReadyAPI 2.x project structure.
 */
class ReadyApi2Adapter implements ProjectStructureAdapter {
    
    @Override
    public ReadyApiProject parseProject(Element rootElement, String filePath) {
        ReadyApiProject project = new ReadyApiProject();
        project.setFilePath(filePath);
        project.setName(rootElement.attributeValue("name"));
        
        // Parse properties (different element name in 2.x)
        Element propertiesElement = rootElement.element("projectProperties");
        if (propertiesElement != null) {
            for (Element property : propertiesElement.elements("property")) {
                String name = property.attributeValue("name");
                String value = property.attributeValue("value");
                
                if (name != null && !name.isEmpty()) {
                    project.addProperty(name, value);
                }
            }
        }
        
        // Parse interfaces (different structure in 2.x)
        Element interfacesElement = rootElement.element("interface");
        if (interfacesElement != null) {
            for (Element interfaceElement : interfacesElement.elements("service")) {
                ReadyApiInterface apiInterface = new ReadyApiInterface();
                apiInterface.setName(interfaceElement.attributeValue("name"));
                
                // Parse resources
                for (Element resourceElement : interfaceElement.elements("resource")) {
                    ReadyApiResource resource = new ReadyApiResource();
                    resource.setName(resourceElement.attributeValue("name"));
                    resource.setPath(resourceElement.attributeValue("path"));
                    
                    // Parse methods
                    for (Element methodElement : resourceElement.elements("method")) {
                        ReadyApiMethod method = new ReadyApiMethod();
                        method.setName(methodElement.attributeValue("name"));
                        method.setHttpMethod(methodElement.attributeValue("method"));
                        
                        // Parse requests
                        for (Element requestElement : methodElement.elements("request")) {
                            ReadyApiRequest request = new ReadyApiRequest();
                            request.setName(requestElement.attributeValue("name"));
                            request.setEndpoint(requestElement.elementText("endpoint"));
                            
                            // Add request to method
                            method.addRequest(request);
                        }
                        
                        // Add method to resource
                        resource.addMethod(method);
                    }
                    
                    // Add resource to interface
                    apiInterface.addResource(resource);
                }
                
                // Add interface to project
                project.addInterface(apiInterface);
            }
        }
        
        // Parse other elements...
        
        return project;
    }
    
    @Override
    public boolean canHandle(Element rootElement) {
        String version = rootElement.attributeValue("soapui-version");
        return version != null && version.startsWith("2.");
    }
}

/**
 * Adapter for ReadyAPI 3.x project structure.
 */
class ReadyApi3Adapter implements ProjectStructureAdapter {
    
    @Override
    public ReadyApiProject parseProject(Element rootElement, String filePath) {
        ReadyApiProjectParser parser = new ReadyApiProjectParser();
        return parser.parseProjectElement(rootElement, filePath);
    }
    
    @Override
    public boolean canHandle(Element rootElement) {
        String version = rootElement.attributeValue("soapui-version");
        if (version != null) {
            return version.startsWith("3.") || version.startsWith("4.") || version.startsWith("5.");
        }
        
        // Check for elements typical to ReadyAPI 3.x structure
        return rootElement.element("con:soapui-project") != null ||
               rootElement.attributeValue("created") != null;
    }
} 