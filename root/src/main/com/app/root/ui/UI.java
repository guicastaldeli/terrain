package main.com.app.root.ui;
import main.com.app.root.Window;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root._text_renderer.TextRenderer;
import main.com.app.root.screen_controller.Screen;
import main.com.app.root._font.FontConfig;
import main.com.app.root._font.FontMap;
import main.com.app.root.DocParser;
import org.joml.Vector2f;
import java.util.ArrayList;
import java.util.List;

public class UI {
    public static final String UI_DIR = "root/src/main/com/app/root/ui/";

    public static Window window;
    public static ShaderProgram shaderProgram;
    private static UIController uiController;
    private TextRenderer textRenderer;

    public String uiName;
    public String filePath;
    public UIData uiData;
    public boolean visible;

    public UI(String filePath, String uiName) {
        this.filePath = filePath;
        this.uiName = uiName;
        try {
            FontConfig fontConfig = FontMap.getFont("arial");
            this.textRenderer = new TextRenderer(
                window,
                shaderProgram,
                fontConfig.path,
                fontConfig.size,
                window.getWidth(),
                window.getHeight()
            );

            this.uiData = DocParser.parseUI(
                filePath, 
                Screen.window.getWidth(), 
                Screen.window.getHeight()
            );
            
            System.out.println("ui initialized successfully");
            System.out.println("TextRenderer: " + textRenderer);
            System.out.println("uiData: " + uiData);
            System.out.println("Elements count: " + (uiData != null ? uiData.elements.size() : 0));
        } catch (Exception err) {
            System.err.println("Failed to init ui: " + uiName);
            System.err.println("Error: " + err.getMessage());
            err.printStackTrace();
        }
    }
    private void init(
        Window window, 
        ShaderProgram shaderProgram, 
        UIController uiController
    ) {
        try {
            UI.window = window;
            UI.shaderProgram = shaderProgram;
            UI.uiController = uiController;
        } catch (Exception e) {
            System.err.println("Failed to initialize UI: " + uiName);
            e.printStackTrace();
        }
    }
}
