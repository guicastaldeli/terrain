package main.com.app.root.screen_controller;
import main.com.app.root.Window;
import main.com.app.root._save.SaveGenerator;
import main.com.app.root._save.SaveLoader;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root._text_renderer.TextRenderer;
import main.com.app.root.Console;
import main.com.app.root.DataController;
import main.com.app.root.DocParser;
import main.com.app.root.Scene;
import main.com.app.root.StateController;
import java.util.*;

public class Screen implements ScreenInputHandler {
    public static final String DIR = "root/src/main/com/app/root/screen_controller/";
    public static final String FONT_PATH = "root/src/main/com/app/root/_font/fonts/arial.ttf";
    private static float fontSize = 24.0f;

    public static Window window;
    public static ShaderProgram shaderProgram;
    public static ScreenController screenController;
    
    public static SaveGenerator saveGenerator;
    public static SaveLoader saveLoader;
    public static DataController dataController;
    public static StateController stateController;
    public static Scene scene;
    
    public TextRenderer textRenderer;
    public boolean active = false;
    public String screenName;
    public ScreenData screenData;

    public static void init(
        Window window,
        ShaderProgram shaderProgram, 
        ScreenController screenController,
        SaveGenerator saveGenerator,
        SaveLoader saveLoader,
        DataController dataController,
        StateController stateController
    ) {
        Screen.window = window;
        Screen.shaderProgram = shaderProgram;
        Screen.screenController = screenController;
        Screen.saveGenerator = saveGenerator;
        Screen.saveLoader = saveLoader;
        Screen.dataController = dataController;
        Screen.stateController = stateController;
    }
    public Screen(String filePath, String screenName) {
        this.screenName = screenName;
        try {
            System.out.println("Loading font from: " + FONT_PATH);
            System.out.println("Loading screen XML from: " + DIR + filePath);
            
            this.textRenderer = new TextRenderer(
                Screen.window,
                Screen.shaderProgram,
                FONT_PATH,
                fontSize,
                Screen.window.getWidth(),
                Screen.window.getHeight()
            );
            
            this.screenData = DocParser.parseScreen(
                filePath, 
                Screen.window.getWidth(), 
                Screen.window.getHeight()
            );
            
            System.out.println("Screen initialized successfully");
            System.out.println("TextRenderer: " + textRenderer);
            System.out.println("ScreenData: " + screenData);
            System.out.println("Elements count: " + (screenData != null ? screenData.elements.size() : 0));
        } catch (Exception err) {
            System.err.println("Failed to init screen: " + screenName);
            System.err.println("Error: " + err.getMessage());
            err.printStackTrace();
        }
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public String getScreenType() {
        return screenData.screenType;
    }
    
    public String getScreenName() {
        return screenName;
    }

    /**
     * Scene
     */
    public static void setScene(Scene scene) {
        Screen.scene = scene;
    }

    public static Scene getScene() {
        return scene;
    }

    public String checkClick(int mouseX, int mouseY) {
        if(!active) return null;

        List<ScreenElement> buttons = DocParser.getElementsByType(screenData, "button");
        for(ScreenElement button : buttons) {
            float width = textRenderer.getTextWidth(button.text, button.scale);
            float height = textRenderer.getFontMetrics().lineHeight * button.scale;

            if(mouseX >= button.x && 
                mouseX <= button.x + width &&
                mouseY >= button.y && 
                mouseY <= button.y + height
            ) {
                Console.getInstance().handleAction(button.action);
                return button.action;
            }
        }
        return null;
    }

    public ScreenElement getElementById(String id) {
        return DocParser.getElementById(screenData, id);
    }
    
    public List<ScreenElement> getElementsByType(String type) {
        return DocParser.getElementsByType(screenData, type);
    }

    public TextRenderer getTextRenderer() {
        return textRenderer;
    }
    
    /**
     * Render
     */
    public void render() {
        if(!active || textRenderer == null) {
            System.out.println("Screen not rendering: active=" + active + ", textRenderer=" + textRenderer);
            return;
        }
        
        for(ScreenElement el : screenData.elements) {
            textRenderer.renderText(
                el.text,
                el.x, el.y,
                el.scale,
                el.color   
            );
        }
    }
}
