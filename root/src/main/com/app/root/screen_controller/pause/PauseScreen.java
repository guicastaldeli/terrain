package main.com.app.root.screen_controller.pause;
import main.com.app.root.Console;
import main.com.app.root.DocParser;
import main.com.app.root.Window;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.screen_controller.Screen;
import main.com.app.root.screen_controller.ScreenController;
import main.com.app.root.screen_controller.ScreenController.SCREENS;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

public class PauseScreen extends Screen {
    private final Window window;
    private final ShaderProgram shaderProgram;
    private final ScreenController screenController;
    private static final String SCREEN_PATH = "C:/Users/casta/OneDrive/Desktop/vscode/terrain/root/src/main/com/app/root/screen_controller/pause/pause_screen.xml";

    private static final String FONT_PATH = "C:/Users/casta/OneDrive/Desktop/vscode/terrain/root/src/main/com/app/root/_text/font/arial.ttf";
    private static float fontSize = 24.0f;

    public PauseScreen(
        Window window,
        ShaderProgram shaderProgram, 
        ScreenController screenController
    ) {
        super(
            window,
            shaderProgram,
            SCREEN_PATH,
            "pause",
            FONT_PATH,
            fontSize
        );
        this.shaderProgram = shaderProgram;
        this.screenController = screenController;
        this.window = window;
    }

    @Override
    public void handleAction(String action) {
        switch (action) {
            case "continue":
                togglePause();
                break;
            case "exit":
                break;
            default:
                break;
        }
    }

    @Override
    public void handleKeyPress(int key, int action) {
        if(key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
            togglePause();
        }
    }

    public void togglePause() {
        boolean isCurrentlyActive = isActive();
        setActive(!isCurrentlyActive);
        
        if(!isCurrentlyActive) {
            Console.getInstance().pause();
            setActive(true);
            screenController.switchTo(SCREENS.PAUSE);
            screenController.enableCursor();
        } else {
            Console.getInstance().resume();
            setActive(false);
            screenController.switchTo(null);
            screenController.disableCursor();
        }
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
