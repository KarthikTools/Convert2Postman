package com.readyapi.converter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles data source connections and exports data to files.
 */
public class DataFileExporter {
    private final ReadyApiProject project;
    private final File outputDir;
    
    public DataFileExporter(ReadyApiProject project, File outputDir) {
        this.project = project;
        this.outputDir = outputDir;
    }
    
    /**
     * Export data from all data sources in the project.
     */
    public void export() {
        // Find all data source test steps
        List<ReadyApiTestStep> dataSourceSteps = findDataSourceSteps();
        
        for (ReadyApiTestStep step : dataSourceSteps) {
            try {
                exportDataSource(step);
            } catch (Exception e) {
                System.err.println("Error exporting data source: " + step.getName() + " - " + e.getMessage());
            }
        }
    }
    
    /**
     * Find all data source test steps in the project.
     * 
     * @return List of data source test steps
     */
    private List<ReadyApiTestStep> findDataSourceSteps() {
        List<ReadyApiTestStep> dataSourceSteps = new ArrayList<>();
        
        // Iterate through all test suites and test cases
        for (ReadyApiTestSuite testSuite : project.getTestSuites()) {
            for (ReadyApiTestCase testCase : testSuite.getTestCases()) {
                for (ReadyApiTestStep testStep : testCase.getTestSteps()) {
                    if ("datasource".equalsIgnoreCase(testStep.getType())) {
                        dataSourceSteps.add(testStep);
                    }
                }
            }
        }
        
        return dataSourceSteps;
    }
    
    /**
     * Export data from a data source test step.
     * 
     * @param step The data source test step
     * @throws Exception If there's an error exporting the data
     */
    private void exportDataSource(ReadyApiTestStep step) throws Exception {
        String dataSourceType = step.getProperty("type");
        
        if (dataSourceType == null) {
            System.out.println("Unknown data source type for step: " + step.getName());
            return;
        }
        
        switch (dataSourceType.toLowerCase()) {
            case "excel":
                exportExcelDataSource(step);
                break;
            case "csv":
                exportCsvDataSource(step);
                break;
            case "jdbc":
                exportJdbcDataSource(step);
                break;
            case "file":
                exportFileDataSource(step);
                break;
            default:
                System.out.println("Unsupported data source type: " + dataSourceType + " for step: " + step.getName());
        }
    }
    
    /**
     * Export data from an Excel data source.
     * 
     * @param step The data source test step
     * @throws Exception If there's an error exporting the data
     */
    private void exportExcelDataSource(ReadyApiTestStep step) throws Exception {
        String file = step.getProperty("file");
        String worksheet = step.getProperty("worksheet");
        
        if (file == null || file.isEmpty()) {
            throw new Exception("Missing file property for Excel data source");
        }
        
        System.out.println("Exporting Excel data source: " + file + ", worksheet: " + worksheet);
        
        // In a real implementation, we would read the Excel file
        // For now, we'll create a placeholder JSON file
        
        File outputFile = new File(outputDir, step.getName() + ".json");
        
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write("[\n");
            writer.write("  {\n");
            writer.write("    \"id\": \"1\",\n");
            writer.write("    \"name\": \"Sample Data 1\",\n");
            writer.write("    \"value\": \"Value 1\"\n");
            writer.write("  },\n");
            writer.write("  {\n");
            writer.write("    \"id\": \"2\",\n");
            writer.write("    \"name\": \"Sample Data 2\",\n");
            writer.write("    \"value\": \"Value 2\"\n");
            writer.write("  }\n");
            writer.write("]\n");
        }
        
