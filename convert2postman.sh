#!/bin/bash

# Build and Run Script for Convert2Postman
# This script builds the Convert2Postman tool and provides a simple interface to run it

# Set colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

# Set default values
PROJECT_DIR="$(pwd)"
INPUT_FILE=""
OUTPUT_DIR="./output"
VERBOSE=false
SKIP_VALIDATION=false
ENVIRONMENT=""
FORMAT="json"
INCLUDE_TESTS=true

# Function to display usage information
function show_usage {
    echo -e "${YELLOW}Convert2Postman - ReadyAPI to Postman Converter${NC}"
    echo ""
    echo "Usage: $0 [options]"
    echo ""
    echo "Options:"
    echo "  -i, --input FILE       Path to ReadyAPI project XML file (required)"
    echo "  -o, --output DIR       Directory to save output files (default: ./output)"
    echo "  -v, --verbose          Enable verbose output"
    echo "  -s, --skip-validation  Skip validation of the generated Postman collection"
    echo "  -e, --environment ENV  Environment to use for variable substitution"
    echo "  -f, --format FORMAT    Output format: json or yaml (default: json)"
    echo "  -t, --include-tests    Include test scripts in the output (default: true)"
    echo "  -b, --build-only       Only build the project, don't run the converter"
    echo "  -h, --help             Show this help message"
    echo ""
    echo "Example:"
    echo "  $0 -i ./ready_api_project.xml -o ./postman_output -v"
    echo ""
}

# Function to build the project
function build_project {
    echo -e "${YELLOW}Building Convert2Postman...${NC}"
    
    # Navigate to project directory
    cd "$PROJECT_DIR"
    
    # Check if gradlew exists and is executable
    if [ ! -x "./gradlew" ]; then
        echo -e "${YELLOW}Making gradlew executable...${NC}"
        chmod +x ./gradlew
    fi
    
    # Build the project
    ./gradlew clean build
    
    # Check if build was successful
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}Build successful!${NC}"
        return 0
    else
        echo -e "${RED}Build failed!${NC}"
        return 1
    fi
}

# Function to run the converter
function run_converter {
    echo -e "${YELLOW}Running Convert2Postman...${NC}"
    
    # Build command
    CMD="java -cp app/build/libs/app-all.jar com.readyapi.converter.ReadyApiToPostmanConverter"
    
    # Add options
    CMD="$CMD \"$INPUT_FILE\" \"$OUTPUT_DIR\""
    
    # Run the command
    if [ "$VERBOSE" = true ]; then
        echo -e "${YELLOW}Executing: $CMD${NC}"
    fi
    
    eval $CMD
    
    # Check if conversion was successful
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}Conversion successful!${NC}"
        echo -e "${GREEN}Output files are in: $OUTPUT_DIR${NC}"
        return 0
    else
        echo -e "${RED}Conversion failed!${NC}"
        return 1
    fi
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    key="$1"
    
    case $key in
        -i|--input)
            INPUT_FILE="$2"
            shift
            shift
            ;;
        -o|--output)
            OUTPUT_DIR="$2"
            shift
            shift
            ;;
        -v|--verbose)
            VERBOSE=true
            shift
            ;;
        -s|--skip-validation)
            SKIP_VALIDATION=true
            shift
            ;;
        -e|--environment)
            ENVIRONMENT="$2"
            shift
            shift
            ;;
        -f|--format)
            FORMAT="$2"
            shift
            shift
            ;;
        -t|--include-tests)
            INCLUDE_TESTS="$2"
            shift
            shift
            ;;
        -b|--build-only)
            BUILD_ONLY=true
            shift
            ;;
        -h|--help)
            show_usage
            exit 0
            ;;
        *)
            echo -e "${RED}Unknown option: $key${NC}"
            show_usage
            exit 1
            ;;
    esac
done

# Check if input file is provided
if [ -z "$INPUT_FILE" ] && [ "$BUILD_ONLY" != true ]; then
    echo -e "${RED}Error: Input file is required${NC}"
    show_usage
    exit 1
fi

# Build the project
build_project
if [ $? -ne 0 ]; then
    exit 1
fi

# Run the converter if not build-only
if [ "$BUILD_ONLY" != true ]; then
    run_converter
    if [ $? -ne 0 ]; then
        exit 1
    fi
fi

exit 0
