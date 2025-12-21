package main.com.app.root.screen_controller.title;
import main.com.app.root.DocParser;
import main.com.app.root.Window;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.screen_controller.Screen;
import main.com.app.root.screen_controller.ScreenController;

public class TitleScreen extends Screen {
    private final Window window;
    private final ShaderProgram shaderProgram;
    private final ScreenController screenController;
    private static final String SCREEN_PATH = "C:/Users/casta/OneDrive/Desktop/vscode/terrain/root/src/main/com/app/root/screen_controller/title/title_screen.xml";

    private static final String FONT_PATH = "C:/Users/casta/OneDrive/Desktop/vscode/terrain/root/src/main/com/app/root/_text/font/arial.ttf";
    private static float fontSize = 24.0f;

    public TitleScreen(
        Window window,
        ShaderProgram shaderProgram, 
        ScreenController screenController
    ) {
        super(
            window,
            shaderProgram,
            SCREEN_PATH,
            "title",
            FONT_PATH,
            fontSize
        );
        this.shaderProgram = shaderProgram;
        this.screenController = screenController;
        this.window = window;
    }

    @Override
    public void handleAction(String action) {
        
    }

    @Override
    public void handleKeyPress(int ket, int action) {
        
    }

    /**
     * Window Resize
     */
    @Override
    public void onWindowResize(int width, int height) {
        if(getTextRenderer() != null) {
            getTextRenderer().updateScreenSize(width, height);
        }

        try {
            this.screenData = DocParser.parseScreen(
                SCREEN_PATH,
                width,
                height
            );
        } catch (Exception err) {
            System.err.println("Failed to re-parse screen on resize: " + err.getMessage());
        }
    }
}
