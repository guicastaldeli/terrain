package main.com.app.root.screen_controller;
import main.com.app.root.screen_controller.main.MainScreen;
import main.com.app.root.screen_controller.pause.PauseScreen;
import main.com.app.root.DataController;
import main.com.app.root.StateController;
import main.com.app.root.Window;
import main.com.app.root._save.SaveGenerator;
import main.com.app.root._save.SaveLoader;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root._text_renderer.TextRenderer;

import java.util.HashMap;
import java.util.Map;
import static org.lwjgl.glfw.GLFW.*;

public class ScreenController {
    public enum SCREENS {
        MAIN,
        PAUSE
    }

    public final Window window;
    public final ShaderProgram shaderProgram;
    private final SaveGenerator saveGenerator;
    private final SaveLoader saveLoader;
    private final DataController dataController;
    private final StateController stateController;
    public TextRenderer textRenderer;
    public Screen screen;

    public Map<SCREENS, Screen> screens;
    public MainScreen mainScreen;
    public PauseScreen pauseScreen;

    public SCREENS activeScreen = null;
    public Screen currentScreen;

    private boolean[] keyPressed = new boolean[GLFW_KEY_LAST + 1];

    public ScreenController(
        Window window, 
        ShaderProgram shaderProgram,
        SaveGenerator saveGenerator,
        SaveLoader saveLoader,
        DataController dataController,
        StateController stateController
    ) {
        this.window = window;
        this.shaderProgram = shaderProgram;
        this.saveGenerator = saveGenerator;
        this.saveLoader = saveLoader;
        this.dataController = dataController;
        this.stateController = stateController;

        Screen.init(
            window, 
            shaderProgram, 
            this, 
            saveGenerator, 
            saveLoader, 
            dataController, 
            stateController
        );
        this.screens = new HashMap<>();
        this.currentScreen = null;

        window.addResizeCallback(() -> { handleWindowResize(); });

        this.initScreens();
    }

    /**
     * Handle Window Resize
     */
    private void handleWindowResize() {
        int newWidth = window.getWidth();
        int newHeight = window.getHeight();

        for(Screen screen : screens.values()) {
            if(screen != null) {
                screen.onWindowResize(newWidth, newHeight);
            }
        }
    }

    public void setKeyState(int key, boolean pressed) {
        if(key >= 0 && key < keyPressed.length) {
            keyPressed[key] = pressed;
        }
    }

    public void switchTo(SCREENS screenType) {
        if(screenType == null) {
            if(currentScreen != null) {
                currentScreen.setActive(false);
            }
            currentScreen = null;
            activeScreen = null;
        }

        Screen screen = screens.get(screenType);
        if(screen != null) {
            if(currentScreen != null) {
                currentScreen.setActive(false);
            }
            currentScreen = screen;
            activeScreen = screenType;
            currentScreen.setActive(true);
        } 
    }

    public Screen getCurrentScreen() {
        return currentScreen;
    }

    public boolean isScreenActive(SCREENS screenType) {
        Screen screen = screens.get(screenType);
        return screen != null && screen.isActive();
    }

    public String checkClick(int mouseX, int mouseY) {
        if (currentScreen != null) {
            return currentScreen.checkClick(mouseX, mouseY);
        }
        return null;
    }

    /**
     * Cursor
     */
    public void enableCursor() {
        glfwSetInputMode(
            window.getWindow(), 
            GLFW_CURSOR, 
            GLFW_CURSOR_NORMAL
        );
    }

    public void disableCursor() {
        glfwSetInputMode(
            window.getWindow(), 
            GLFW_CURSOR, 
            GLFW_CURSOR_DISABLED
        );
    }

    public boolean shouldCursorBeEnabled() {
        return activeScreen != null;
    }

    /**
     * Screen Handler
     */
    private ScreenInputHandler screenHandler = new ScreenInputHandler() {
        @Override
        public void handleKeyPress(int key, int action) {
            if(key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                switchTo(SCREENS.PAUSE);
            }
        }
        
        @Override
        public void handleAction(String action) {
            
        }
    };

    public ScreenInputHandler getCurrentInputHandler() {
        if(currentScreen != null) {
            return currentScreen;
        }
        return screenHandler;
    }

    /**
     * Init Screens
     */
    public void initScreens() {
        /* Title */
        mainScreen = new MainScreen();
        screens.put(SCREENS.MAIN, mainScreen);

        /* Pauase */
        pauseScreen = new PauseScreen();
        screens.put(SCREENS.PAUSE, pauseScreen);
    }

    /**
     * Title Screen
     */
    public MainScreen getMainScreen() {
        return mainScreen;
    }

    /**
     * Pause Screen
     */
    public PauseScreen getPauseScreen() {
        return pauseScreen;
    }

    /**
     * Render
     */
    public void render() {
        if(currentScreen != null) {
            currentScreen.render();
        }
    }
}
