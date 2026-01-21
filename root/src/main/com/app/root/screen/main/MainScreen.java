package main.com.app.root.screen.main;
import main.com.app.root.DocParser;
import main.com.app.root._save.SaveInfo;
import main.com.app.root.screen.Screen;

import java.util.*;

public class MainScreen extends Screen {
    private static final String SCREEN_PATH = DIR + "main/main_screen.xml";

    public MainScreenAction mainScreenAction;
    public List<SaveInfo> availableSaves;
    
    public SaveNameDialog saveNameDialog;
    public LoadSaveMenu loadSaveMenu;

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
        loadSaveMenu.show();
        refreshSaveList();
        this.active = false;
    }

    /**
     * Refresh Save List
     */
    public void refreshSaveList() {
        availableSaves = saveLoader.listAvailableSaves();
    }

    /**
     * Refresh Screem
     */
    public void refreshScreen() {
        
    }

    @Override 
    public void render() {
        if(saveNameDialog.isActive()) {
            saveNameDialog.render();
        } else if(loadSaveMenu.isActive()) {
            loadSaveMenu.render();
        } else {
            //DocParser.renderScreen(screenData, 1280, 720, shaderProgram, textRenderer);
            super.render();
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
}
