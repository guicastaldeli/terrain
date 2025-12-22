package main.com.app.root.screen_controller.title;
import main.com.app.root.DocParser;
import main.com.app.root._save.SaveInfo;
import main.com.app.root.screen_controller.Screen;
import java.util.*;

public class TitleScreen extends Screen {
    private static final String SCREEN_PATH = DIR + "title/title_screen.xml";

    private TitleScreenAction titleScreenAction;
    private boolean showSaveMenu = false;
    private List<SaveInfo> availableSaves;

    public TitleScreen() {
        super(SCREEN_PATH, "title");
        this.titleScreenAction = new TitleScreenAction(
            this,
            getScene(), 
            screenController,
            saveLoader,
            stateController
        );

        refreshSaveList();
    }

    @Override
    public void handleAction(String action) {
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
                //loadLastSave(); //Implement Later
            case "start":
                titleScreenAction.start(getScene());
                break;
            case "load":
                titleScreenAction.load();
                break;
            case "settings":
                titleScreenAction.openSettings(); //Implement later
            case "exit":
                System.exit(0);
                break;
        }
    }

    /**
     * Handle Save Menu Actions
     */
    private void handleSaveMenuAction(String action) {
        if(action.startsWith("load_")) {
            String saveId = action.substring(5);
            titleScreenAction.load();
        } else if(action.startsWith("delete_")) {
            String saveId = action.substring(7);
            titleScreenAction.deleteSave(saveId);
        } else if(action.equals("back")) {
            showSaveMenu = false;
            refreshScreen();
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
}
