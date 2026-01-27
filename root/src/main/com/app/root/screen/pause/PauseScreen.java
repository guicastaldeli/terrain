package main.com.app.root.screen.pause;
import main.com.app.root.DocParser;
import main.com.app.root.screen.Screen;
import main.com.app.root.screen.ScreenElement;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

public class PauseScreen extends Screen {
    private static final String SCREEN_PATH = DIR + "pause/pause_screen.xml";

    public PauseScreenAction pauseScreenAction;

    public PauseScreen() {
        super(SCREEN_PATH, "pause");
        this.pauseScreenAction = new PauseScreenAction(
            screenController, 
            this,
            stateController,
            saveGenerator,
            dataGetter
        );
    }

    private void resetHover() {
        if(screenData == null) return;
        for(ScreenElement element : screenData.elements) {
            if(element.hoverable && element.isHovered) {
                element.removeHover();
            }
        }
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
            resetHover();
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
        } catch(Exception err) {
            System.err.println("Failed to re-parse screen on resize: " + err.getMessage());
        }
    }

    @Override
    public void update() {
       if(lastMouseX >= 0 && lastMouseY >= 0) {
           handleMouseMove(lastMouseX, lastMouseY);
           System.out.println(lastMouseX);
        }
    }

    @Override
    public void handleMouseMove(int mouseX, int mouseY) {
        if(!active) return;
        
        for(ScreenElement element : screenData.elements) {
            if(element.visible && element.hoverable) {
                boolean wasHovered = element.isHovered;
                boolean isHovered = element.containsPoint(mouseX, mouseY);
                
                if(isHovered && !wasHovered) {
                    element.applyHover();
                } else if(!isHovered && wasHovered) {
                    element.removeHover();
                }
            }
        }
    }
}
