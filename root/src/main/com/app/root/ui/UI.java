package main.com.app.root.ui;
import main.com.app.root.Window;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root._text_renderer.TextRenderer;
import main.com.app.root.mesh.Mesh;
import main.com.app.root._font.FontConfig;
import main.com.app.root._font.FontMap;
import main.com.app.root.Console;
import main.com.app.root.DocParser;
import main.com.app.root.Upgrader;
import java.util.List;

public class UI implements UIHandler {
    public static final String DIR = "root/src/main/com/app/root/ui/";

    public static Window window;
    public static ShaderProgram shaderProgram;
    public static UIController uiController;
    public static Upgrader upgrader;
    public TextRenderer textRenderer;
    public static Mesh mesh;

    public String uiName;
    public String filePath;
    public UIData uiData;
    public boolean visible;
    private UIElement uiElement;

    public static void init(
        Window window, 
        ShaderProgram shaderProgram, 
        UIController uiController,
        Upgrader upgrader,
        Mesh mesh
    ) {
        try {
            UI.window = window;
            UI.shaderProgram = shaderProgram;
            UI.uiController = uiController;
            UI.upgrader = upgrader;
            UI.mesh = mesh;
        } catch(Exception e) {
            //System.err.println("Failed to initialize UI: " + UI.uinam);
            e.printStackTrace();
        }
    }
    public UI(String filePath, String uiName) {
        this.filePath = filePath;
        this.uiName = uiName;
        try {
            FontConfig fontConfig = FontMap.getFont("arial");
            this.textRenderer = new TextRenderer(
                window,
                shaderProgram,
                window.getWidth(),
                window.getHeight()
            );

            this.uiData = DocParser.parseUI(
                filePath, 
                window.getWidth(), 
                window.getHeight()
            );
            
            System.out.println("ui initialized successfully");
            System.out.println("TextRenderer: " + textRenderer);
            System.out.println("uiData: " + uiData);
            System.out.println("Elements count: " + (uiData != null ? uiData.elements.size() : 0));
        } catch(Exception err) {
            System.err.println("Failed to init ui: " + uiName);
            System.err.println("Error: " + err.getMessage());
            err.printStackTrace();
        }
    }

    @Override
    public void onShow() {
        this.visible = true;
    }

    @Override
    public void onHide() {
        this.visible = false;
    }

    public boolean isClickable() {
        return 
            uiElement.type.equals("button") || 
            uiElement.type.equals("upgrade_button") || 
            uiElement.type.equals("equip_button");
    }

    public String checkClick(int mouseX, int mouseY) {
        if(!visible) return null;

        List<UIElement> buttons = DocParser.getElementsByType(uiData, "button");
        for(UIElement button : buttons) {
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

    public TextRenderer getTextRenderer() {
        return textRenderer;
    }
}
