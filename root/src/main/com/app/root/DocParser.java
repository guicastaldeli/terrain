package main.com.app.root;

import main.com.app.root.screen_controller.ScreenData;
import main.com.app.root.screen_controller.ScreenElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.xml.sax.InputSource;

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
        
        String placeholder = element.hasAttribute("placeholder") ? 
            element.getAttribute("placeholder") : "";
        int maxLength = element.hasAttribute("maxLength") ? 
            Integer.parseInt(element.getAttribute("maxLength")) : 50;
        boolean enabled = !element.hasAttribute("enabled") || 
            Boolean.parseBoolean(element.getAttribute("enabled"));
        
        ScreenElement screenElement = new ScreenElement(
            type, 
            id, 
            text, 
            x, y, 
            scale, 
            color, 
            action
        );
        
        if("input".equals(type)) {
            screenElement.attr.put("placeholder", placeholder);
            screenElement.attr.put("maxLength", String.valueOf(maxLength));
            screenElement.attr.put("enabled", String.valueOf(enabled));
            screenElement.attr.put("isInput", "true");
        }
        
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
     * Get Input Elements
     */
    public static List<ScreenElement> getInputElements(ScreenData screenData) {
        List<ScreenElement> result = new ArrayList<>();
        for(ScreenElement element : screenData.elements) {
            if("input".equals(element.type) || "true".equals(element.attr.get("isInput"))) {
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
    
    public static class Selector {
        private Document document;
        private XPath xpath;
        
        public Selector(String filePath) throws Exception {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            this.document = builder.parse(new File(filePath));
            this.xpath = XPathFactory.newInstance().newXPath();
        }
        
        public Selector fromString(String xml) throws Exception {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            this.document = builder.parse(new InputSource(new StringReader(xml)));
            this.xpath = XPathFactory.newInstance().newXPath();
            return this;
        }
        
        public List<Element> select(String xpathExpression) throws Exception {
            List<Element> result = new ArrayList<>();
            XPathExpression expr = xpath.compile(xpathExpression);
            NodeList nodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            
            for(int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if(node.getNodeType() == Node.ELEMENT_NODE) {
                    result.add((Element) node);
                }
            }
            
            return result;
        }
        
        public Element selectFirst(String xpathExpression) throws Exception {
            XPathExpression expr = xpath.compile(xpathExpression);
            Node node = (Node) expr.evaluate(document, XPathConstants.NODE);
            return (node != null && node.getNodeType() == Node.ELEMENT_NODE) ? 
                   (Element) node : null;
        }
        
        public List<Element> selectByTag(String tagName) throws Exception {
            return select("//" + tagName);
        }
        
        public List<Element> selectByAttribute(String attributeName, String attributeValue) throws Exception {
            return select(String.format("//*[@%s='%s']", attributeName, attributeValue));
        }
        
        public List<Element> selectById(String id) throws Exception {
            return select("//*[@id='" + id + "']");
        }
        
        public List<Element> selectByClass(String className) throws Exception {
            return select("//*[contains(@class, '" + className + "')]");
        }
        
        public List<Element> selectChildren(String parentXPath) throws Exception {
            return select(parentXPath + "/*");
        }
        
        public List<Element> selectDescendants(String ancestorXPath) throws Exception {
            return select(ancestorXPath + "//*");
        }
        
        public List<Element> selectSiblings(String elementXPath) throws Exception {
            return select(elementXPath + "/following-sibling::* | " + elementXPath + "/preceding-sibling::*");
        }
        
        public Element selectParent(String elementXPath) throws Exception {
            return selectFirst(elementXPath + "/..");
        }
        
        public List<Element> selectByPosition(String xpathExpression, String position) throws Exception {
            switch(position.toLowerCase()) {
                case "first":
                    return select("(" + xpathExpression + ")[1]");
                case "last":
                    return select("(" + xpathExpression + ")[last()]");
                default:
                    if(position.matches("\\d+")) {
                        return select("(" + xpathExpression + ")[" + position + "]");
                    }
                    return select(xpathExpression);
            }
        }
        
        public List<String> getTexts(String xpathExpression) throws Exception {
            List<String> texts = new ArrayList<>();
            List<Element> elements = select(xpathExpression);
            for(Element element : elements) {
                texts.add(element.getTextContent().trim());
            }
            return texts;
        }
        
        public String getText(String xpathExpression) throws Exception {
            List<String> texts = getTexts(xpathExpression);
            return texts.isEmpty() ? "" : texts.get(0);
        }
        
        public List<String> getAttributeValues(String xpathExpression, String attributeName) throws Exception {
            List<String> values = new ArrayList<>();
            List<Element> elements = select(xpathExpression);
            for(Element element : elements) {
                if(element.hasAttribute(attributeName)) {
                    values.add(element.getAttribute(attributeName));
                }
            }
            return values;
        }
        
        public String getAttributeValue(String xpathExpression, String attributeName) throws Exception {
            Element element = selectFirst(xpathExpression);
            return (element != null && element.hasAttribute(attributeName)) ? 
                   element.getAttribute(attributeName) : "";
        }
        
        public Map<String, String> getAttributes(String xpathExpression) throws Exception {
            Map<String, String> attributes = new HashMap<>();
            Element element = selectFirst(xpathExpression);
            if(element != null) {
                var attributeMap = element.getAttributes();
                for(int i = 0; i < attributeMap.getLength(); i++) {
                    var attr = attributeMap.item(i);
                    attributes.put(attr.getNodeName(), attr.getNodeValue());
                }
            }
            return attributes;
        }
        
        public int count(String xpathExpression) throws Exception {
            XPathExpression expr = xpath.compile("count(" + xpathExpression + ")");
            Double count = (Double) expr.evaluate(document, XPathConstants.NUMBER);
            return count.intValue();
        }
        
        public boolean exists(String xpathExpression) throws Exception {
            return selectFirst(xpathExpression) != null;
        }
        
        public Document getDocument() {
            return document;
        }
    }
    
    public static class RangeParser {
        private Document document;
        
        public RangeParser(String filePath) throws Exception {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            this.document = builder.parse(new File(filePath));
        }
        
        public RangeParser fromString(String xml) throws Exception {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            this.document = builder.parse(new InputSource(new StringReader(xml)));
            return this;
        }
        
        public List<Element> extractElementsInRange(String startXPath, String endXPath) throws Exception {
            List<Element> result = new ArrayList<>();
            Selector selector = new Selector("");
            selector.document = this.document;
            selector.xpath = XPathFactory.newInstance().newXPath();
            
            Element startElement = selector.selectFirst(startXPath);
            Element endElement = selector.selectFirst(endXPath);
            
            if(startElement == null || endElement == null) {
                return result;
            }
            
            NodeList allElements = document.getElementsByTagName("*");
            boolean inRange = false;
            
            for(int i = 0; i < allElements.getLength(); i++) {
                Element current = (Element) allElements.item(i);
                
                if(current.isSameNode(startElement)) {
                    inRange = true;
                }
                
                if(inRange) {
                    result.add(current);
                }
                
                if(current.isSameNode(endElement)) {
                    break;
                }
            }
            
            return result;
        }
        
        public String extractRangeAsXml(String startXPath, String endXPath) throws Exception {
            List<Element> elements = extractElementsInRange(startXPath, endXPath);
            StringBuilder xmlBuilder = new StringBuilder();
            
            for(Element element : elements) {
                xmlBuilder.append(elementToString(element)).append("\n");
            }
            
            return xmlBuilder.toString();
        }
        
        public List<String> extractContentBetweenTags(String startTag, String endTag) {
            List<String> results = new ArrayList<>();
            try {
                NodeList allNodes = document.getElementsByTagName("*");
                
                for(int i = 0; i < allNodes.getLength(); i++) {
                    Element element = (Element) allNodes.item(i);
                    String content = element.getTextContent();
                    
                    int startIdx = content.indexOf(startTag);
                    int endIdx = content.indexOf(endTag);
                    
                    if(startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
                        results.add(content.substring(startIdx + startTag.length(), endIdx).trim());
                    }
                }
            } catch(Exception err) {
                err.printStackTrace();
            }
            return results;
        }
        
        public List<String> extractAllPatterns(String startMarker, String endMarker) {
            List<String> patterns = new ArrayList<>();
            String xmlContent = documentToString(document);
            
            int currentIndex = 0;
            while(true) {
                int startIndex = xmlContent.indexOf(startMarker, currentIndex);
                if(startIndex == -1) break;
                
                int endIndex = xmlContent.indexOf(endMarker, startIndex + startMarker.length());
                if(endIndex == -1) break;
                
                String pattern = xmlContent.substring(startIndex + startMarker.length(), endIndex);
                patterns.add(pattern.trim());
                currentIndex = endIndex + endMarker.length();
            }
            
            return patterns;
        }
        
        public List<Element> extractElementsAtDepth(int depth) throws Exception {
            List<Element> result = new ArrayList<>();
            extractElementsAtDepthRecursive(document.getDocumentElement(), 0, depth, result);
            return result;
        }
        
        private void extractElementsAtDepthRecursive(
            Element element,
            int currentDepth, 
            int targetDepth, 
            List<Element> result
        ) {
            if(currentDepth == targetDepth) {
                result.add(element);
                return;
            }
            
            NodeList children = element.getChildNodes();
            for(int i = 0; i < children.getLength(); i++) {
                if(children.item(i) instanceof Element) {
                    extractElementsAtDepthRecursive(
                        (Element) children.item(i), 
                        currentDepth + 1, 
                        targetDepth, 
                        result
                    );
                }
            }
        }
        
        public List<Element> extractElementsInDepthRange(int minDepth, int maxDepth) throws Exception {
            List<Element> result = new ArrayList<>();
            extractElementsInDepthRangeRecursive(document.getDocumentElement(), 0, minDepth, maxDepth, result);
            return result;
        }
        
        private void extractElementsInDepthRangeRecursive(
            Element element, 
            int currentDepth, 
            int minDepth, int maxDepth, 
            List<Element> result
        ) {
            if(currentDepth >= minDepth && currentDepth <= maxDepth) {
                result.add(element);
            }
            
            if(currentDepth < maxDepth) {
                NodeList children = element.getChildNodes();
                for(int i = 0; i < children.getLength(); i++) {
                    if(children.item(i) instanceof Element) {
                        extractElementsInDepthRangeRecursive((Element) children.item(i), 
                                                            currentDepth + 1, 
                                                            minDepth, 
                                                            maxDepth, 
                                                            result);
                    }
                }
            }
        }
        
        public List<List<Element>> extractRepeatingPatterns(String patternXPath) throws Exception {
            List<List<Element>> patterns = new ArrayList<>();
            Selector selector = new Selector("");
            selector.document = this.document;
            selector.xpath = XPathFactory.newInstance().newXPath();
            
            List<Element> patternElements = selector.select(patternXPath);
            Map<String, List<Element>> patternMap = new HashMap<>();
            
            for(Element element : patternElements) {
                String patternKey = getElementPatternKey(element);
                patternMap.computeIfAbsent(patternKey, k -> new ArrayList<>()).add(element);
            }
            
            for(List<Element> patternList : patternMap.values()) {
                if(patternList.size() > 1) {
                    patterns.add(patternList);
                }
            }
            
            return patterns;
        }
        
        private String getElementPatternKey(Element element) {
            StringBuilder key = new StringBuilder();
            key.append(element.getTagName());
            
            var attributes = element.getAttributes();
            List<String> attrNames = new ArrayList<>();
            for(int i = 0; i < attributes.getLength(); i++) {
                attrNames.add(attributes.item(i).getNodeName());
            }
            attrNames.sort(String::compareTo);
            key.append("[").append(String.join(",", attrNames)).append("]");
            
            NodeList children = element.getChildNodes();
            int elementChildCount = 0;
            for(int i = 0; i < children.getLength(); i++) {
                if(children.item(i) instanceof Element) {
                    elementChildCount++;
                    key.append(((Element) children.item(i)).getTagName());
                }
            }
            key.append("(").append(elementChildCount).append(")");
            
            return key.toString();
        }
        
        public List<List<String>> extractTableData(String rowXPath, String... cellXPaths) throws Exception {
            List<List<String>> tableData = new ArrayList<>();
            Selector selector = new Selector("");
            selector.document = this.document;
            selector.xpath = XPathFactory.newInstance().newXPath();
            
            List<Element> rows = selector.select(rowXPath);
            for(Element row : rows) {
                List<String> rowData = new ArrayList<>();
                for(String cellXPath : cellXPaths) {
                    String cellValue = selector.getText(row.getTagName() + cellXPath);
                    rowData.add(cellValue != null ? cellValue : "");
                }
                tableData.add(rowData);
            }
            
            return tableData;
        }
        
        private String elementToString(Element element) {
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("<").append(element.getTagName());
                
                var attributes = element.getAttributes();
                for(int i = 0; i < attributes.getLength(); i++) {
                    var attr = attributes.item(i);
                    sb.append(" ").append(attr.getNodeName())
                      .append("=\"").append(attr.getNodeValue()).append("\"");
                }
                
                sb.append(">");
                sb.append(element.getTextContent());
                sb.append("</").append(element.getTagName()).append(">");
                
                return sb.toString();
            } catch(Exception err) {
                return "";
            }
        }
        
        private String documentToString(Document doc) {
            try {
                return doc.getDocumentElement().getTextContent();
            } catch(Exception err) {
                return "";
            }
        }
        
        public Document getDocument() {
            return document;
        }
    }
    
    public static Selector createSelector(String filePath) throws Exception {
        return new Selector(filePath);
    }
    
    public static Selector createSelectorFromString(String xml) throws Exception {
        return new Selector("").fromString(xml);
    }
    
    public static RangeParser createRangeParser(String filePath) throws Exception {
        return new RangeParser(filePath);
    }
    
    public static RangeParser createRangeParserFromString(String xml) throws Exception {
        return new RangeParser("").fromString(xml);
    }
}