package com.readyapi.converter;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Paths;

/**
 * Main class for converting ReadyAPI projects to Postman collections.
 */
public class ReadyApiToPostmanConverter {
    // private static final Logger logger = LoggerFactory.getLogger(ReadyApiToPostmanConverter.class);
    
    // List to track items that couldn't be converted
    private final List<String> conversionIssues = new ArrayList<>();
    
    public static void main(String[] args) {
        // Get the current working directory
        String currentPath = new File(".").getAbsolutePath();
        System.out.println("Current directory: " + currentPath);
        
        // Use absolute paths to be sure
        String convertToPostmanDir = "/Users/kargee/createXML/samples/Convert2Postman";
        String inputFilePath = convertToPostmanDir + "/clean_xml/ready_api_project.xml";
        String outputDirectory = convertToPostmanDir + "/output";
        
        // Check if the file exists
        File inputFile = new File(inputFilePath);
        if (!inputFile.exists()) {
            System.err.println("ERROR: Input file does not exist: " + inputFilePath);
            return;
        }
        
        System.out.println("Input file path: " + inputFilePath);
        System.out.println("Output directory: " + outputDirectory);
        
        ReadyApiToPostmanConverter converter = new ReadyApiToPostmanConverter();
        converter.convert(inputFilePath, outputDirectory);
    }
    
    /**
     * Convert a ReadyAPI project to Postman collection
     * 
     * @param readyApiFile Path to the ReadyAPI project file
     * @param outputDirectory Directory to save the output files
     */
    public void convert(String readyApiFile, String outputDirectory) {
        System.out.println("Starting conversion of ReadyAPI project: " + readyApiFile);
        
        try {
            // Create output directory if it doesn't exist
            File outputDir = new File(outputDirectory);
            if (!outputDir.exists()) {
                if (!outputDir.mkdirs()) {
                    System.err.println("Failed to create output directory: " + outputDirectory);
                    return;
                }
            }
            
            // Parse the ReadyAPI project
            System.out.println("Parsing ReadyAPI project...");
            ReadyApiProject project = new ReadyApiProjectParser().parse(readyApiFile);
            
            // Create Postman collection
            System.out.println("Creating Postman collection...");
            PostmanCollectionBuilder collectionBuilder = new PostmanCollectionBuilder(project);
            PostmanCollection collection = collectionBuilder.build();
            collection.setConversionIssues(collectionBuilder.getConversionIssues());
            
            // Create Postman environment
            System.out.println("Creating Postman environment...");
            PostmanEnvironment environment = new PostmanEnvironmentBuilder(project).build();
            
            // Save Postman collection and environment
            String projectName = project.getName();
            String collectionFile = outputDir.getPath() + File.separator + projectName + ".postman_collection.json";
            String environmentFile = outputDir.getPath() + File.separator + projectName + ".postman_environment.json";
            String issuesFile = outputDir.getPath() + File.separator + projectName + "_conversion_issues.txt";
            
            System.out.println("Saving Postman collection to: " + collectionFile);
            collection.saveToFile(collectionFile);
            
            System.out.println("Saving Postman environment to: " + environmentFile);
            environment.saveToFile(environmentFile);
            
            // Save any CSV data files
            System.out.println("Saving data files...");
            new DataFileExporter(project, outputDir).export();
            
            // Save conversion issues if any
            if (!collection.getConversionIssues().isEmpty()) {
                System.out.println("Saving conversion issues to: " + issuesFile);
                ConversionIssueReporter.saveIssues(collection.getConversionIssues(), issuesFile);
            }
            
            System.out.println("Validating Postman collection...");
            boolean isValid = new PostmanCollectionValidator().validate(collectionFile);
            if (isValid) {
                System.out.println("Postman collection validation successful!");
            } else {
                System.out.println("Postman collection validation failed. See logs for details.");
            }
            
            System.out.println("Conversion completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Error during conversion: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get the list of conversion issues
     * 
     * @return List of conversion issues
     */
    public List<String> getConversionIssues() {
        return conversionIssues;
    }
} 