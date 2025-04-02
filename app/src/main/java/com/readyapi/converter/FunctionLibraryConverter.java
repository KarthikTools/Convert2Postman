package com.readyapi.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts ReadyAPI Groovy script libraries to Postman JavaScript equivalents.
 * Uses dynamic analysis of the input script to generate appropriate JavaScript conversions.
 */
public class FunctionLibraryConverter {
    private static final Logger logger = LoggerFactory.getLogger(FunctionLibraryConverter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // Common Groovy to JavaScript mapping patterns
    private static final Map<Pattern, String> SYNTAX_CONVERSIONS = new HashMap<>();
    
    static {
        // Initialize common conversion patterns
        // Groovy def keyword to JavaScript let/const
        SYNTAX_CONVERSIONS.put(
            Pattern.compile("def\\s+(\\w+)\\s*=\\s*(.+)"), 
            "let $1 = $2"
        );
        
        // Groovy string interpolation "${var}" to JavaScript `${var}`
        SYNTAX_CONVERSIONS.put(
            Pattern.compile("\"(.*?)\\$\\{(.*?)\\}(.*?)\""), 
            "`$1${$2}$3`"
        );
        
        // Groovy list declaration to JavaScript array
        SYNTAX_CONVERSIONS.put(
            Pattern.compile("\\[([^\\]]*?)\\]\\s+as\\s+List"), 
            "[$1]"
        );
        
        // Groovy map declaration to JavaScript object
        SYNTAX_CONVERSIONS.put(
            Pattern.compile("\\[([^\\]]*?)\\]\\s+as\\s+Map"), 
            "{$1}"
        );
    }
    
    /**
     * Converts a Groovy script to JavaScript, analyzing the script contents
     * to generate appropriate JavaScript equivalents.
     *
     * @param groovyScript The Groovy script to convert
     * @return The converted JavaScript code
     */
    public static String convertGroovyToJavaScript(String groovyScript) {
        try {
            // Basic analysis: Split the script into method declarations and class definitions
            List<MethodInfo> methods = extractMethods(groovyScript);
            List<ClassInfo> classes = extractClasses(groovyScript);
            
            StringBuilder jsScript = new StringBuilder();
            
            // Add documentation header
            jsScript.append("// Function Library converted from ReadyAPI Groovy script\n");
            jsScript.append("// This is an automatically generated JavaScript equivalent\n\n");
            
            // Create class for each detected class
            for (ClassInfo classInfo : classes) {
                convertClassToJavaScript(classInfo, jsScript);
            }
            
            // If no classes found but methods exist, create a default utility class
            if (classes.isEmpty() && !methods.isEmpty()) {
                jsScript.append("class FunctionLibrary {\n");
                jsScript.append("    constructor(log, context, testRunner) {\n");
                jsScript.append("        this.log = log;\n");
                jsScript.append("        this.context = context;\n");
                jsScript.append("        this.testRunner = testRunner;\n");
                jsScript.append("    }\n\n");
                
                // Add methods
                for (MethodInfo method : methods) {
                    convertMethodToJavaScript(method, jsScript, 4);
                }
                
                jsScript.append("}\n\n");
                
                // Add initialization code for default class
                jsScript.append("// Initialize the function library\n");
                jsScript.append("const functionLibrary = new FunctionLibrary(\n");
                jsScript.append("    console, // log\n");
                jsScript.append("    pm.variables, // context\n");
                jsScript.append("    pm.testRunner // testRunner\n");
                jsScript.append(");\n\n");
                
                // Export the library
                jsScript.append("// Export the library for use in other scripts\n");
                jsScript.append("pm.functionLibrary = functionLibrary;\n");
            }
            
            return jsScript.toString();
        } catch (Exception e) {
            logger.error("Failed to convert Groovy script to JavaScript", e);
            throw new RuntimeException("Failed to convert Groovy script to JavaScript: " + e.getMessage(), e);
        }
    }
    
    /**
     * Extracts method information from a Groovy script.
     *
     * @param script The Groovy script
     * @return List of MethodInfo objects
     */
    private static List<MethodInfo> extractMethods(String script) {
        List<MethodInfo> methods = new ArrayList<>();
        
        // Pattern to match method declarations: def methodName(params) { ... }
        Pattern methodPattern = Pattern.compile(
            "(?:public|private|protected)?\\s*(?:static)?\\s*(?:def|void|String|int|boolean|Object|Map|List|\\w+)?\\s+(\\w+)\\s*\\(([^)]*)\\)\\s*\\{([^}]*(?:\\{[^}]*\\}[^}]*)*)\\}",
            Pattern.DOTALL
        );
        
        Matcher matcher = methodPattern.matcher(script);
        while (matcher.find()) {
            String name = matcher.group(1);
            String params = matcher.group(2);
            String body = matcher.group(3);
            
            // Skip if it looks like it's part of a class definition
            // This is a simplification - proper parsing would use an AST
            if (script.substring(0, matcher.start()).contains("class") && 
                script.substring(0, matcher.start()).lastIndexOf("class") > 
                script.substring(0, matcher.start()).lastIndexOf("}")) {
                continue;
            }
            
            methods.add(new MethodInfo(name, params, body));
        }
        
        return methods;
    }
    
    /**
     * Extracts class information from a Groovy script.
     *
     * @param script The Groovy script
     * @return List of ClassInfo objects
     */
    private static List<ClassInfo> extractClasses(String script) {
        List<ClassInfo> classes = new ArrayList<>();
        
        // Pattern to match class declarations: class ClassName { ... }
        Pattern classPattern = Pattern.compile(
            "class\\s+(\\w+)(?:\\s+extends\\s+(\\w+))?(?:\\s+implements\\s+([\\w,\\s]+))?\\s*\\{([^}]*(?:\\{[^}]*\\}[^}]*)*)\\}",
            Pattern.DOTALL
        );
        
        Matcher matcher = classPattern.matcher(script);
        while (matcher.find()) {
            String name = matcher.group(1);
            String parent = matcher.group(2); // May be null
            String implementsStr = matcher.group(3); // May be null
            String body = matcher.group(4);
            
            ClassInfo classInfo = new ClassInfo(name, parent, implementsStr);
            
            // Extract methods from class body
            List<MethodInfo> classMethods = extractMethods(body);
            classInfo.setMethods(classMethods);
            
            // Extract fields
            Pattern fieldPattern = Pattern.compile(
                "(?:private|protected|public)?\\s*(?:final)?\\s*(?:static)?\\s*(?:def|String|int|boolean|Object|Map|List|\\w+)\\s+(\\w+)(?:\\s*=\\s*([^;\\r\\n]+))?");
            
            Matcher fieldMatcher = fieldPattern.matcher(body);
            while (fieldMatcher.find()) {
                String fieldName = fieldMatcher.group(1);
                String fieldValue = fieldMatcher.group(2); // May be null
                classInfo.addField(fieldName, fieldValue);
            }
            
            classes.add(classInfo);
        }
        
        return classes;
    }
    
    /**
     * Converts a Groovy method to JavaScript.
     *
     * @param method The method info
     * @param jsScript The StringBuilder to append to
     * @param indentLevel The indentation level
     */
    private static void convertMethodToJavaScript(MethodInfo method, StringBuilder jsScript, int indentLevel) {
        String indent = StringUtils.repeat(" ", indentLevel);
        String bodyIndent = StringUtils.repeat(" ", indentLevel + 4);
        
        // Convert parameters
        String params = method.getParams();
        
        // Check if the method needs to be async (simplified check)
        boolean isAsync = method.getBody().contains("wait") || 
                         method.getBody().contains("sleep") || 
                         method.getBody().contains("run(");
        
        // Method declaration
        jsScript.append(indent).append(isAsync ? "async " : "").append(method.getName())
               .append("(").append(params).append(") {\n");
        
        // Convert body with common syntax conversions
        String body = method.getBody();
        for (Map.Entry<Pattern, String> conversion : SYNTAX_CONVERSIONS.entrySet()) {
            body = conversion.getKey().matcher(body).replaceAll(conversion.getValue());
        }
        
        // Additional specific conversions - these would need to be more sophisticated in a real implementation
        
        // Convert Groovy-specific constructs to JavaScript
        body = body.replace(".each {", ".forEach(");
        body = body.replace("it ->", "item =>");
        body = body.replace(" -> ", " => ");
        body = body.replace(".collect {", ".map(");
        body = body.replace(".find {", ".find(");
        body = body.replace(".any {", ".some(");
        body = body.replace(".all {", ".every(");
        body = body.replace(".toString()", ".toString()");
        body = body.replace("println", "console.log");
        
        // Add try/catch for potential errors
        jsScript.append(bodyIndent).append("try {\n");
        
        // Add converted body with proper indentation
        for (String line : body.split("\n")) {
            jsScript.append(bodyIndent).append("    ").append(line).append("\n");
        }
        
        // Add catch block
        jsScript.append(bodyIndent).append("} catch (error) {\n");
        jsScript.append(bodyIndent).append("    console.error(`Error in ").append(method.getName())
               .append(": ${error.message}`);\n");
        jsScript.append(bodyIndent).append("    throw error;\n");
        jsScript.append(bodyIndent).append("}\n");
        
        jsScript.append(indent).append("}\n\n");
    }
    
    /**
     * Converts a Groovy class to JavaScript.
     *
     * @param classInfo The class info
     * @param jsScript The StringBuilder to append to
     */
    private static void convertClassToJavaScript(ClassInfo classInfo, StringBuilder jsScript) {
        // Class declaration
        jsScript.append("class ").append(classInfo.getName());
        
        // Add parent class if exists
        if (classInfo.getParent() != null) {
            jsScript.append(" extends ").append(classInfo.getParent());
        }
        
        jsScript.append(" {\n");
        
        // Constructor
        jsScript.append("    constructor(log, context, testRunner) {\n");
        if (classInfo.getParent() != null) {
            jsScript.append("        super(log, context, testRunner);\n");
        }
        jsScript.append("        this.log = log;\n");
        jsScript.append("        this.context = context;\n");
        jsScript.append("        this.testRunner = testRunner;\n");
        
        // Initialize fields with default values
        for (Map.Entry<String, String> field : classInfo.getFields().entrySet()) {
            String value = field.getValue() != null ? field.getValue() : "null";
            jsScript.append("        this.").append(field.getKey()).append(" = ").append(value).append(";\n");
        }
        
        jsScript.append("    }\n\n");
        
        // Add methods
        for (MethodInfo method : classInfo.getMethods()) {
            convertMethodToJavaScript(method, jsScript, 4);
        }
        
        jsScript.append("}\n\n");
        
        // Add initialization code
        jsScript.append("// Initialize the ").append(classInfo.getName()).append("\n");
        jsScript.append("const ").append(StringUtils.uncapitalize(classInfo.getName())).append(" = new ")
               .append(classInfo.getName()).append("(\n");
        jsScript.append("    console, // log\n");
        jsScript.append("    pm.variables, // context\n");
        jsScript.append("    pm.testRunner // testRunner\n");
        jsScript.append(");\n\n");
        
        // Export the class instance
        jsScript.append("// Export the library for use in other scripts\n");
        jsScript.append("pm.").append(StringUtils.uncapitalize(classInfo.getName())).append(" = ")
               .append(StringUtils.uncapitalize(classInfo.getName())).append(";\n\n");
    }
    
    /**
     * Converts a Groovy script library to a Postman variable.
     *
     * @param libraryName The name of the library
     * @param groovyScript The Groovy script
     * @return JSON string representation of the Postman variable
     */
    public static String convertLibraryToPostmanVariable(String libraryName, String groovyScript) {
        try {
            String jsScript = convertGroovyToJavaScript(groovyScript);
            
            Map<String, Object> libraryVar = new HashMap<>();
            libraryVar.put("key", libraryName);
            libraryVar.put("value", jsScript);
            libraryVar.put("type", "string");
            libraryVar.put("enabled", true);
            
            return objectMapper.writeValueAsString(libraryVar);
        } catch (Exception e) {
            logger.error("Failed to convert library to Postman variable", e);
            throw new RuntimeException("Failed to convert library to Postman variable: " + e.getMessage(), e);
        }
    }
    
    /**
     * Helper class to store method information.
     */
    private static class MethodInfo {
        private final String name;
        private final String params;
        private final String body;
        
        public MethodInfo(String name, String params, String body) {
            this.name = name;
            this.params = params;
            this.body = body;
        }
        
        public String getName() {
            return name;
        }
        
        public String getParams() {
            return params;
        }
        
        public String getBody() {
            return body;
        }
    }
    
    /**
     * Helper class to store class information.
     */
    private static class ClassInfo {
        private final String name;
        private final String parent;
        private final String implementsStr;
        private List<MethodInfo> methods = new ArrayList<>();
        private final Map<String, String> fields = new HashMap<>();
        
        public ClassInfo(String name, String parent, String implementsStr) {
            this.name = name;
            this.parent = parent;
            this.implementsStr = implementsStr;
        }
        
        public String getName() {
            return name;
        }
        
        public String getParent() {
            return parent;
        }
        
        public String getImplementsStr() {
            return implementsStr;
        }
        
        public List<MethodInfo> getMethods() {
            return methods;
        }
        
        public void setMethods(List<MethodInfo> methods) {
            this.methods = methods;
        }
        
        public Map<String, String> getFields() {
            return fields;
        }
        
        public void addField(String name, String value) {
            fields.put(name, value);
        }
    }
}