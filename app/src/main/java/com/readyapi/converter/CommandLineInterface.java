package com.readyapi.converter;

import org.apache.commons.cli.*;
import java.io.File;

/**
 * Enhanced command-line interface for the ReadyAPI to Postman converter.
 * Provides robust command-line options and configuration.
 */
public class CommandLineInterface {
    
    private static final Options options = new Options();
    
    static {
        // Setup command line options
        Option input = Option.builder("i")
                .longOpt("input")
                .desc("Path to ReadyAPI project XML file (required)")
                .hasArg()
                .required(true)
                .argName("FILE")
                .build();
        
        Option output = Option.builder("o")
                .longOpt("output")
                .desc("Directory to save output files (default: ./output)")
                .hasArg()
                .required(false)
                .argName("DIR")
                .build();
        
        Option verbose = Option.builder("v")
                .longOpt("verbose")
                .desc("Enable verbose output")
                .hasArg(false)
                .required(false)
                .build();
        
        Option help = Option.builder("h")
                .longOpt("help")
                .desc("Show help message")
                .hasArg(false)
                .required(false)
                .build();
        
        Option skipValidation = Option.builder("s")
                .longOpt("skip-validation")
                .desc("Skip validation of the generated Postman collection")
                .hasArg(false)
                .required(false)
                .build();
        
        Option environment = Option.builder("e")
                .longOpt("environment")
                .desc("Environment to use for variable substitution (default: none)")
                .hasArg()
                .required(false)
                .argName("ENV")
                .build();
        
        Option format = Option.builder("f")
                .longOpt("format")
                .desc("Output format: json or yaml (default: json)")
                .hasArg()
                .required(false)
                .argName("FORMAT")
                .build();
        
        Option includeTests = Option.builder("t")
                .longOpt("include-tests")
                .desc("Include test scripts in the output (default: true)")
                .hasArg()
                .required(false)
                .argName("BOOL")
                .build();
        
        // Add options
        options.addOption(input);
        options.addOption(output);
        options.addOption(verbose);
        options.addOption(help);
        options.addOption(skipValidation);
        options.addOption(environment);
        options.addOption(format);
        options.addOption(includeTests);
    }
    
    /**
     * Parse command line arguments and run the converter.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;
        
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println("Error parsing command line arguments: " + e.getMessage());
            formatter.printHelp("convert2postman", options, true);
            System.exit(1);
        }
        
        // Show help if requested
        if (cmd.hasOption("help")) {
            formatter.printHelp("convert2postman", options, true);
            System.exit(0);
        }
        
        // Get input file
        String inputFile = cmd.getOptionValue("input");
        
        // Get output directory (default: ./output)
        String outputDir = cmd.getOptionValue("output", "./output");
        
        // Get other options
        boolean verbose = cmd.hasOption("verbose");
        boolean skipValidation = cmd.hasOption("skip-validation");
        String environment = cmd.getOptionValue("environment");
        String format = cmd.getOptionValue("format", "json");
        boolean includeTests = Boolean.parseBoolean(cmd.getOptionValue("include-tests", "true"));
        
        // Validate input file
        File inputFileObj = new File(inputFile);
        if (!inputFileObj.exists()) {
            System.err.println("Error: Input file does not exist: " + inputFile);
            System.exit(1);
        }
        
        // Create configuration
        ConverterConfig config = new ConverterConfig();
        config.setVerbose(verbose);
        config.setSkipValidation(skipValidation);
        config.setEnvironment(environment);
        config.setFormat(format);
        config.setIncludeTests(includeTests);
        
        // Run converter
        if (verbose) {
            System.out.println("Starting conversion with the following configuration:");
            System.out.println("  Input file: " + inputFile);
            System.out.println("  Output directory: " + outputDir);
            System.out.println("  Environment: " + (environment != null ? environment : "none"));
            System.out.println("  Format: " + format);
            System.out.println("  Include tests: " + includeTests);
            System.out.println("  Skip validation: " + skipValidation);
        }
        
        ReadyApiToPostmanConverter converter = new ReadyApiToPostmanConverter();
        converter.convert(inputFile, outputDir);
    }
    
    /**
     * Configuration class for the converter.
     */
    public static class ConverterConfig {
        private boolean verbose = false;
        private boolean skipValidation = false;
        private String environment = null;
        private String format = "json";
        private boolean includeTests = true;
        
        public boolean isVerbose() {
            return verbose;
        }
        
        public void setVerbose(boolean verbose) {
            this.verbose = verbose;
        }
        
        public boolean isSkipValidation() {
            return skipValidation;
        }
        
        public void setSkipValidation(boolean skipValidation) {
            this.skipValidation = skipValidation;
        }
        
        public String getEnvironment() {
            return environment;
        }
        
        public void setEnvironment(String environment) {
            this.environment = environment;
        }
        
        public String getFormat() {
            return format;
        }
        
        public void setFormat(String format) {
            this.format = format;
        }
        
        public boolean isIncludeTests() {
            return includeTests;
        }
        
        public void setIncludeTests(boolean includeTests) {
            this.includeTests = includeTests;
        }
    }
}
