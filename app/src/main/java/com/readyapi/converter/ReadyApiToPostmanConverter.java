package com.readyapi.converter;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Paths;
import java.io.FileWriter;
import org.dom4j.Element;

/**
 * Main class for converting ReadyAPI projects to Postman collections.
 */
public class ReadyApiToPostmanConverter {
    // private static final Logger logger = LoggerFactory.getLogger(ReadyApiToPostmanConverter.class);
    
    // List to track items that couldn't be converted
    private final List<String> conversionIssues = new ArrayList<>();
    
    public static void main(String[] args) {
        // Parse command line arguments
        if (args.length < 1) {
            System.out.println("Usage: ReadyApiToPostmanConverter <input-file> [output-directory]");
            System.out.println("  <input-file>       : Path to ReadyAPI project XML file");
            System.out.println("  [output-directory] : Optional directory to save output files (defaults to ./output)");
            return;
        }
        
        // Get the input file path from command line arguments
        String inputFilePath = args[0];
        
        // Get the output directory from command line arguments or use default
        String outputDirectory = args.length > 1 ? args[1] : "./output";
        
        // Get the current working directory
        String currentPath = new File(".").getAbsolutePath();
        System.out.println("Current directory: " + currentPath);
        
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
            ReadyApiProjectParser parser = new ReadyApiProjectParser();
            ReadyApiProject project = parser.parse(readyApiFile);
            
            // Create a ConversionIssueReporter
            ConversionIssueReporter issueReporter = new ConversionIssueReporter(project.getName());
            
            // Create Postman collection
            System.out.println("Creating Postman collection...");
            PostmanCollectionBuilder collectionBuilder = new PostmanCollectionBuilder(project);
            PostmanCollection collection = collectionBuilder.build();
            collection.setConversionIssues(collectionBuilder.getConversionIssues());
            
            // Create Postman environment
            System.out.println("Creating Postman environment...");
            PostmanEnvironment environment = new PostmanEnvironmentBuilder(project, parser.getRootElement(), issueReporter).build();
            
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
            
            // Create a README file with information about function libraries
            createReadmeFile(outputDir, collection);
            
            System.out.println("Conversion completed successfully! Files written to " + outputDir.getAbsolutePath());
            System.out.println("Collection file: " + collectionFile);
            System.out.println("Environment file: " + environmentFile);
            
            return;
        } catch (Exception e) {
            System.err.println("Error during conversion: " + e.getMessage());
            e.printStackTrace();
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
            
            // logger.info("Created README file: {}", readmeFile.getPath());
        } catch (IOException e) {
            // logger.error("Error creating README file", e);
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
