package main.com.app.root;
import main.com.app.root.ui.UIData;
import main.com.app.root.ui.UIElement;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root._text_renderer.TextRenderer;
import main.com.app.root.screen.ScreenData;
import main.com.app.root.screen.ScreenElement;
import javax.script.ScriptException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.lwjgl.BufferUtils;
import java.nio.FloatBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class DocParser {
    private static int uiVao = 0;
    private static int uiVbo = 0;
    private static int uiEbo = 0;
    private static boolean uiBuffersInitialized = false;

    private static ScriptEngine engine;
    
    /**
     * Parse
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
            parseEl(root, screenData.elements, screenWidth, screenHeight, null);
        } catch(Exception err) {
            System.err.println("Error parsing screen XML: " + err.getMessage());
            err.printStackTrace();
        }

        return screenData;
    }
    public static UIData parseUI(String filePath, int screenWidth, int screenHeight) {
        UIData uiData = new UIData(filePath);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(filePath));

            Element root = document.getDocumentElement();
            uiData.uiType = root.getTagName();

            parseAttr(root, uiData.uiAttr);
            parseEl(root, uiData.elements, screenWidth, screenHeight, null);
        } catch(Exception err) {
            System.err.println("Error parsing screen XML: " + err.getMessage());
            err.printStackTrace();
        }

        return uiData;
    }

    private static void parseEl(
        Element parent, 
        List<ScreenElement> elements,
        int screenWidth,
        int screenHeight,
        ScreenElement parentElement
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
                screenHeight,
                parentElement
            );
            
            if(screenElement != null) {
                elements.add(screenElement);
                parseEl(
                    element, 
                    elements,
                    screenWidth,
                    screenHeight,
                    screenElement
                );
            }
        }
    }
    private static void parseEl(
        Element parent, 
        List<UIElement> elements,
        int screenWidth,
        int screenHeight,
        UIElement parentElement
    ) {
        NodeList children = parent.getChildNodes();
        
        for(int i = 0; i < children.getLength(); i++) {
            if(!(children.item(i) instanceof Element)) continue;
            
            Element element = (Element) children.item(i);
            String tagName = element.getTagName();
            
            UIElement uiElement = createScreenElement(
                element, 
                tagName,
                screenWidth,
                screenHeight,
                parentElement
            );
            
            if(uiElement != null) {
                elements.add(uiElement);
                parseEl(
                    element, 
                    elements,
                    screenWidth,
                    screenHeight,
                    uiElement
                );
            }
        }
    }
    
    private static ScreenElement createScreenElement(
        Element element, 
        String type, 
        int screenWidth, 
        int screenHeight,
        ScreenElement parentElement
    ) {
        String text = element.getTextContent().trim();
        String id = element.hasAttribute("id") ? element.getAttribute("id") : "";
        
        int x = parseCoordinate(element, "x", screenWidth, 1280);
        int y = parseCoordinate(element, "y", screenHeight, 720);
        if(parentElement != null) {
            x += parentElement.x;
            y += parentElement.y;
        }
        
        int width = parseSize(element, "width", screenWidth, 1280, 100);
        int height = parseSize(element, "height", screenHeight, 720, 50);
        
        float scale = 
            element.hasAttribute("scale") ? 
            Float.parseFloat(element.getAttribute("scale")) : 
            1.0f;
        
        float[] color = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
        if(element.hasAttribute("color")) {
            String colorStr = element.getAttribute("color");
            String[] colorParts = colorStr.split(",");
            if(colorParts.length >= 3) {
                color[0] = Float.parseFloat(colorParts[0]);
                color[1] = Float.parseFloat(colorParts[1]);
                color[2] = Float.parseFloat(colorParts[2]);
                if(colorParts.length >= 4) {
                    color[3] = Float.parseFloat(colorParts[3]);
                }
            }
        }
        boolean hasBackground = 
            type.equals("div") || 
            type.equals("container");
        if(element.hasAttribute("background")) {
            hasBackground = true;
            String bgStr = element.getAttribute("background");
            String[] bgParts = bgStr.split(",");
            if(bgParts.length >= 3) {
                color = new float[] {
                    Float.parseFloat(bgParts[0]),
                    Float.parseFloat(bgParts[1]),
                    Float.parseFloat(bgParts[2]),
                    bgParts.length >= 4 ? Float.parseFloat(bgParts[3]) : 1.0f
                };
            }
        }
        String fontFamily = "arial";
        if(element.hasAttribute("fontFamily")) {
            fontFamily = element.getAttribute("fontFamily").toLowerCase();
        }
        
        String action = element
            .hasAttribute("action") ? 
            element.getAttribute("action") : 
            "";
        
        float borderWidth = 
            element.hasAttribute("border") ? 
            Float.parseFloat(element.getAttribute("border")) : 
            0.0f;
        
        float[] borderColor = new float[]{0.0f, 0.0f, 0.0f, 1.0f};
        if(element.hasAttribute("borderColor")) {
            String borderColorStr = element.getAttribute("borderColor");
            String[] borderParts = borderColorStr.split(",");
            if(borderParts.length >= 3) {
                borderColor = new float[] {
                    Float.parseFloat(borderParts[0]),
                    Float.parseFloat(borderParts[1]),
                    Float.parseFloat(borderParts[2]),
                    borderParts.length >= 4 ? Float.parseFloat(borderParts[3]) : 1.0f
                };
            }
        }
        if(element.hasAttribute("text")) {
            String textAttr = element.getAttribute("text");
            text = evaluateExpression(textAttr);
        } else {
            text = evaluateExpression(text);
        }
        
        ScreenElement screenElement = new ScreenElement(
            type, 
            id, 
            text, 
            fontFamily,
            x, y,
            width, height,
            scale, 
            color, 
            action
        );
        
        screenElement.borderWidth = borderWidth;
        screenElement.borderColor = borderColor;
        if(element.hasAttribute("background")) screenElement.hasBackground = true;
        parseAttr(element, screenElement.attr);
        
        if(element.hasAttribute("hoverable")) {
            screenElement.hoverable = Boolean.parseBoolean(element.getAttribute("hoverable"));
        }
        
        if(type.equals("button")) {
            screenElement.hoverable = true;
        }
        if(type.equals("container")) {
            screenElement.hoverable = true;
        }
        if(type.equals("div")) {
            screenElement.hoverable = true;
        }
        
        if(element.hasAttribute("hoverColor")) {
            String hoverColorStr = element.getAttribute("hoverColor");
            String[] parts = hoverColorStr.split(",");
            if(parts.length >= 3) {
                screenElement.hoverColor = new float[]{
                    Float.parseFloat(parts[0]),
                    Float.parseFloat(parts[1]),
                    Float.parseFloat(parts[2]),
                    parts.length >= 4 ? Float.parseFloat(parts[3]) : 1.0f
                };
            }
        }
        
        if(element.hasAttribute("hoverTextColor")) {
            String hoverTextColorStr = element.getAttribute("hoverTextColor");
            screenElement.hoverTextColor = parseColor(hoverTextColorStr);
            if(screenElement.hoverColor == null) {
                screenElement.hoverColor = screenElement.hoverTextColor.clone();
            }
        }
        
        if(element.hasAttribute("hoverBorderColor")) {
            String hoverBorderColorStr = element.getAttribute("hoverBorderColor");
            String[] parts = hoverBorderColorStr.split(",");
            if(parts.length >= 3) {
                screenElement.hoverBorderColor = new float[]{
                    Float.parseFloat(parts[0]),
                    Float.parseFloat(parts[1]),
                    Float.parseFloat(parts[2]),
                    parts.length >= 4 ? Float.parseFloat(parts[3]) : 1.0f
                };
            }
        }
        
        if(element.hasAttribute("hoverScale")) {
            screenElement.hoverScale = Float.parseFloat(element.getAttribute("hoverScale"));
        }
        
        if(element.hasAttribute("visible")) {
            screenElement.visible = Boolean.parseBoolean(element.getAttribute("visible"));
        }
        if(element.hasAttribute("textShadow")) {
            String shadowStr = element.getAttribute("textShadow");
            String[] shadowParts = shadowStr.split(" ");
            
            if(shadowParts.length >= 2) {
                screenElement.hasShadow = true;
                
                String[] offsetParts = shadowParts[0].split(",");
                if(offsetParts.length >= 2) {
                    screenElement.shadowOffsetX = Float.parseFloat(offsetParts[0].replace("px", "").trim());
                    screenElement.shadowOffsetY = Float.parseFloat(offsetParts[1].replace("px", "").trim());
                }
                
                if(shadowParts.length >= 2) {
                    screenElement.shadowBlur = Float.parseFloat(shadowParts[1].replace("px", "").trim());
                }
                
                if(shadowParts.length >= 3) {
                    String[] colorParts = shadowParts[2].split(",");
                    if(colorParts.length >= 3) {
                        screenElement.shadowColor[0] = Float.parseFloat(colorParts[0]);
                        screenElement.shadowColor[1] = Float.parseFloat(colorParts[1]);
                        screenElement.shadowColor[2] = Float.parseFloat(colorParts[2]);
                        screenElement.shadowColor[3] = colorParts.length >= 4 ? 
                            Float.parseFloat(colorParts[3]) : 0.5f;
                    }
                }
            }
        }
        
        return screenElement;
    }
    private static UIElement createScreenElement(
        Element element, 
        String type, 
        int screenWidth, 
        int screenHeight,
        UIElement parentElement
    ) {
        String text = element.getTextContent().trim();
        String id = element.hasAttribute("id") ? element.getAttribute("id") : "";
        
        int x = parseCoordinate(element, "x", screenWidth, 1280);
        int y = parseCoordinate(element, "y", screenHeight, 720);
        if(parentElement != null) {
            x += parentElement.x;
            y += parentElement.y;
        }
        
        int width = parseSize(element, "width", screenWidth, 1280, 100);
        int height = parseSize(element, "height", screenHeight, 720, 50);
        
        float scale = 
            element.hasAttribute("scale") ? 
            Float.parseFloat(element.getAttribute("scale")) : 
            1.0f;
        
        float[] color = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
        if(element.hasAttribute("color")) {
            String colorStr = element.getAttribute("color");
            String[] colorParts = colorStr.split(",");
            if(colorParts.length >= 3) {
                color[0] = Float.parseFloat(colorParts[0]);
                color[1] = Float.parseFloat(colorParts[1]);
                color[2] = Float.parseFloat(colorParts[2]);
                if(colorParts.length >= 4) {
                    color[3] = Float.parseFloat(colorParts[3]);
                }
            }
        }
        boolean hasBackground = 
            type.equals("div") || 
            type.equals("container");
        if(element.hasAttribute("background")) {
            String bgStr = element.getAttribute("background");
            String[] bgParts = bgStr.split(",");
            if(bgParts.length >= 3) {
                color = new float[] {
                    Float.parseFloat(bgParts[0]),
                    Float.parseFloat(bgParts[1]),
                    Float.parseFloat(bgParts[2]),
                    bgParts.length >= 4 ? Float.parseFloat(bgParts[3]) : 1.0f
                };
            }
        }
        float[] textColor = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
        if(element.hasAttribute("textColor")) {
            String textColorStr = element.getAttribute("textColor");
            String[] parts = textColorStr.split(",");
            if(parts.length >= 3) {
                textColor[0] = Float.parseFloat(parts[0]);
                textColor[1] = Float.parseFloat(parts[1]);
                textColor[2] = Float.parseFloat(parts[2]);
                if(parts.length >= 4) {
                    textColor[3] = Float.parseFloat(parts[3]);
                }
            }
        }
        String fontFamily = "arial";
        if(element.hasAttribute("fontFamily")) {
            fontFamily = element.getAttribute("fontFamily").toLowerCase();
        }
        
        String action = element
            .hasAttribute("action") ? 
            element.getAttribute("action") : 
            "";
        
        float borderWidth = 
            element.hasAttribute("border") ? 
            Float.parseFloat(element.getAttribute("border")) : 
            0.0f;
        
        float[] borderColor = new float[]{0.0f, 0.0f, 0.0f, 1.0f};
        if(element.hasAttribute("borderColor")) {
            String borderColorStr = element.getAttribute("borderColor");
            String[] borderParts = borderColorStr.split(",");
            if(borderParts.length >= 3) {
                borderColor = new float[] {
                    Float.parseFloat(borderParts[0]),
                    Float.parseFloat(borderParts[1]),
                    Float.parseFloat(borderParts[2]),
                    borderParts.length >= 4 ? Float.parseFloat(borderParts[3]) : 1.0f
                };
            }
        }

        if(element.hasAttribute("text")) {
            String textAttr = element.getAttribute("text");
            text = evaluateExpression(textAttr);
        } else {
            text = evaluateExpression(text);
        }
        
        UIElement uiElement = new UIElement(
            type, 
            id, 
            text, 
            x, y,
            width, height,
            scale, 
            color, 
            hasBackground,
            action
        );
        
        uiElement.borderWidth = borderWidth;
        uiElement.borderColor = borderColor;
        uiElement.fontFamily = fontFamily;
        if(element.hasAttribute("background")) uiElement.hasBackground = true;
        parseAttr(element, uiElement.attr);
        
        if(element.hasAttribute("hoverable")) {
            uiElement.hoverable = Boolean.parseBoolean(element.getAttribute("hoverable"));
        }
        
        if(type.equals("button")) {
            uiElement.hoverable = true;
        }
        
        if(element.hasAttribute("hoverColor")) {
            String hoverColorStr = element.getAttribute("hoverColor");
            String[] parts = hoverColorStr.split(",");
            if(parts.length >= 3) {
                uiElement.hoverColor = new float[]{
                    Float.parseFloat(parts[0]),
                    Float.parseFloat(parts[1]),
                    Float.parseFloat(parts[2]),
                    parts.length >= 4 ? Float.parseFloat(parts[3]) : 1.0f
                };
            }
        }
        
        if(element.hasAttribute("hoverTextColor")) {
            String hoverTextColorStr = element.getAttribute("hoverTextColor");
            String[] parts = hoverTextColorStr.split(",");
            if(parts.length >= 3) {
                uiElement.hoverTextColor = new float[]{
                    Float.parseFloat(parts[0]),
                    Float.parseFloat(parts[1]),
                    Float.parseFloat(parts[2]),
                    parts.length >= 4 ? Float.parseFloat(parts[3]) : 1.0f
                };
            }
        }
        
        if(element.hasAttribute("hoverBorderColor")) {
            String hoverBorderColorStr = element.getAttribute("hoverBorderColor");
            String[] parts = hoverBorderColorStr.split(",");
            if(parts.length >= 3) {
                uiElement.hoverBorderColor = new float[]{
                    Float.parseFloat(parts[0]),
                    Float.parseFloat(parts[1]),
                    Float.parseFloat(parts[2]),
                    parts.length >= 4 ? Float.parseFloat(parts[3]) : 1.0f
                };
            }
        }
        
        if(element.hasAttribute("hoverScale")) {
            uiElement.hoverScale = Float.parseFloat(element.getAttribute("hoverScale"));
        }
        
        if(element.hasAttribute("visible")) {
            uiElement.visible = Boolean.parseBoolean(element.getAttribute("visible"));
        }
        if(element.hasAttribute("textShadow")) {
            String shadowStr = element.getAttribute("textShadow");
            String[] shadowParts = shadowStr.split(" ");
            
            if(shadowParts.length >= 2) {
                uiElement.hasShadow = true;
                
                String[] offsetParts = shadowParts[0].split(",");
                if(offsetParts.length >= 2) {
                    uiElement.shadowOffsetX = Float.parseFloat(offsetParts[0].replace("px", "").trim());
                    uiElement.shadowOffsetY = Float.parseFloat(offsetParts[1].replace("px", "").trim());
                }
                
                if(shadowParts.length >= 2) {
                    uiElement.shadowBlur = Float.parseFloat(shadowParts[1].replace("px", "").trim());
                }
                
                if(shadowParts.length >= 3) {
                    String[] colorParts = shadowParts[2].split(",");
                    if(colorParts.length >= 3) {
                        uiElement.shadowColor[0] = Float.parseFloat(colorParts[0]);
                        uiElement.shadowColor[1] = Float.parseFloat(colorParts[1]);
                        uiElement.shadowColor[2] = Float.parseFloat(colorParts[2]);
                        uiElement.shadowColor[3] = colorParts.length >= 4 ? 
                            Float.parseFloat(colorParts[3]) : 0.5f;
                    }
                }
            }
        }
        
        return uiElement;
    }

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
    
    private static int parseSize(Element element, String attrName, int currentScreenSize, int originalScreenSize, int defaultValue) {
        if(!element.hasAttribute(attrName)) return defaultValue;
        
        String sizeStr = element.getAttribute(attrName);
        if(sizeStr.endsWith("%")) {
            float percentage = Float.parseFloat(sizeStr.replace("%", "")) / 100.0f;
            return (int)(currentScreenSize * percentage);
        } else if(sizeStr.equals("auto")) {
            return defaultValue;
        } else {
            int originalSize = Integer.parseInt(sizeStr);
            float scaleFactor = (float)currentScreenSize / originalScreenSize;
            return (int)(originalSize * scaleFactor);
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
            if(element.type.equals(type) && element.visible) {
                result.add(element);
            }
        }
        return result;
    }
    public static List<UIElement> getElementsByType(UIData uiData, String type) {
        List<UIElement> result = new ArrayList<>();
        for(UIElement element : uiData.elements) {
            if(element.type.equals(type) && element.visible) {
                result.add(element);
            }
        }
        return result;
    }
    
    public static List<ScreenElement> getDivElements(ScreenData screenData) {
        return getElementsByType(screenData, "div");
    }
    
    /**
     * Get Element By Id
     */
    public static ScreenElement getElementById(ScreenData screenData, String id) {
        for(ScreenElement element : screenData.elements) {
            if(element.id.equals(id) && element.visible) {
                return element;
                }
            }
            return null;
        }
        
        public static void initUIRendering() {
        if(uiBuffersInitialized) return;
        
        uiVao = glGenVertexArrays();
        uiVbo = glGenBuffers();
        uiEbo = glGenBuffers();
        
        glBindVertexArray(uiVao);
        
        glBindBuffer(GL_ARRAY_BUFFER, uiVbo);
        glBufferData(GL_ARRAY_BUFFER, 4 * 8 * 4, GL_DYNAMIC_DRAW);
        
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 6 * 4, 0);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, 4, GL_FLOAT, false, 6 * 4, 2 * 4);
        glEnableVertexAttribArray(2);
        
        int[] indices = {0, 1, 2, 2, 3, 0};
        java.nio.IntBuffer indicesBuffer = BufferUtils.createIntBuffer(indices.length);
        indicesBuffer.put(indices);
        indicesBuffer.flip();
        
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, uiEbo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);
        
        glBindVertexArray(0);
        uiBuffersInitialized = true;
    }
    
    public static void renderUIElement(
        ScreenElement element,
        int screenWidth,
        int screenHeight,
        ShaderProgram shaderProgram
    ) {
        if(!element.visible || !element.hasBackground) return;
        
        initUIRendering();
        
        float x1 = element.x;
        float y1 = element.y;
        float x2 = element.x + element.width;
        float y2 = element.y + element.height;
        
        boolean depthTest = glGetBoolean(GL_DEPTH_TEST);
        if(depthTest) glDisable(GL_DEPTH_TEST);
        
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        shaderProgram.bind();
        shaderProgram.setUniform("shaderType", 3);
        shaderProgram.setUniform("screenSize", (float) screenWidth, (float) screenHeight);
        
        float[] vertices = {
            x1, y1, element.getRed(), element.getGreen(), element.getBlue(), element.getAlpha(),
            x1, y2, element.getRed(), element.getGreen(), element.getBlue(), element.getAlpha(),
            x2, y2, element.getRed(), element.getGreen(), element.getBlue(), element.getAlpha(),
            x2, y1, element.getRed(), element.getGreen(), element.getBlue(), element.getAlpha()
        };
        
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.length);
        vertexBuffer.put(vertices);
        vertexBuffer.flip();
        
        glBindVertexArray(uiVao);
        glBindBuffer(GL_ARRAY_BUFFER, uiVbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
    
        
        glBindVertexArray(0);
        shaderProgram.unbind();
        
        if(depthTest) glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
    }
    public static void renderUIElement(
        UIElement element,
        int screenWidth,
        int screenHeight,
        ShaderProgram shaderProgram
    ) {
        if(!element.visible || !element.hasBackground) return;
        
        initUIRendering();
        
        float x1 = element.x;
        float y1 = element.y;
        float x2 = element.x + element.width;
        float y2 = element.y + element.height;
        
        boolean depthTest = glGetBoolean(GL_DEPTH_TEST);
        if(depthTest) glDisable(GL_DEPTH_TEST);
        
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        shaderProgram.bind();
        shaderProgram.setUniform("shaderType", 3);
        shaderProgram.setUniform("screenSize", (float) screenWidth, (float) screenHeight);
        
        float[] vertices = {
            x1, y1, element.getRed(), element.getGreen(), element.getBlue(), element.getAlpha(),
            x1, y2, element.getRed(), element.getGreen(), element.getBlue(), element.getAlpha(),
            x2, y2, element.getRed(), element.getGreen(), element.getBlue(), element.getAlpha(),
            x2, y1, element.getRed(), element.getGreen(), element.getBlue(), element.getAlpha()
        };
        
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.length);
        vertexBuffer.put(vertices);
        vertexBuffer.flip();
        
        glBindVertexArray(uiVao);
        glBindBuffer(GL_ARRAY_BUFFER, uiVbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
    
        
        glBindVertexArray(0);
        shaderProgram.unbind();
        
        if(depthTest) glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
    }
    
    private static void renderBorder(
        float x1, float y1, float x2, float y2,
        float borderWidth, float[] borderColor
    ) {
        if(borderWidth <= 0) return;
        
        glLineWidth(borderWidth);
        
        float[] borderVertices = {
            x1, y1, borderColor[0], borderColor[1],
            x2, y1, borderColor[0], borderColor[1],
            x2, y2, borderColor[2], borderColor[3],
            x1, y2, borderColor[2], borderColor[3],
            x1, y1, borderColor[0], borderColor[1]
        };
        
        FloatBuffer borderBuffer = BufferUtils.createFloatBuffer(borderVertices.length);
        borderBuffer.put(borderVertices);
        borderBuffer.flip();
        
        glBindBuffer(GL_ARRAY_BUFFER, uiVbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0, borderBuffer);
        
        glDrawArrays(GL_LINE_LOOP, 0, 5);
    }
    
    public static void renderScreen(
        ScreenData screenData,
        int screenWidth,
        int screenHeight,
        ShaderProgram shaderProgram,
        TextRenderer textRenderer
    ) {
        if(screenData == null || screenData.elements.isEmpty()) return;
        
        for(ScreenElement element : screenData.elements) {
            if(element.visible && element.type.equals("div")) {
                renderUIElement(element, screenWidth, screenHeight, shaderProgram);
            }
        }
        for(ScreenElement element : screenData.elements) {
            if(element.visible && element.type.equals("button")) {
                renderUIElement(element, screenWidth, screenHeight, shaderProgram);
                
                if(textRenderer != null && element.text != null && !element.text.isEmpty()) {
                    if(element.hasShadow) {
                        textRenderer.renderTextWithShadow(
                            element.text,
                            element.x,
                            element.y,
                            element.scale,
                            element.color,
                            element.shadowOffsetX,
                            element.shadowOffsetY,
                            element.shadowBlur,
                            element.shadowColor,
                            element.fontFamily
                        );
                    } else {
                        textRenderer.renderText(
                            element.text,
                            element.x,
                            element.y,
                            element.scale,
                            element.color,
                            element.fontFamily
                        );
                    }
                }
            }
        }
        for(ScreenElement element : screenData.elements) {
            if(element.visible && element.type.equals("label")) {
                if(textRenderer != null && element.text != null && !element.text.isEmpty()) {
                    if(element.hasShadow) {
                        textRenderer.renderTextWithShadow(
                            element.text,
                            element.x,
                            element.y,
                            element.scale,
                            element.color,
                            element.shadowOffsetX,
                            element.shadowOffsetY,
                            element.shadowBlur,
                            element.shadowColor,
                            element.fontFamily
                        );
                    } else {
                        textRenderer.renderText(
                            element.text,
                            element.x,
                            element.y,
                            element.scale,
                            element.color,
                            element.fontFamily
                        );
                    }
                }
            }
        }
    }
    public static void renderUI(
        UIData uiData,
        int screenWidth,
        int screenHeight,
        ShaderProgram shaderProgram,
        TextRenderer textRenderer
    ) {
        if(uiData == null || uiData.elements.isEmpty()) return;
        
        for(UIElement element : uiData.elements) {
            if(element.visible && element.type.equals("div")) {
                renderUIElement(element, screenWidth, screenHeight, shaderProgram);
            }
        }
        
        for(UIElement element : uiData.elements) {
            if(element.visible && element.type.equals("button")) {
                renderUIElement(element, screenWidth, screenHeight, shaderProgram);
                
                if(textRenderer != null && element.text != null && !element.text.isEmpty()) {
                    if(element.hasShadow) {
                        textRenderer.renderTextWithShadow(
                            element.text,
                            element.x,
                            element.y,
                            element.scale,
                            element.color,
                            element.shadowOffsetX,
                            element.shadowOffsetY,
                            element.shadowBlur,
                            element.shadowColor,
                            element.fontFamily
                        );
                    } else {
                        textRenderer.renderText(
                            element.text,
                            element.x,
                            element.y,
                            element.scale,
                            element.color,
                            element.fontFamily
                        );
                    }
                }
            }
        }
        
        for(UIElement element : uiData.elements) {
            if(element.visible && element.type.equals("label")) {
                if(textRenderer != null && element.text != null && !element.text.isEmpty()) {
                    if(element.hasShadow) {
                        textRenderer.renderTextWithShadow(
                            element.text,
                            element.x,
                            element.y,
                            element.scale,
                            element.color,
                            element.shadowOffsetX,
                            element.shadowOffsetY,
                            element.shadowBlur,
                            element.shadowColor,
                            element.fontFamily
                        );
                    } else {
                        textRenderer.renderText(
                            element.text,
                            element.x,
                            element.y,
                            element.scale,
                            element.color,
                            element.fontFamily
                        );
                    }
                }
            }
        }
    }
    
    public static void cleanup() {
        if(uiVao != 0) glDeleteVertexArrays(uiVao);
        if(uiVbo != 0) glDeleteBuffers(uiVbo);
        if(uiEbo != 0) glDeleteBuffers(uiEbo);
        uiBuffersInitialized = false;
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
    
    public static List<ScreenElement> parseDivs(
        String xmlFilePath,
        int screenWidth,
        int screenHeight
    ) {
        ScreenData screenData = parseScreen(
            xmlFilePath,
            screenWidth,
            screenHeight
        );
        return getElementsByType(screenData, "div");
    }

    private static float[] parseColor(String colorStr) {
        if(colorStr == null || colorStr.trim().isEmpty()) {
            return null;
        }
        
        String[] parts = colorStr.split(",");
        if(parts.length >= 3) {
            return new float[]{
                Float.parseFloat(parts[0].trim()),
                Float.parseFloat(parts[1].trim()),
                Float.parseFloat(parts[2].trim()),
                parts.length >= 4 ? Float.parseFloat(parts[3].trim()) : 1.0f
            };
        }
        return null;
    }

    /**
     * Script Engine
     */
    private static void initScriptEngine() {
        if (engine == null) {
            ScriptEngineManager manager = new ScriptEngineManager();
            engine = manager.getEngineByName("JavaScript");
        }
    }

    private static String evaluateExpression(String text) {
        if (text == null || !text.contains("${")) {
            return text;
        }
        
        Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
        Matcher matcher = pattern.matcher(text);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String expression = matcher.group(1);
            String replacement = evaluateSimpleExpression(expression);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    private static String evaluateSimpleExpression(String expression) {
        expression = expression.trim();
        
        Pattern repeatPattern = Pattern.compile("'(.*?)'\\.repeat\\((\\d+)\\)");
        Matcher repeatMatcher = repeatPattern.matcher(expression);
        if (repeatMatcher.matches()) {
            String text = repeatMatcher.group(1);
            int count = Integer.parseInt(repeatMatcher.group(2));
            return text.repeat(count);
        }
        
        if(expression.contains("+")) {
            String[] parts = expression.split("\\+");
            StringBuilder result = new StringBuilder();
            for (String part : parts) {
                part = part.trim().replaceAll("^'|'$", "");
                result.append(part);
            }
            return result.toString();
        }
        
        if(expression.startsWith("'") && expression.endsWith("'")) {
            return expression.substring(1, expression.length() - 1);
        }
        
        try {
            initScriptEngine();
            Object result = engine.eval(expression);
            return result != null ? result.toString() : expression;
        } catch (ScriptException e) {
            System.err.println("Could not evaluate expression: " + expression);
            return "${" + expression + "}";
        }
    }
}