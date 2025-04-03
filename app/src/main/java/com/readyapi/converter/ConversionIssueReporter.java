package com.readyapi.converter;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Enhanced issue reporter for conversion process.
 * Provides detailed reporting and categorization of issues.
 */
public class ConversionIssueReporter {
    private final String projectName;
    private final List<ConversionIssue> issues = new ArrayList<>();
    private final LocalDateTime reportTime = LocalDateTime.now();
    
    public ConversionIssueReporter(String projectName) {
        this.projectName = projectName;
    }
    
    /**
     * Add an error issue.
     * 
     * @param component The component where the issue occurred
     * @param description The issue description
     */
    public void addError(String component, String description) {
        issues.add(new ConversionIssue(component, description, Severity.ERROR));
    }
    
    /**
     * Add a warning issue.
     * 
     * @param component The component where the issue occurred
     * @param description The issue description
     */
    public void addWarning(String component, String description) {
        issues.add(new ConversionIssue(component, description, Severity.WARNING));
    }
    
    /**
     * Add an info issue.
     * 
     * @param component The component where the issue occurred
     * @param description The issue description
     */
    public void addInfo(String component, String description) {
        issues.add(new ConversionIssue(component, description, Severity.INFO));
    }
    
    /**
     * Get all issues.
     * 
     * @return List of all issues
     */
    public List<ConversionIssue> getIssues() {
        return issues;
    }
    
    /**
     * Get issues of a specific severity.
     * 
     * @param severity The severity level
     * @return List of issues with the specified severity
     */
    public List<ConversionIssue> getIssuesBySeverity(Severity severity) {
        List<ConversionIssue> result = new ArrayList<>();
        for (ConversionIssue issue : issues) {
            if (issue.getSeverity() == severity) {
                result.add(issue);
            }
        }
        return result;
    }
    
    /**
     * Get issues for a specific component.
     * 
     * @param component The component name
     * @return List of issues for the specified component
     */
    public List<ConversionIssue> getIssuesByComponent(String component) {
        List<ConversionIssue> result = new ArrayList<>();
        for (ConversionIssue issue : issues) {
            if (issue.getComponent().equals(component)) {
                result.add(issue);
            }
        }
        return result;
    }
    
    /**
     * Check if there are any error issues.
     * 
     * @return True if there are error issues
     */
    public boolean hasErrors() {
        for (ConversionIssue issue : issues) {
            if (issue.getSeverity() == Severity.ERROR) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Generate a detailed report of all issues.
     * 
     * @return The report as a string
     */
    public String generateReport() {
        StringBuilder report = new StringBuilder();
        
        // Add report header
        report.append("# Conversion Report for ").append(projectName).append("\n\n");
        report.append("Generated: ").append(reportTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\n\n");
        
        // Add summary
        report.append("## Summary\n\n");
        report.append("- Total issues: ").append(issues.size()).append("\n");
        report.append("- Errors: ").append(getIssuesBySeverity(Severity.ERROR).size()).append("\n");
        report.append("- Warnings: ").append(getIssuesBySeverity(Severity.WARNING).size()).append("\n");
        report.append("- Info: ").append(getIssuesBySeverity(Severity.INFO).size()).append("\n\n");
        
        // Group issues by component
        report.append("## Issues by Component\n\n");
        
        // Get unique components
        List<String> components = new ArrayList<>();
        for (ConversionIssue issue : issues) {
            if (!components.contains(issue.getComponent())) {
                components.add(issue.getComponent());
            }
        }
        
        // Report issues by component
        for (String component : components) {
            report.append("### ").append(component).append("\n\n");
            
            // Group by severity within component
            for (Severity severity : Severity.values()) {
                List<ConversionIssue> componentIssues = new ArrayList<>();
                for (ConversionIssue issue : issues) {
                    if (issue.getComponent().equals(component) && issue.getSeverity() == severity) {
                        componentIssues.add(issue);
                    }
                }
                
                if (!componentIssues.isEmpty()) {
                    report.append("#### ").append(severity).append("\n\n");
                    for (ConversionIssue issue : componentIssues) {
                        report.append("- ").append(issue.getDescription()).append("\n");
                    }
                    report.append("\n");
                }
            }
        }
        
        // Add recommendations
        report.append("## Recommendations\n\n");
        
        if (hasErrors()) {
            report.append("### Critical Issues\n\n");
            report.append("The following critical issues should be addressed before using the converted collection:\n\n");
            
            for (ConversionIssue issue : getIssuesBySeverity(Severity.ERROR)) {
                report.append("- **").append(issue.getComponent()).append("**: ");
                report.append(issue.getDescription()).append("\n");
            }
            report.append("\n");
        }
        
        report.append("### General Recommendations\n\n");
        report.append("1. Review all warnings and consider addressing them for better compatibility.\n");
        report.append("2. Test the converted collection thoroughly before using in production.\n");
        report.append("3. Pay special attention to authentication and data handling.\n");
        
        return report.toString();
    }
    
    /**
     * Save issues to a file.
     * 
     * @param issues The list of issues
     * @param filePath The file path
     */
    public static void saveIssues(List<String> issues, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("# Conversion Issues\n\n");
            
            for (String issue : issues) {
                writer.write("- " + issue + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error saving issues: " + e.getMessage());
        }
    }
    
    /**
     * Save the detailed report to a file.
     * 
     * @param outputDir The output directory
     * @return The path to the saved report
     */
    public String saveReport(File outputDir) {
        String reportFileName = projectName + "_conversion_report.md";
        File reportFile = new File(outputDir, reportFileName);
        
        try (FileWriter writer = new FileWriter(reportFile)) {
            writer.write(generateReport());
            return reportFile.getPath();
        } catch (IOException e) {
            System.err.println("Error saving report: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Represents a conversion issue.
     */
    public static class ConversionIssue {
        private final String component;
        private final String description;
        private final Severity severity;
        
        public ConversionIssue(String component, String description, Severity severity) {
            this.component = component;
            this.description = description;
            this.severity = severity;
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
        
        @Override
        public String toString() {
            return severity + " in " + component + ": " + description;
        }
    }
    
    /**
     * Enum for issue severity levels.
     */
    public enum Severity {
        ERROR,
        WARNING,
        INFO
    }
}
