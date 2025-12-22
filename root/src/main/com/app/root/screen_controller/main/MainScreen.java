package main.com.app.root.screen_controller.main;
import main.com.app.root.DocParser;
import main.com.app.root._save.SaveInfo;
import main.com.app.root.screen_controller.Screen;
import java.util.*;

public class MainScreen extends Screen {
    private static final String SCREEN_PATH = DIR + "main/main_screen.xml";

    private MainScreenAction mainScreenAction;
    private boolean showSaveMenu = false;
    private List<SaveInfo> availableSaves;
    private SaveNameDialog saveNameDialog;

    public MainScreen() {
        super(SCREEN_PATH, "Main");
        this.mainScreenAction = new MainScreenAction(
            this,
            getScene(), 
            screenController,
            saveLoader,
            stateController,
            saveGenerator
        );
        this.saveNameDialog = new SaveNameDialog(mainScreenAction);
        refreshSaveList();
    }

    @Override
    public void handleAction(String action) {
        if(saveNameDialog.isActive()) {
            saveNameDialog.handleAction(action);
            return;
        }
        if(showSaveMenu) {
            handleSaveMenuAction(action);
        } else {
            handleMainMenuAction(action);
        }
    }

    @Override
    public void handleKeyPress(int ket, int action) {
        
    }

    /**
     * Main Menu Actions
     */
    private void handleMainMenuAction(String action) {
        switch (action) {
            case "contine":
                mainScreenAction.loadLastSave();
            case "start":
                saveNameDialog.show();
                break;
            case "load":
                showSaveMenu();
                break;
            case "settings":
                mainScreenAction.openSettings(); //Implement later
            case "exit":
                System.exit(0);
                break;
        }
    }

    /**
     * Save Menu
     */
    public void showSaveMenu() {
        showSaveMenu = true;
        refreshSaveList();
        renderSaveMenu();
    }

    private void handleSaveMenuAction(String action) {
        if(action.startsWith("load_")) {
            String saveId = action.substring(5);
            mainScreenAction.load(saveId);
        } else if(action.startsWith("delete_")) {
            String saveId = action.substring(7);
            mainScreenAction.deleteSave(saveId);
        } else if(action.equals("back")) {
            showSaveMenu = false;
            refreshScreen();
        }
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

    /**
     * Render Save Menu
     */
    public void renderSaveMenu() {
        
    }

    @Override 
    public void render() {
        if(showSaveMenu) {
            renderSaveMenu();
        } else {
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
