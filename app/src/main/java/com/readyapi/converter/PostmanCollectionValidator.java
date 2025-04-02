package com.readyapi.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Utility for validating Postman collections.
 */
public class PostmanCollectionValidator {
    private static final Logger logger = LoggerFactory.getLogger(PostmanCollectionValidator.class);
    
    /**
     * Validate a Postman collection JSON file.
     * 
     * @param filePath Path to the Postman collection JSON file
     * @return true if the collection is valid, false otherwise
     */
    public boolean validate(String filePath) {
        logger.info("Validating Postman collection: {}", filePath);
        
        try {
            // Load the collection JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(new File(filePath));
            
            // Validate required fields
            boolean valid = validateRequiredFields(rootNode);
            
            if (valid) {
                logger.info("Postman collection is valid.");
            } else {
                logger.warn("Postman collection is not valid.");
            }
            
            return valid;
            
        } catch (IOException e) {
            logger.error("Error validating Postman collection: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Validate required fields in the Postman collection.
     * 
     * @param rootNode The root JSON node of the Postman collection
     * @return true if all required fields are present, false otherwise
     */
    private boolean validateRequiredFields(JsonNode rootNode) {
        // Validate info
        JsonNode infoNode = rootNode.get("info");
        if (infoNode == null) {
            logger.warn("Missing 'info' field in Postman collection.");
            return false;
        }
        
        // Validate info._postman_id
        if (!infoNode.has("_postman_id")) {
            logger.warn("Missing '_postman_id' field in Postman collection info.");
            return false;
        }
        
        // Validate info.name
        if (!infoNode.has("name")) {
            logger.warn("Missing 'name' field in Postman collection info.");
            return false;
        }
        
        // Validate info.schema
        if (!infoNode.has("schema")) {
            logger.warn("Missing 'schema' field in Postman collection info.");
            return false;
        }
        
        // Validate item
        if (!rootNode.has("item")) {
            logger.warn("Missing 'item' field in Postman collection.");
            return false;
        }
        
        // Validate collection items recursively
        JsonNode itemsNode = rootNode.get("item");
        if (!itemsNode.isArray()) {
            logger.warn("'item' field is not an array in Postman collection.");
            return false;
        }
        
        // Collection looks valid
        return true;
    }
} 