package com.readyapi.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for exporting data files from ReadyAPI projects.
 * Handles various data sources including CSV and properties files.
 */
public class DataFileExporter {
    private static final Logger logger = LoggerFactory.getLogger(DataFileExporter.class);
    
    private final ReadyApiProject project;
    private final File outputDir;
    private final File projectBase;
    
    /**
     * Pattern to match Excel cell references (e.g., Sheet1!A1:D10)
     */
    private static final Pattern EXCEL_CELL_REFERENCE_PATTERN = 
            Pattern.compile("([^!]+)!([A-Z]+)(\\d+)(?::([A-Z]+)(\\d+))?");
    
    public DataFileExporter(ReadyApiProject project, File outputDir) {
        this.project = project;
        this.outputDir = outputDir;
        
        // Determine the project base directory (containing the XML file)
        String projectPath = project.getProperty("projectPath");
        if (projectPath != null && !projectPath.isEmpty()) {
            this.projectBase = new File(projectPath);
        } else {
            // Default to the current directory
            this.projectBase = new File(".");
        }
    }
    
    /**
     * Export data files from the ReadyAPI project.
     */
    public void export() {
        logger.info("Exporting data files from ReadyAPI project: {}", project.getName());
        
        // Create a data directory
        File dataDir = new File(outputDir, "data");
        if (!dataDir.exists() && !dataDir.mkdirs()) {
            logger.error("Failed to create data directory: {}", dataDir.getPath());
            return;
        }
        
        // Process all data sources in the project
        extractDataSourcesFromProject(dataDir);
        
        logger.info("Exported data files to: {}", dataDir.getPath());
    }
    
    /**
     * Extract all data sources from the project.
     * 
     * @param dataDir Directory to save the data files
     */
    private void extractDataSourcesFromProject(File dataDir) {
        // Extract from project properties
        extractDataSourceFromProperties(project.getProperties(), dataDir, "project");
        
        // Extract from test suites
        for (ReadyApiTestSuite testSuite : project.getTestSuites()) {
            // Extract from test suite properties
            extractDataSourceFromProperties(testSuite.getProperties(), dataDir, testSuite.getName());
            
            // Look for data-driven test cases
            for (ReadyApiTestCase testCase : testSuite.getTestCases()) {
                // Extract from test case properties
                extractDataSourceFromProperties(testCase.getProperties(), dataDir, 
                        testSuite.getName() + "_" + testCase.getName());
                
                // Process data source test steps
                for (ReadyApiTestStep testStep : testCase.getTestSteps()) {
                    if ("datasource".equalsIgnoreCase(testStep.getType())) {
                        processDataSourceStep(testStep, dataDir, testSuite.getName(), testCase.getName());
                    } else if ("properties".equalsIgnoreCase(testStep.getType())) {
                        processPropertiesStep(testStep, dataDir, testSuite.getName(), testCase.getName());
                    } else if ("datasink".equalsIgnoreCase(testStep.getType())) {
                        processDataSinkStep(testStep, dataDir, testSuite.getName(), testCase.getName());
                    }
                }
            }
        }
    }
    
    /**
     * Extract data source information from properties.
     * 
     * @param properties Map of properties
     * @param dataDir Directory to save data files
     * @param prefix Prefix for file names
     */
    private void extractDataSourceFromProperties(Map<String, String> properties, File dataDir, String prefix) {
        String dataSource = properties.get("DataSource");
        if (dataSource != null && !dataSource.isEmpty()) {
            try {
                if (isExternalFilePath(dataSource)) {
                    // External file path
                    processExternalDataSource(dataSource, dataDir, prefix);
                } else if (isExcelReference(dataSource)) {
                    // Excel reference - just create a placeholder with information
                    createExcelReferencePlaceholder(dataSource, properties, dataDir, prefix);
                } else {
                    // Inline data
                    generateCSVFromInlineData(dataDir, prefix, dataSource);
                }
            } catch (IOException e) {
                logger.error("Error processing data source: {}", dataSource, e);
            }
        }
    }
    
