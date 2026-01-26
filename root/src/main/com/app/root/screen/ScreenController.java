package main.com.app.root.screen;
import main.com.app.root.DataController;
import main.com.app.root.StateController;
import main.com.app.root.Tick;
import main.com.app.root.Window;
import main.com.app.root._save.DataGetter;
import main.com.app.root._save.SaveGenerator;
import main.com.app.root._save.SaveLoader;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root._text_renderer.TextRenderer;
import main.com.app.root.screen.main.MainScreen;
import main.com.app.root.screen.pause.PauseScreen;

import java.util.HashMap;
import java.util.Map;
import static org.lwjgl.glfw.GLFW.*;

public class ScreenController {
    public enum SCREENS {
        MAIN,
        PAUSE,
        SAVE_NAME_DIALOG,
        LOAD_SAVE_MENU
    }

    public final Window window;
    private final Tick tick;
    public final ShaderProgram shaderProgram;
    private final SaveGenerator saveGenerator;
    private final SaveLoader saveLoader;
    private final DataController dataController;
    private final DataGetter dataGetter;
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
        Tick tick,
        ShaderProgram shaderProgram,
        SaveGenerator saveGenerator,
        SaveLoader saveLoader,
        DataController dataController,
        DataGetter dataGetter,
        StateController stateController
    ) {
        this.window = window;
        this.tick = tick;
        this.shaderProgram = shaderProgram;
        this.saveGenerator = saveGenerator;
        this.saveLoader = saveLoader;
        this.dataController = dataController;
        this.dataGetter = dataGetter;
        this.stateController = stateController;

        Screen.init(
            window, 
            tick,
            shaderProgram, 
            this, 
            saveGenerator, 
            saveLoader, 
            dataController, 
            stateController,
            dataGetter
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
            for(Screen screen : screens.values()) {
                if(screen != null) {
                    screen.setActive(false);
                }
            }
            currentScreen = null;
            activeScreen = null;
        }

        Screen screen = screens.get(screenType);
        if(screen != null) {
            for(Screen s : screens.values()) {
                s.setActive(false);
            }
            currentScreen = screen;
            activeScreen = screenType;
            currentScreen.setActive(true);
        } 
    }

    public ScreenHandler getCurrentScreen() {
        if(currentScreen != null) return currentScreen;
        return screenHandler;
    }

    public boolean isScreenActive(SCREENS screenType) {
        Screen screen = screens.get(screenType);
        return screen != null && screen.isActive();
    }

    public String checkClick(int mouseX, int mouseY) {
        if(currentScreen != null) {
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
    private ScreenHandler screenHandler = new ScreenHandler() {
        @Override
        public void handleKeyPress(int key, int action) {
            if(key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                if(pauseScreen != null) {
                    pauseScreen.setActive(true);
                    pauseScreen.pauseScreenAction.togglePause();
                    switchTo(SCREENS.PAUSE);
                    enableCursor();
                    stateController.setPaused(true);
                }
            }
        }
    };

    public ScreenHandler getCurrentInputHandler() {
        if(currentScreen != null) {
            return currentScreen;
        }
        return screenHandler;
    }

    /**
     * Init Screens
     */
    public void initScreens() {
        /* Main */
        mainScreen = new MainScreen();
        screens.put(SCREENS.MAIN, mainScreen);

        /* Pauase */
        pauseScreen = new PauseScreen();
        screens.put(SCREENS.PAUSE, pauseScreen);

        /* Save Name Dialog */
        screens.put(SCREENS.SAVE_NAME_DIALOG, mainScreen.saveNameDialog);

        /* Load Save Menu */
        screens.put(SCREENS.LOAD_SAVE_MENU, mainScreen.loadSaveMenu);
    }

    /**
     * Main Screen
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
        if(currentScreen != null && currentScreen.isActive()) {
            currentScreen.render();
        }
    }
}
