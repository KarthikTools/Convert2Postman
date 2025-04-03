package com.readyapi.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Paths;
import java.io.FileWriter;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import java.util.Map;
import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.readyapi.converter.postman.*;

/**
 * Main class for converting ReadyAPI projects to Postman collections.
 */
public class ReadyApiToPostmanConverter {
    private static final Logger logger = LoggerFactory.getLogger(ReadyApiToPostmanConverter.class);
    
    private String baseUrl;
    private final List<String> conversionIssues;
    private ConversionIssueReporter issueReporter;
    private ReadyApiProjectParser projectParser;
    private ObjectMapper objectMapper;
    
    public ReadyApiToPostmanConverter(String baseUrl) {
        this.baseUrl = baseUrl;
        this.conversionIssues = new ArrayList<>();
        this.issueReporter = new ConversionIssueReporter();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
    
    public ReadyApiToPostmanConverter() {
        this("");
    }
    
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java -jar converter.jar <input-file> <output-directory> [base-url]");
            System.exit(1);
        }

        String inputFile = args[0];
        String outputDir = args[1];
        String baseUrl = args.length > 2 ? args[2] : "";

        try {
            ReadyApiToPostmanConverter converter = new ReadyApiToPostmanConverter(baseUrl);
            converter.convert(inputFile, outputDir);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Convert a ReadyAPI project to Postman collection
     * 
     * @param readyApiFile Path to the ReadyAPI project file
     * @param outputDirectory Directory to save the output files
     * @return true if conversion was successful, false otherwise
     */
    public boolean convert(String readyApiFile, String outputDirectory) {
        logger.info("Starting conversion of ReadyAPI project: {}", readyApiFile);
        
        try {
            // Create output directory if it doesn't exist
            File outputDir = new File(outputDirectory);
            if (!outputDir.exists()) {
                if (!outputDir.mkdirs()) {
                    logger.error("Failed to create output directory: {}", outputDirectory);
                    return false;
                }
            }
            
            // Parse the ReadyAPI project
            logger.info("Parsing ReadyAPI project...");
            projectParser = new ReadyApiProjectParser();
            ReadyApiProject project;
            
            try {
                project = projectParser.parse(readyApiFile);
                logger.info("Successfully parsed project: {}", project.getName());
            } catch (DocumentException e) {
                logger.error("Failed to parse ReadyAPI project: {}", e.getMessage(), e);
                System.err.println("Error parsing ReadyAPI project: " + e.getMessage());
                return false;
            }
            
            // Create a ConversionIssueReporter
            issueReporter = new ConversionIssueReporter(project.getName());
            
            // Create Postman collection
            logger.info("Creating Postman collection...");
            PostmanCollection collection = convert(project);
            collection.setConversionIssues(conversionIssues);
            
            // Create Postman environment
            logger.info("Creating Postman environment...");
            PostmanEnvironment environment = new PostmanEnvironmentBuilder(project, projectParser.getRootElement(), issueReporter).build();
            
            // Save Postman collection and environment
            String projectName = project.getName();
            String collectionFile = outputDir.getPath() + File.separator + projectName + ".postman_collection.json";
            String environmentFile = outputDir.getPath() + File.separator + projectName + ".postman_environment.json";
            String issuesFile = outputDir.getPath() + File.separator + projectName + "_conversion_issues.txt";
            String reportFile = outputDir.getPath() + File.separator + projectName + "_conversion_report.md";
            
            logger.info("Saving Postman collection to: {}", collectionFile);
            try {
                collection.saveToFile(collectionFile);
            } catch (IOException e) {
                logger.error("Failed to save Postman collection: {}", e.getMessage(), e);
                issueReporter.addError("File I/O", "Failed to save Postman collection: " + e.getMessage());
                return false;
            }
            
            logger.info("Saving Postman environment to: {}", environmentFile);
            try {
                environment.saveToFile(environmentFile);
            } catch (IOException e) {
                logger.error("Failed to save Postman environment: {}", e.getMessage(), e);
                issueReporter.addError("File I/O", "Failed to save Postman environment: " + e.getMessage());
                return false;
            }
            
            // Save any CSV data files
            logger.info("Saving data files...");
            new DataFileExporter(project, outputDir).export();
            
            // Save conversion issues if any
            if (!conversionIssues.isEmpty()) {
                logger.info("Saving conversion issues to: {}", issuesFile);
                ConversionIssueReporter.saveIssues(conversionIssues, issuesFile);
            }
            
            // Save detailed conversion report
            logger.info("Saving conversion report to: {}", reportFile);
            issueReporter.saveReport(outputDir);
            
            logger.info("Validating Postman collection...");
            boolean isValid = new PostmanCollectionValidator().validate(collectionFile);
            if (isValid) {
                logger.info("Postman collection validation successful!");
            } else {
                logger.warn("Postman collection validation failed. See logs for details.");
                issueReporter.addWarning("Validation", "Postman collection validation failed");
            }
            
            // Create a README file with information about function libraries
            createReadmeFile(outputDir, collection);
            
            logger.info("Conversion completed successfully! Files written to {}", outputDir.getAbsolutePath());
            logger.info("Collection file: {}", collectionFile);
            logger.info("Environment file: {}", environmentFile);
            
            // Check if there were any critical errors
            if (issueReporter.hasErrors()) {
                logger.warn("Conversion completed with errors. See report for details.");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            logger.error("Unexpected error during conversion: {}", e.getMessage(), e);
            System.err.println("Error during conversion: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Create a README file with conversion information and instructions
     * 
     * @param outputDir The output directory
     * @param collection The Postman collection
     */
    private void createReadmeFile(File outputDir, PostmanCollection collection) {
        try {
            File readmeFile = new File(outputDir, collection.getInfo().getName() + "_README.md");
            
            try (FileWriter writer = new FileWriter(readmeFile)) {
                writer.write("# " + collection.getInfo().getName() + " - Converted from ReadyAPI\n\n");
                
                writer.write("## About This Collection\n\n");
                writer.write("This Postman collection was automatically converted from a ReadyAPI project using " +
                            "the ReadyAPI to Postman Converter tool.\n\n");
                
                writer.write("## Function Library\n\n");
                writer.write("ReadyAPI projects often use function libraries (Groovy scripts) to provide reusable " +
                            "functionality across test cases. In Postman, these have been converted to JavaScript " +
                            "and are included as a collection variable called `FunctionLibrary`.\n\n");
                
                writer.write("### Using Functions in Postman\n\n");
                writer.write("To use functions from the Function Library in your Postman scripts:\n\n");
                
                writer.write("```javascript\n");
                writer.write("// Get the function library\n");
                writer.write("const RT = JSON.parse(pm.collectionVariables.get('FunctionLibrary'));\n\n");
                
                writer.write("// Use functions from the library\n");
                writer.write("const environment = RT.getEnvironmentType();\n");
                writer.write("RT.logInfo(`Running in ${environment} environment`);\n");
                writer.write("```\n\n");
                
                writer.write("### Available Functions\n\n");
                writer.write("The following functions are available in the library:\n\n");
                
                writer.write("- `getEnvironmentType()` - Returns the current environment (DEV, SIT, UAT)\n");
                writer.write("- `logInfo(message)` - Logs an informational message to the console\n");
                writer.write("- `createLogFile(prefix, filename)` - Simulates log file creation (logs to console in Postman)\n\n");
                
                writer.write("## Environment-Specific Configuration\n\n");
                writer.write("The original ReadyAPI project contained environment-specific settings that have been " +
                            "converted to Postman environment variables. Make sure to select the correct environment " +
                            "before running the tests.\n\n");
                
                writer.write("## Known Limitations\n\n");
                writer.write("1. Some ReadyAPI features don't have direct equivalents in Postman and have been simulated:\n");
                writer.write("   - File operations - Simulated with console logging\n");
                writer.write("   - Database connections - Need to be implemented using Postman's HTTP requests\n");
                writer.write("   - Complex Groovy expressions - May require manual review\n\n");
                
                writer.write("2. Test execution is not directly comparable to ReadyAPI:\n");
                writer.write("   - ReadyAPI's test case and test suite structure is represented as folders in Postman\n");
                writer.write("   - Data sources are implemented as environment variables or collection variables\n\n");
                
                writer.write("## Contact\n\n");
                writer.write("If you encounter any issues with the converted collection, please contact your API team.\n");
            }
            
            logger.info("Created README file: {}", readmeFile.getPath());
        } catch (IOException e) {
            logger.error("Error creating README file", e);
            issueReporter.addWarning("Documentation", "Failed to create README file: " + e.getMessage());
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
    
    /**
     * Get the issue reporter
     * 
     * @return The ConversionIssueReporter
     */
    public ConversionIssueReporter getIssueReporter() {
        return issueReporter;
    }
    
    /**
     * Convert a ReadyAPI project to Postman collection
     * 
     * @param project The ReadyAPI project to convert
     * @return The converted Postman collection
     */
    public PostmanCollection convert(ReadyApiProject project) {
        PostmanCollection collection = new PostmanCollection();
        
        // Set collection info
        PostmanCollection.PostmanInfo info = new PostmanCollection.PostmanInfo(project.getName());
        info.setDescription(project.getDescription());
        collection.setInfo(info);
        
        // Convert interfaces to folders
        for (ReadyApiInterface apiInterface : project.getInterfaces()) {
            com.readyapi.converter.postman.PostmanItem interfaceFolder = new com.readyapi.converter.postman.PostmanItem();
            interfaceFolder.setName(apiInterface.getName());
            
            // Convert resources to requests
            for (ReadyApiResource resource : apiInterface.getResources()) {
                com.readyapi.converter.postman.PostmanItem requestItem = convertResourceToRequest(resource);
                interfaceFolder.addItem(requestItem);
            }
            
            collection.addItem(interfaceFolder);
        }
        
        // Add variables
        Map<String, String> properties = project.getProperties();
        if (properties != null) {
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                collection.addVariable(new PostmanCollection.PostmanVariable(
                    entry.getKey(),
                    entry.getValue(),
                    "string",
                    false
                ));
            }
        }
        
        return collection;
    }
    
    private com.readyapi.converter.postman.PostmanItem convertResourceToRequest(ReadyApiResource resource) {
        com.readyapi.converter.postman.PostmanItem requestItem = new com.readyapi.converter.postman.PostmanItem();
        requestItem.setName(resource.getName());
        
        PostmanRequest request = new PostmanRequest();
        
        // Set URL
        PostmanRequest.PostmanUrl url = new PostmanRequest.PostmanUrl();
        url.setRaw(baseUrl + resource.getPath());
        url.setProtocol("http");
        url.setHost(new ArrayList<>());
        url.getHost().add("{{baseUrl}}");
        url.setPath(new ArrayList<>());
        for (String pathSegment : resource.getPath().split("/")) {
            if (!pathSegment.isEmpty()) {
                url.getPath().add(pathSegment);
            }
        }
        request.setUrl(url);
        
        // Set headers
        for (ReadyApiMethod method : resource.getMethods()) {
            for (ReadyApiRequest readyRequest : method.getRequests()) {
                for (Map.Entry<String, String> header : readyRequest.getRequestHeaders().entrySet()) {
                    PostmanRequest.PostmanHeader postmanHeader = new PostmanRequest.PostmanHeader(
                        header.getKey(), header.getValue());
                    request.getHeader().add(postmanHeader);
                }
                
                // Set body
                if (readyRequest.getRequestBody() != null && !readyRequest.getRequestBody().isEmpty()) {
                    PostmanRequest.PostmanBody body = new PostmanRequest.PostmanBody();
                    body.setMode("raw");
                    body.setRaw(readyRequest.getRequestBody());
                    
                    PostmanRequest.PostmanBody.PostmanBodyOptions options = 
                        new PostmanRequest.PostmanBody.PostmanBodyOptions();
                    
                    PostmanRequest.PostmanBody.PostmanBodyOptions.PostmanRawOptions rawOptions = 
                        new PostmanRequest.PostmanBody.PostmanBodyOptions.PostmanRawOptions();
                    
                    if (readyRequest.getMediaType() != null) {
                        if (readyRequest.getMediaType().contains("json")) {
                            rawOptions.setLanguage("json");
                        } else if (readyRequest.getMediaType().contains("xml")) {
                            rawOptions.setLanguage("xml");
                        } else {
                            rawOptions.setLanguage("text");
                        }
                    } else {
                        rawOptions.setLanguage("text");
                    }
                    
                    options.setRaw(rawOptions);
                    body.setOptions(options);
                    
                    request.setBody(body);
                }
            }
        }
        
        requestItem.setRequest(request);
        return requestItem;
    }
}