    /**
     * Process a data source test step.
     * 
     * @param testStep The data source test step
     * @param dataDir Directory to save data files
     * @param testSuiteName Name of the test suite
     * @param testCaseName Name of the test case
     */
    private void processDataSourceStep(ReadyApiTestStep testStep, File dataDir, 
                                      String testSuiteName, String testCaseName) {
        Map<String, String> properties = testStep.getProperties();
        String dataSource = properties.get("sourceStep");
        String dataSourceType = properties.get("type");
        
        if (dataSource == null || dataSource.isEmpty()) {
            dataSource = testStep.getProperty("file");
        }
        
        if (dataSource == null || dataSource.isEmpty()) {
            logger.warn("No data source found for test step: {}", testStep.getName());
            return;
        }
        
        try {
            String prefix = testSuiteName + "_" + testCaseName + "_" + testStep.getName();
            
            if ("EXCEL".equalsIgnoreCase(dataSourceType)) {
                // Handle Excel data source - just copy the file
                String excelFile = properties.get("file");
                if (excelFile != null && !excelFile.isEmpty()) {
                    File sourceFile = resolveFilePath(excelFile);
                    if (sourceFile.exists()) {
                        copyFile(sourceFile, dataDir, prefix + ".excel_info.txt");
                        
                        // Include information about cell range in a text file
                        String worksheet = properties.getOrDefault("worksheet", "");
                        String startCell = properties.getOrDefault("startCell", "");
                        String endCell = properties.getOrDefault("endCell", "");
                        
                        createExcelInfoFile(dataDir, prefix, excelFile, worksheet, startCell, endCell);
                    } else {
                        logger.warn("Excel file not found: {}", excelFile);
                    }
                } else if (isExcelReference(dataSource)) {
                    createExcelReferencePlaceholder(dataSource, properties, dataDir, prefix);
                }
            } else if ("FILE".equalsIgnoreCase(dataSourceType) || isExternalFilePath(dataSource)) {
                // Handle file data source
                processExternalDataSource(dataSource, dataDir, prefix);
            } else if ("JDBC".equalsIgnoreCase(dataSourceType)) {
                // JDBC data sources need to be handled separately in Postman
                logger.warn("JDBC data sources are not directly supported in Postman: {}", testStep.getName());
                generatePlaceholderFile(dataDir, prefix, "JDBC_DATA_SOURCE");
            } else {
                // Handle inline data
                generateCSVFromInlineData(dataDir, prefix, dataSource);
            }
        } catch (IOException e) {
            logger.error("Error processing data source step: {}", testStep.getName(), e);
        }
    }
    
    /**
     * Process a datasink test step.
     * 
     * @param testStep The datasink test step
     * @param dataDir Directory to save data files
     * @param testSuiteName Name of the test suite
     * @param testCaseName Name of the test case
     */
    private void processDataSinkStep(ReadyApiTestStep testStep, File dataDir, 
                                    String testSuiteName, String testCaseName) {
        Map<String, String> properties = testStep.getProperties();
        String targetStep = properties.get("targetStep");
        String format = properties.get("format");
        String file = properties.get("file");
        
        if (file == null || file.isEmpty()) {
            logger.warn("No output file specified for datasink step: {}", testStep.getName());
            return;
        }
        
        try {
            String prefix = testSuiteName + "_" + testCaseName + "_" + testStep.getName();
            
            // Create a reference file in the data directory
            File referenceFile = new File(dataDir, prefix + ".datasink_info.txt");
            
            try (FileWriter writer = new FileWriter(referenceFile)) {
                writer.write("DataSink Step: " + testStep.getName() + "\n");
                writer.write("Target File: " + file + "\n");
                
                if (format != null && !format.isEmpty()) {
                    writer.write("Format: " + format + "\n");
                }
                
                if (targetStep != null && !targetStep.isEmpty()) {
                    writer.write("Target Step: " + targetStep + "\n");
                }
                
                // Write information about column mappings if present
                String columnMappings = properties.get("columnMappings");
                if (columnMappings != null && !columnMappings.isEmpty()) {
                    writer.write("\nColumn Mappings:\n");
                    String[] mappings = columnMappings.split(";");
                    for (String mapping : mappings) {
                        if (!mapping.isEmpty()) {
                            writer.write("- " + mapping.replace("=", " → ") + "\n");
                        }
                    }
                }
                
                writer.write("\nNOTE: In Postman, data export functionality can be implemented using:\n");
                writer.write("1. Newman custom reporters for automated exports\n");
                writer.write("2. Console output and manual copying for ad-hoc exports\n");
                writer.write("3. Environment variables to store temporary data\n");
            }
            
            logger.info("Created datasink reference file: {}", referenceFile.getPath());
            
            // If the target file is an Excel file, create an Excel info file
            if (file.toLowerCase().endsWith(".xls") || file.toLowerCase().endsWith(".xlsx")) {
                String worksheet = properties.get("worksheet");
                createExcelInfoFile(dataDir, prefix + "_output", file, 
                                   worksheet != null ? worksheet : "", "", "");
            }
        } catch (IOException e) {
            logger.error("Error processing datasink step: {}", testStep.getName(), e);
        }
    }
    
