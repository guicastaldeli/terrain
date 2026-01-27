package main.com.app.root.screen.main;
import main.com.app.root.DocParser;
import main.com.app.root._save.SaveInfo;
import main.com.app.root.screen.Screen;
import main.com.app.root.screen.ScreenElement;
import main.com.app.root.screen.main.scene.MainScreenScene;
import java.util.*;

public class MainScreen extends Screen {
    private static final String SCREEN_PATH = DIR + "main/main_screen.xml";

    public MainScreenAction mainScreenAction;
    public List<SaveInfo> availableSaves;
    
    public SaveNameDialog saveNameDialog;
    public LoadSaveMenu loadSaveMenu;

    private MainScreenScene mainScreenScene;

    public MainScreen() {
        super(SCREEN_PATH, "main");
        this.mainScreenAction = new MainScreenAction(
            this,
            scene, 
            screenController,
            saveLoader,
            stateController,
            saveGenerator
        );
        this.loadSaveMenu = new LoadSaveMenu(this);
        this.saveNameDialog = new SaveNameDialog(mainScreenAction);
        refreshSaveList();
        initScene();
    }

    @Override
    public void handleAction(String action) {
        if(saveNameDialog.isActive()) {
            saveNameDialog.handleAction(action);
            return;
        }
        if(loadSaveMenu.isActive()) {
           loadSaveMenu.handleAction(action);
        } else {
            handleMainMenuAction(action);
        }
    }

    @Override
    public void handleKeyPress(int key, int action) {
        if(saveNameDialog.isActive()) {
            saveNameDialog.handleKeyPress(key, action);
            return;
        }
    }

    /**
     * Main Menu Actions
     */
    private void handleMainMenuAction(String action) {
        switch (action) {
            case "continue":
                mainScreenAction.loadLastSave();
                break;
            case "start":
                saveNameDialog.show();
                break;
            case "load":
                showSaveMenu();
                break;
            case "settings":
                mainScreenAction.openSettings(); //Implement later
                break;
            case "exit":
                System.exit(0);
                break;
        }
    }

    /**
     * Save Menu
     */
    public void showSaveMenu() {
        refreshSaveList();
        loadSaveMenu.show();
        this.active = false;
        resetHover();
    }

    /**
     * Refresh Save List
     */
    public void refreshSaveList() {
        availableSaves = saveLoader.listAvailableSaves();
    }

    @Override 
    public void render() {
        renderScene();
        if(saveNameDialog.isActive()) {
            saveNameDialog.render();
        } else if(loadSaveMenu.isActive()) {
            loadSaveMenu.render();
        } else {
            super.render();
        }
    }

    @Override
    public void update() {
        mainScreenScene.update();
        if(lastMouseX >= 0 && lastMouseY >= 0) {
           handleMouseMove(lastMouseX, lastMouseY);
           System.out.println(lastMouseX);
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
            if(loadSaveMenu.isActive()) {
                loadSaveMenu.onWindowResize(width, height);
            } else if(saveNameDialog.isActive()) {
                saveNameDialog.onWindowResize(width, height);
            } else {
                this.screenData = DocParser.parseScreen(
                    SCREEN_PATH,
                    width,
                    height
                );
            }
        } catch(Exception err) {
            System.err.println("Failed to re-parse screen on resize: " + err.getMessage());
        }
    }

    /**
     * 
     * Scene
     * 
     */
    public void initScene() {
        mainScreenScene = new MainScreenScene(
            window, 
            tick, 
            shaderProgram
        );
        mainScreenScene.init();
    }

    public void renderScene() {
        mainScreenScene.render();
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

    public void resetHover() {
        if(screenData == null) return;
        for(ScreenElement element : screenData.elements) {
            if(element.hoverable && element.isHovered) {
                element.removeHover();
            }
        }
    }
}
