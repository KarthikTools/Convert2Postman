package com.readyapi.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Utility for reporting conversion issues.
 */
public class ConversionIssueReporter {
    private static final Logger logger = LoggerFactory.getLogger(ConversionIssueReporter.class);
    
    /**
     * Save conversion issues to a file.
     * 
     * @param issues List of conversion issues
     * @param filePath Path to save the issues file
     */
    public static void saveIssues(List<String> issues, String filePath) {
        if (issues == null || issues.isEmpty()) {
            logger.info("No conversion issues to report.");
            return;
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("ReadyAPI to Postman Conversion Issues");
            writer.println("=====================================");
            writer.println();
            
            for (int i = 0; i < issues.size(); i++) {
                writer.println((i + 1) + ". " + issues.get(i));
            }
            
            logger.info("Saved {} conversion issues to: {}", issues.size(), filePath);
            
        } catch (IOException e) {
            logger.error("Error saving conversion issues: {}", e.getMessage(), e);
        }
    }
} 