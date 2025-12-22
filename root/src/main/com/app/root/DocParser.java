package main.com.app.root;
import main.com.app.root.screen_controller.ScreenData;
import main.com.app.root.screen_controller.ScreenElement;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DocParser {
    /**
     * Parse Screen
     */
    public static ScreenData parseScreen(String filePath, int screenWidth, int screenHeight) {
        ScreenData screenData = new ScreenData(filePath);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(filePath));

            Element root = document.getDocumentElement();
            screenData.screenType = root.getTagName();

            parseAttr(root, screenData.screenAttr);
            parseEl(root, screenData.elements, screenWidth, screenHeight);
        } catch(Exception err) {
            System.err.println("Error parsing screen XML: " + err.getMessage());
            err.printStackTrace();
        }

        return screenData;
    }

    private static void parseEl(
        Element parent, 
        List<ScreenElement> elements,
        int screenWidth,
        int screenHeight
    ) {
        NodeList children = parent.getChildNodes();
        
        for(int i = 0; i < children.getLength(); i++) {
            if(!(children.item(i) instanceof Element)) continue;
            
            Element element = (Element) children.item(i);
            String tagName = element.getTagName();
            
            ScreenElement screenElement = createScreenElement(
                element, 
                tagName,
                screenWidth,
                screenHeight
            );
            if(screenElement != null) {
                elements.add(screenElement);
            }
            
            parseEl(
                element, 
                elements,
                screenWidth,
                screenHeight
            );
        }
    }
    
    private static ScreenElement createScreenElement(
        Element element, 
        String type, 
        int screenWidth, 
        int screenHeight
    ) {
        String text = element.getTextContent().trim();
        String id = element.hasAttribute("id") ? element.getAttribute("id") : "";
        
        int x = parseCoordinate(element, "x", screenWidth, 1280);
        int y = parseCoordinate(element, "y", screenHeight, 720);
        
        float scale = element.hasAttribute("scale") ? 
            Float.parseFloat(element.getAttribute("scale")) : 1.0f;
        
        float[] color = new float[]{ 1.0f, 1.0f, 1.0f };
        if(element.hasAttribute("color")) {
            String colorStr = element.getAttribute("color");
            String[] colorParts = colorStr.split(",");
            if(colorParts.length >= 3) {
                color = new float[] {
                    Float.parseFloat(colorParts[0]),
                    Float.parseFloat(colorParts[1]),
                    Float.parseFloat(colorParts[2])
                };
            }
        }
        
        String action = element
            .hasAttribute("action") ? 
            element.getAttribute("action") : 
            "";
        
        ScreenElement screenElement = new ScreenElement(
            type, 
            id, 
            text, 
            x, y, 
            scale, 
            color, 
            action
        );
        parseAttr(element, screenElement.attr);
        return screenElement;
    }

    /**
     * Parse coordinates
     */
    private static int parseCoordinate(Element element, String attrName, int currentScreenSize, int originalScreenSize) {
        if(!element.hasAttribute(attrName)) return 0;
        
        String coordStr = element.getAttribute(attrName);
        if(coordStr.endsWith("%")) {
            float percentage = Float.parseFloat(coordStr.replace("%", "")) / 100.0f;
            return (int)(currentScreenSize * percentage);
        } else {
            int originalCoord = Integer.parseInt(coordStr);
            float scaleFactor = (float)currentScreenSize / originalScreenSize;
            return (int)(originalCoord * scaleFactor);
        }
    }
    
    private static void parseAttr(Element element, Map<String, String> attributes) {
        var attributeMap = element.getAttributes();
        for(int i = 0; i < attributeMap.getLength(); i++) {
            var attr = attributeMap.item(i);
            attributes.put(attr.getNodeName(), attr.getNodeValue());
        }
    }
    
    /**
     * Get Elements By Type
     */
    public static List<ScreenElement> getElementsByType(ScreenData screenData, String type) {
        List<ScreenElement> result = new ArrayList<>();
        for(ScreenElement element : screenData.elements) {
            if(element.type.equals(type)) {
                result.add(element);
            }
        }
        return result;
    }
    
    /**
     * Get Element By Id
     */
    public static ScreenElement getElementById(ScreenData screenData, String id) {
        for(ScreenElement element : screenData.elements) {
            if(element.id.equals(id)) {
                return element;
            }
        }
        return null;
    }
    
    /**
     * Parse Buttons
     */
    public static List<ScreenElement> parseButtons(
        String xmlFilePath,
        int screenWidth,
        int screenHeight
    ) {
        ScreenData screenData = parseScreen(
            xmlFilePath,
            screenWidth,
            screenHeight
        );
        return getElementsByType(screenData, "button");
    }
    
    /**
     * Parse Labels
     */
    public static List<ScreenElement> parseLabels(
        String xmlFilePath,
        int screenWidth,
        int screenHeight
    ) {
        ScreenData screenData = parseScreen(
            xmlFilePath,
            screenWidth,
            screenHeight
        );
        return getElementsByType(screenData, "label");
    }
}
