package com.readyapi.converter;

import com.readyapi.converter.postman.PostmanCollection;
import com.readyapi.converter.postman.PostmanItem;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Builds a Postman collection from ReadyAPI project elements
 */
public class PostmanCollectionBuilder {
    private static final Logger logger = LoggerFactory.getLogger(PostmanCollectionBuilder.class);
    
    private final ReadyApiProjectParser parser;
    private final Map<String, String> variableMap;
    
    public PostmanCollectionBuilder() {
        this.parser = new ReadyApiProjectParser();
        this.variableMap = new HashMap<>();
    }
    
    /**
     * Build a Postman collection from ReadyAPI project elements
     * 
     * @param rootElement The root element of the ReadyAPI project
     * @return The built Postman collection
     */
    public PostmanCollection build(Element rootElement) {
        PostmanCollection collection = new PostmanCollection();
        collection.setInfo(createCollectionInfo(rootElement));
        
        // Process test cases
        List<Element> testCases = rootElement.elements("testCase");
        for (Element testCase : testCases) {
            processTestCase(testCase, collection);
        }
        
        return collection;
    }
    
    private void processTestCase(Element testCase, PostmanCollection collection) {
        String testCaseName = testCase.attributeValue("name");
        logger.info("Processing test case: {}", testCaseName);
        
        PostmanItem folder = new PostmanItem();
        folder.setName(testCaseName);
        
        // Process test steps
        List<Element> testSteps = testCase.elements("testStep");
        for (Element testStep : testSteps) {
            PostmanItem item = parser.convertTestStep(testStep);
            folder.addItem(item);
        }
        
        collection.addItem(folder);
    }
    
    private PostmanCollection.PostmanInfo createCollectionInfo(Element rootElement) {
        PostmanCollection.PostmanInfo info = new PostmanCollection.PostmanInfo();
        info.setName(rootElement.attributeValue("name", "ReadyAPI Project"));
        info.setSchema("https://schema.getpostman.com/json/collection/v2.1.0/collection.json");
        return info;
    }
} 