    /**
     * Process a properties test step.
     * 
     * @param testStep The properties test step
     * @param dataDir Directory to save data files
     * @param testSuiteName Name of the test suite
     * @param testCaseName Name of the test case
     */
    private void processPropertiesStep(ReadyApiTestStep testStep, File dataDir, 
                                      String testSuiteName, String testCaseName) {
        String content = testStep.getContent();
        if (content == null || content.isEmpty()) {
            return;
        }
        
        try {
            String prefix = testSuiteName + "_" + testCaseName + "_" + testStep.getName();
            File propertiesFile = new File(dataDir, prefix + ".properties");
            
            try (FileWriter writer = new FileWriter(propertiesFile)) {
                writer.write(content);
            }
            
            logger.info("Created properties file: {}", propertiesFile.getPath());
        } catch (IOException e) {
            logger.error("Error creating properties file for test step: {}", testStep.getName(), e);
        }
    }
    
    /**
     * Check if the data source is an external file path.
     * 
     * @param dataSource Data source string
     * @return True if it's an external file path
     */
    private boolean isExternalFilePath(String dataSource) {
        return dataSource.contains("/") || dataSource.contains("\\") || 
               dataSource.endsWith(".csv") || dataSource.endsWith(".xls") || 
               dataSource.endsWith(".xlsx") || dataSource.endsWith(".properties");
    }
    
    /**
     * Check if the data source is an Excel reference.
     * 
     * @param dataSource Data source string
     * @return True if it's an Excel reference
     */
    private boolean isExcelReference(String dataSource) {
        return EXCEL_CELL_REFERENCE_PATTERN.matcher(dataSource).matches();
    }
    
    /**
     * Process an external data source file.
     * 
     * @param dataSource Data source path
     * @param dataDir Directory to save data files
     * @param prefix Prefix for file names
     * @throws IOException If there's an error processing the file
     */
    private void processExternalDataSource(String dataSource, File dataDir, String prefix) throws IOException {
        File sourceFile = resolveFilePath(dataSource);
        
        if (!sourceFile.exists()) {
            logger.warn("Data source file not found: {}", sourceFile.getPath());
            return;
        }
        
        String extension = FilenameUtils.getExtension(sourceFile.getName()).toLowerCase();
        String targetFileName = prefix + "." + extension;
        File targetFile = new File(dataDir, targetFileName);
        
        // Copy the file to the data directory
        copyFile(sourceFile, dataDir, targetFileName);
        logger.info("Copied data source file to: {}", targetFile.getPath());
        
        // For Excel files, create an info file with details
        if (extension.equals("xls") || extension.equals("xlsx")) {
            createExcelInfoFile(dataDir, prefix, sourceFile.getPath(), "", "", "");
        }
    }
    
    /**
     * Create a placeholder file for Excel references.
     * 
     * @param excelReference Excel reference string
     * @param properties Properties map that may contain file information
     * @param dataDir Directory to save the placeholder
     * @param prefix Prefix for the filename
     * @throws IOException If there's an error creating the file
     */
    private void createExcelReferencePlaceholder(String excelReference, Map<String, String> properties, 
                                               File dataDir, String prefix) throws IOException {
        Matcher matcher = EXCEL_CELL_REFERENCE_PATTERN.matcher(excelReference);
        if (!matcher.matches()) {
            logger.warn("Invalid Excel reference format: {}", excelReference);
            return;
        }
        
        String sheetName = matcher.group(1);
        String startColumn = matcher.group(2);
        String startRow = matcher.group(3);
        String endColumn = matcher.group(4); // May be null
        String endRow = matcher.group(5); // May be null
        
        // Find the Excel file path
        String excelFile = properties.get("ExcelFile");
        if (excelFile == null || excelFile.isEmpty()) {
            // Look for other common property names
            excelFile = properties.getOrDefault("file", "");
            if (excelFile.isEmpty()) {
                excelFile = properties.getOrDefault("filename", "");
            }
        }
        
        // Create the info file
        createExcelInfoFile(dataDir, prefix, excelFile, sheetName, 
                            startColumn + startRow, 
                            (endColumn != null && endRow != null) ? endColumn + endRow : "");
    }
    
