package com.readyapi.converter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for reporting conversion issues and generating detailed reports.
 */
public class ConversionIssueReporter {
    private static final Logger logger = LoggerFactory.getLogger(ConversionIssueReporter.class);
    
    private List<ConversionIssue> issues = new ArrayList<>();
    private String projectName;
    
    /**
     * Create a new ConversionIssueReporter.
     * 
     * @param projectName Name of the project being converted
     */
    public ConversionIssueReporter(String projectName) {
        this.projectName = projectName;
    }
    
    /**
     * Static method to save a list of conversion issues to a file.
     * 
     * @param issues The list of issue descriptions to save
     * @param filePath The path to the file to save to
     */
    public static void saveIssues(List<String> issues, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("# Conversion Issues\n\n");
            writer.write("The following issues were encountered during conversion:\n\n");
            
            for (String issue : issues) {
                writer.write("- " + issue + "\n");
            }
            
            logger.info("Conversion issues saved to: {}", filePath);
        } catch (IOException e) {
            logger.error("Error saving conversion issues to file: {}", filePath, e);
        }
    }
    
    /**
     * Add an issue with the specified severity.
     * 
     * @param component Component where the issue occurred
     * @param description Description of the issue
     * @param severity Severity level of the issue
     */
    public void addIssue(String component, String description, Severity severity) {
        ConversionIssue issue = new ConversionIssue(component, description, severity);
        issues.add(issue);
        
        // Log based on severity
        switch (severity) {
            case ERROR:
                logger.error("{}: {}", component, description);
                break;
            case WARNING:
                logger.warn("{}: {}", component, description);
                break;
            case INFO:
                logger.info("{}: {}", component, description);
                break;
        }
    }
    
    /**
     * Add an error.
     * 
     * @param component Component where the error occurred
     * @param description Description of the error
     */
    public void addError(String component, String description) {
        addIssue(component, description, Severity.ERROR);
    }
    
    /**
     * Add a warning.
     * 
     * @param component Component where the warning occurred
     * @param description Description of the warning
     */
    public void addWarning(String component, String description) {
        addIssue(component, description, Severity.WARNING);
    }
    
    /**
     * Add an information message.
     * 
     * @param component Component where the info occurred
     * @param description Description of the info
     */
    public void addInfo(String component, String description) {
        addIssue(component, description, Severity.INFO);
    }
    
    /**
     * Gets all issues.
     * 
     * @return List of all conversion issues
     */
    public List<ConversionIssue> getIssues() {
        return issues;
    }
    
    /**
     * Check if there are any errors.
     * 
     * @return True if there are errors
     */
    public boolean hasErrors() {
        return issues.stream().anyMatch(issue -> issue.getSeverity() == Severity.ERROR);
    }
    
    /**
     * Get count of issues by severity.
     * 
     * @param severity Severity level to count
     * @return Count of issues with given severity
     */
    public int countIssues(Severity severity) {
        return (int) issues.stream().filter(issue -> issue.getSeverity() == severity).count();
    }
    
    /**
     * Generate a detailed report of all conversion issues.
     * 
     * @return Report as a string
     */
    public String generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("# Conversion Report for ").append(projectName).append("\n\n");
        report.append("Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\n\n");
        
        report.append("## Summary\n\n");
        report.append("- Errors: ").append(countIssues(Severity.ERROR)).append("\n");
        report.append("- Warnings: ").append(countIssues(Severity.WARNING)).append("\n");
        report.append("- Info: ").append(countIssues(Severity.INFO)).append("\n\n");
        
        // Group issues by component
        Map<String, List<ConversionIssue>> issuesByComponent = issues.stream()
                .collect(Collectors.groupingBy(ConversionIssue::getComponent));
        
        for (Map.Entry<String, List<ConversionIssue>> entry : issuesByComponent.entrySet()) {
            report.append("## ").append(entry.getKey()).append("\n\n");
            
            // Group by severity within component
            Map<Severity, List<ConversionIssue>> issuesBySeverity = entry.getValue().stream()
                    .collect(Collectors.groupingBy(ConversionIssue::getSeverity));
            
            for (Severity severity : Severity.values()) {
                List<ConversionIssue> severityIssues = issuesBySeverity.get(severity);
                if (severityIssues != null && !severityIssues.isEmpty()) {
                    report.append("### ").append(severity).append("\n\n");
                    for (ConversionIssue issue : severityIssues) {
                        report.append("- ").append(issue.getDescription()).append("\n");
                    }
                    report.append("\n");
                }
            }
        }
        
        // Add recommendations for common error patterns
        if (hasErrors()) {
            report.append("## Recommendations\n\n");
            
            // Check for common patterns and provide recommendations
            if (issues.stream().anyMatch(i -> i.getDescription().contains("XPath"))) {
                report.append("### XPath Issues\n\n");
                report.append("XPath expressions in ReadyAPI don't translate directly to Postman. Consider:\n");
                report.append("- Using XML to JSON conversion in pre-request scripts\n");
                report.append("- Using JSONPath instead of XPath where possible\n");
                report.append("- Implementing a custom XML parser script\n\n");
            }
            
            if (issues.stream().anyMatch(i -> i.getDescription().contains("script") || i.getDescription().contains("Groovy"))) {
                report.append("### Script Conversion Issues\n\n");
                report.append("Some Groovy scripts couldn't be automatically converted. Consider:\n");
                report.append("- Manually reviewing and updating complex scripts\n");
                report.append("- Implementing missing functionality in JavaScript\n");
                report.append("- Using simplifier alternatives where possible\n\n");
            }
        }
        
        return report.toString();
    }
    
    /**
     * Write the conversion report to a file.
     * 
     * @param outputDir The output directory
     * @return The report file
     * @throws IOException If an I/O error occurs
     */
    public File writeReportToFile(File outputDir) throws IOException {
        String reportContent = generateReport();
        String fileName = projectName.replaceAll("[^a-zA-Z0-9-_]", "_") + "_conversion_report.md";
        File reportFile = new File(outputDir, fileName);
        
        try (FileWriter writer = new FileWriter(reportFile)) {
            writer.write(reportContent);
        }
        
        logger.info("Conversion report written to: {}", reportFile.getAbsolutePath());
        return reportFile;
    }
    
    /**
     * Get a simplified list of issues for legacy compatibility.
     * 
     * @return List of issue descriptions
     */
    public List<String> getIssueDescriptions() {
        return issues.stream()
                .map(issue -> issue.getSeverity() + ": " + issue.getComponent() + " - " + issue.getDescription())
                .collect(Collectors.toList());
    }
    
    /**
     * Class representing a conversion issue.
     */
    public static class ConversionIssue {
        private String component;
        private String description;
        private Severity severity;
        private LocalDateTime timestamp;
        
        public ConversionIssue(String component, String description, Severity severity) {
            this.component = component;
            this.description = description;
            this.severity = severity;
            this.timestamp = LocalDateTime.now();
        }
        
        public String getComponent() {
            return component;
        }
        
        public String getDescription() {
            return description;
        }
        
        public Severity getSeverity() {
            return severity;
        }
        
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        
        @Override
        public String toString() {
            return severity + ": " + component + " - " + description;
        }
    }
    
    /**
     * Severity levels for conversion issues.
     */
    public enum Severity {
        INFO,
        WARNING,
        ERROR
    }
} 