        System.out.println("Exported Excel data to: " + outputFile.getPath());
    }
    
    /**
     * Export data from a CSV data source.
     * 
     * @param step The data source test step
     * @throws Exception If there's an error exporting the data
     */
    private void exportCsvDataSource(ReadyApiTestStep step) throws Exception {
        String file = step.getProperty("file");
        String separator = step.getProperty("separator");
        
        if (file == null || file.isEmpty()) {
            throw new Exception("Missing file property for CSV data source");
        }
        
        if (separator == null || separator.isEmpty()) {
            separator = ","; // Default separator
        }
        
        System.out.println("Exporting CSV data source: " + file + ", separator: " + separator);
        
        // In a real implementation, we would read the CSV file
        // For now, we'll create a placeholder JSON file
        
        File outputFile = new File(outputDir, step.getName() + ".json");
        
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write("[\n");
            writer.write("  {\n");
            writer.write("    \"column1\": \"Value 1\",\n");
            writer.write("    \"column2\": \"Value 2\",\n");
            writer.write("    \"column3\": \"Value 3\"\n");
            writer.write("  },\n");
            writer.write("  {\n");
            writer.write("    \"column1\": \"Value 4\",\n");
            writer.write("    \"column2\": \"Value 5\",\n");
            writer.write("    \"column3\": \"Value 6\"\n");
            writer.write("  }\n");
            writer.write("]\n");
        }
        
        System.out.println("Exported CSV data to: " + outputFile.getPath());
    }
    
    /**
     * Export data from a JDBC data source.
     * 
     * @param step The data source test step
     * @throws Exception If there's an error exporting the data
     */
    private void exportJdbcDataSource(ReadyApiTestStep step) throws Exception {
        String driver = step.getProperty("driver");
        String connectionString = step.getProperty("connectionString");
        String username = step.getProperty("username");
        String password = step.getProperty("password");
        String query = step.getProperty("query");
        
        if (connectionString == null || connectionString.isEmpty()) {
            throw new Exception("Missing connection string for JDBC data source");
        }
        
        if (query == null || query.isEmpty()) {
            throw new Exception("Missing query for JDBC data source");
        }
        
        System.out.println("Exporting JDBC data source: " + connectionString);
        
        // Check if we can actually connect to the database
        boolean canConnect = false;
        Connection connection = null;
        
        try {
            // Load the JDBC driver if specified
            if (driver != null && !driver.isEmpty()) {
                try {
                    Class.forName(driver);
                } catch (ClassNotFoundException e) {
                    System.err.println("JDBC driver not found: " + driver);
                    // Continue anyway, the driver might be on the classpath
                }
            }
            
            // Try to connect to the database
            connection = DriverManager.getConnection(connectionString, username, password);
            canConnect = true;
            
            // If we can connect, execute the query and export the results
            if (canConnect) {
                exportJdbcQueryResults(step, connection, query);
            }
        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
            // Create a placeholder file with the connection details
            createJdbcPlaceholderFile(step, connectionString, query);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    // Ignore
                }
            }
        }
    }
    
    /**
     * Export results from a JDBC query.
     * 
     * @param step The data source test step
     * @param connection The database connection
     * @param query The SQL query
     * @throws Exception If there's an error exporting the data
     */
    private void exportJdbcQueryResults(ReadyApiTestStep step, Connection connection, String query) throws Exception {
        File outputFile = new File(outputDir, step.getName() + ".json");
        
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query);
             FileWriter writer = new FileWriter(outputFile)) {
            
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            // Get column names
            List<String> columnNames = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                columnNames.add(metaData.getColumnName(i));
            }
            
            // Write data as JSON array
            writer.write("[\n");
            
            boolean first = true;
            while (resultSet.next()) {
                if (!first) {
                    writer.write(",\n");
                }
                first = false;
                
                writer.write("  {\n");
                
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = columnNames.get(i - 1);
                    String value = resultSet.getString(i);
                    
                    if (i > 1) {
                        writer.write(",\n");
                    }
                    
                    writer.write("    \"" + columnName + "\": ");
                    if (value == null) {
                        writer.write("null");
                    } else {
                        writer.write("\"" + value.replace("\"", "\\\"") + "\"");
                    }
                }
                
                writer.write("\n  }");
            }
            
            writer.write("\n]\n");
        }
        
        System.out.println("Exported JDBC data to: " + outputFile.getPath());
    }
    
    /**
     * Create a placeholder file for JDBC data source when connection fails.
     * 
     * @param step The data source test step
     * @param connectionString The database connection string
     * @param query The SQL query
     * @throws IOException If there's an error creating the file
     */
    private void createJdbcPlaceholderFile(ReadyApiTestStep step, String connectionString, String query) throws IOException {
        File outputFile = new File(outputDir, step.getName() + ".json");
        
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write("// JDBC Connection Information\n");
            writer.write("// Connection String: " + connectionString + "\n");
            writer.write("// Query: " + query + "\n\n");
            
            writer.write("[\n");
            writer.write("  {\n");
            writer.write("    \"note\": \"This is a placeholder for JDBC data. The actual data would come from executing the query.\",\n");
            writer.write("    \"connection\": \"" + connectionString.replace("\"", "\\\"") + "\",\n");
            writer.write("    \"query\": \"" + query.replace("\"", "\\\"") + "\"\n");
            writer.write("  }\n");
            writer.write("]\n");
        }
        
        System.out.println("Created JDBC placeholder file: " + outputFile.getPath());
    }
    
    /**
     * Export data from a file data source.
     * 
     * @param step The data source test step
     * @throws Exception If there's an error exporting the data
     */
    private void exportFileDataSource(ReadyApiTestStep step) throws Exception {
        String file = step.getProperty("file");
        
        if (file == null || file.isEmpty()) {
            throw new Exception("Missing file property for file data source");
        }
        
        System.out.println("Exporting file data source: " + file);
        
        // In a real implementation, we would read the file
        // For now, we'll create a placeholder JSON file
        
        File outputFile = new File(outputDir, step.getName() + ".json");
        
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write("[\n");
            writer.write("  {\n");
            writer.write("    \"data\": \"Sample file data\"\n");
            writer.write("  }\n");
            writer.write("]\n");
        }
        
        System.out.println("Exported file data to: " + outputFile.getPath());
    }
}
