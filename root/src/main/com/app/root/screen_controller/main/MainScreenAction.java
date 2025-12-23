package main.com.app.root.screen_controller.main;
import main.com.app.root.Scene;
import main.com.app.root.StateController;
import main.com.app.root._save.SaveGenerator;
import main.com.app.root._save.SaveInfo;
import main.com.app.root._save.SaveLoader;
import main.com.app.root.screen_controller.ScreenController;
import java.io.IOException;
import java.util.List;

public class MainScreenAction {
    private final MainScreen mainScreen;
    private final Scene scene;
    private final ScreenController screenController;
    private final SaveLoader saveLoader;
    private final StateController stateController;
    private final SaveGenerator saveGenerator;

    public MainScreenAction(
        MainScreen mainScreen,
        Scene scene,
        ScreenController screenController,
        SaveLoader saveLoader,
        StateController stateController,
        SaveGenerator saveGenerator
    ) {
        this.mainScreen = mainScreen;
        this.scene = scene;
        this.screenController = screenController;
        this.saveLoader = saveLoader;
        this.stateController = stateController;
        this.saveGenerator = saveGenerator;
    }

    /**
     * Start
     */
    public void start(String saveName) {
        try {
            String saveId = saveGenerator.generateNewSave(saveName);
            if(saveLoader.loadSave(saveId)) {
                mainScreen.loadSaveMenu.hide();
                mainScreen.saveNameDialog.hide();
                mainScreen.setActive(false);

                screenController.switchTo(null);
                screenController.disableCursor();
                
                stateController.setInMenu(false);
                stateController.setPaused(false);

                scene.init();
                scene.init = true;
            }
        } catch(IOException err) {
            System.err.println("Failed to create new game: " + err.getMessage());
            err.printStackTrace();
        }
    }

    /**
     * Load
     */
    public void load(String saveId) {
        if(saveLoader.loadSave(saveId)) {
            mainScreen.loadSaveMenu.hide();
            mainScreen.saveNameDialog.hide();
            mainScreen.setActive(false);
            
            screenController.switchTo(null);
            screenController.disableCursor();

            stateController.setInMenu(false);
            stateController.setPaused(false);

            if(!scene.init) scene.init();
            scene.init = true;
        }
    }

    public void loadLastSave() {
        List<SaveInfo> saves = saveLoader.listAvailableSaves();
        if(!saves.isEmpty()) {
            String saveId = saves.get(0).saveId;
            load(saveId);
        } else {
            System.err.println("No saves found to load");
        }
    }

    public void openLoadMenu() {
        mainScreen.showSaveMenu();
    }

    /**
     * Open Settings
     */
    public void openSettings() {
        /////
    }

    /**
     * Delete Save
     */
    public void deleteSave(String saveId) {
        if(saveLoader.deleteSave(saveId)) {
            mainScreen.refreshSaveList();
            mainScreen.loadSaveMenu.render();
        }
    }

    /**
     * Exit
     */
    public void exit() {
        ////////
    }
}
