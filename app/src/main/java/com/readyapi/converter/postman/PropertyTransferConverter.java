package com.readyapi.converter.postman;

import java.util.ArrayList;
import java.util.List;
import org.dom4j.Element;

/**
 * Converts ReadyAPI property transfers to Postman pre-request scripts.
 */
public class PropertyTransferConverter {
    
    /**
     * Convert property transfers from ReadyAPI to Postman pre-request scripts.
     * 
     * @param transfers The transfers element from ReadyAPI XML
     * @return List of pre-request script lines
     */
    public static List<String> convertToPreRequestScript(Element transfers) {
        List<String> scripts = new ArrayList<>();
        
        if (transfers == null) {
            return scripts;
        }
        
        for (Element transfer : transfers.elements("transfer")) {
            String name = transfer.elementText("name");
            String sourcePath = transfer.elementText("sourcePath");
            
            if (name != null && sourcePath != null) {
                scripts.add(String.format("pm.variables.set('%s', pm.response.json()%s);", name, sourcePath));
            }
        }
        
        return scripts;
    }
    
    /**
     * Convert a single property transfer to a pre-request script line.
     * 
     * @param name The name of the property
     * @param sourcePath The JSON path to extract the value from
     * @return The pre-request script line
     */
    public static String convertTransfer(String name, String sourcePath) {
        return String.format("pm.variables.set('%s', pm.response.json()%s);", name, sourcePath);
    }
} 