    /**
     * Create an Excel info file with details about the Excel source.
     * 
     * @param dataDir Directory to save the info file
     * @param prefix Prefix for the filename
     * @param excelFile Excel file path
     * @param worksheet Worksheet name
     * @param startCell Start cell (e.g. "A1")
     * @param endCell End cell (e.g. "D10")
     * @throws IOException If there's an error creating the file
     */
    private void createExcelInfoFile(File dataDir, String prefix, String excelFile, 
                                    String worksheet, String startCell, String endCell) throws IOException {
        File infoFile = new File(dataDir, prefix + ".excel_info.txt");
        
        try (FileWriter writer = new FileWriter(infoFile)) {
            writer.write("Excel File: " + excelFile + "\n");
            if (!worksheet.isEmpty()) {
                writer.write("Worksheet: " + worksheet + "\n");
            }
            if (!startCell.isEmpty()) {
                writer.write("Start Cell: " + startCell + "\n");
            }
            if (!endCell.isEmpty()) {
                writer.write("End Cell: " + endCell + "\n");
            }
            writer.write("\n");
            writer.write("NOTE: To use this data in Postman, please manually extract the data from the Excel file\n");
            writer.write("and import it as a CSV file or a collection variable.\n");
        }
        
        logger.info("Created Excel info file: {}", infoFile.getPath());
    }
    
    /**
     * Resolve a file path relative to the project base directory.
     * 
     * @param filePath File path, may be relative or absolute
     * @return Resolved File object
     */
    private File resolveFilePath(String filePath) {
        File file = new File(filePath);
        if (file.isAbsolute()) {
            return file;
        }
        
        // Try project base directory
        file = new File(projectBase, filePath);
        if (file.exists()) {
            return file;
        }
        
        // Try data directory inside project
        file = new File(projectBase, "data/" + filePath);
        if (file.exists()) {
            return file;
        }
        
        // Try common data directories
        String[] commonDataDirs = {"Data", "TestData", "input", "inputdata"};
        for (String dir : commonDataDirs) {
            file = new File(projectBase, dir + "/" + filePath);
            if (file.exists()) {
                return file;
            }
        }
        
        // Default back to the original path
        return new File(filePath);
    }
    
    /**
     * Copy a file to the target location.
     * 
     * @param sourceFile Source file
     * @param destDir Destination directory
     * @param destFileName Destination file name
     * @throws IOException If there's an error copying the file
     */
    private void copyFile(File sourceFile, File destDir, String destFileName) throws IOException {
        File destFile = new File(destDir, destFileName);
        Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
    
    /**
     * Generate a CSV file from inline data.
     * 
     * @param dataDir Directory to save the CSV file
     * @param prefix Prefix for the CSV file name
     * @param dataSource Inline data source string
     * @throws IOException If there's an error writing the file
     */
    private void generateCSVFromInlineData(File dataDir, String prefix, String dataSource) throws IOException {
        File csvFile = new File(dataDir, prefix + ".csv");
        
        try (FileWriter writer = new FileWriter(csvFile)) {
            // Parse the data source and write to CSV
            String[] lines = dataSource.split("\\r?\\n");
            
            for (String line : lines) {
                writer.write(line);
                writer.write(System.lineSeparator());
            }
        }
        
        logger.info("Created CSV file from inline data: {}", csvFile.getPath());
    }
    
    /**
     * Generate a placeholder file for unsupported data sources.
     * 
     * @param dataDir Directory to save the file
     * @param prefix Prefix for the file name
     * @param type Type of unsupported data source
     * @throws IOException If there's an error writing the file
     */
    private void generatePlaceholderFile(File dataDir, String prefix, String type) throws IOException {
        File placeholderFile = new File(dataDir, prefix + "_placeholder.txt");
        
        try (FileWriter writer = new FileWriter(placeholderFile)) {
            writer.write("This is a placeholder for a " + type + " data source.\n");
            writer.write("Postman does not directly support this type of data source.\n");
            writer.write("You may need to export the data manually and import it into Postman.");
        }
        
        logger.info("Created placeholder file for {} data source: {}", type, placeholderFile.getPath());
    }
    
    /**
     * Escape a string for CSV output.
     * 
     * @param text Text to escape
     * @return Escaped text
     */
    private String csvEscape(String text) {
        if (text == null) {
            return "";
        }
        
        if (text.contains("\"") || text.contains(",") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        
        return text;
    }
} 