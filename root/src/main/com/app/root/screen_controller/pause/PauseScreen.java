package main.com.app.root.screen_controller.pause;
import main.com.app.root.DocParser;
import main.com.app.root.screen_controller.Screen;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

public class PauseScreen extends Screen {
    private static final String SCREEN_PATH = DIR + "pause/pause_screen.xml";

    private PauseScreenAction pauseScreenAction;

    public PauseScreen() {
        super(SCREEN_PATH, "pause");
        this.pauseScreenAction = new PauseScreenAction(
            screenController, 
            this,
            stateController,
            saveGenerator
        );
    }

    @Override
    public void handleAction(String action) {
        switch (action) {
            case "continue":
                pauseScreenAction.togglePause();
                break;
            case "save":
                pauseScreenAction.save();
                break;
            case "settings":
                pauseScreenAction.openSettings();
                break;
            case "exit":
                pauseScreenAction.exitToMenu();
                break;
        }
    }

    @Override
    public void handleKeyPress(int key, int action) {
        if(key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
            pauseScreenAction.togglePause();
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
