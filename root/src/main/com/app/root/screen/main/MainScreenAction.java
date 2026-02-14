package main.com.app.root.screen.main;
import main.com.app.root.Scene;
import main.com.app.root.StateController;
import main.com.app.root._resources.AudioLoader;
import main.com.app.root._save.SaveGenerator;
import main.com.app.root._save.SaveInfo;
import main.com.app.root._save.SaveLoader;
import main.com.app.root.screen.ScreenController;
import main.com.app.root.screen.main.scene.MainScreenScene;

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
            AudioLoader.getInstance().stop(MainScreenScene.ENV_MENU_AUDIO);
            scene.init(true);
            if(scene.getDataGetter() != null) {
                scene.getDataGetter().setEnvController(scene.getEnvController());
                scene.getDataGetter().setUpgrader(scene.getUpgrader());
                scene.getDataGetter().setSpawner(scene.getSpawner());
                scene.getDataGetter().setPlayerController(scene.getPlayerController());
            }
            if(saveGenerator.generateNewSave(saveName) != null) {
                mainScreen.loadSaveMenu.hide();
                mainScreen.saveNameDialog.hide();
                mainScreen.setActive(false);
    
                screenController.switchTo(null);
                screenController.disableCursor();
                    
                stateController.setInMenu(false);
                stateController.setPaused(false);
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
        AudioLoader.getInstance().stop(MainScreenScene.ENV_MENU_AUDIO);
        AudioLoader.getInstance().play("main/select_world.wav", 0.1f);
        scene.init(false);
        if(scene.getDataGetter() != null) {
            scene.getDataGetter().setEnvController(scene.getEnvController());
            scene.getDataGetter().setUpgrader(scene.getUpgrader());
            scene.getDataGetter().setSpawner(scene.getSpawner());
            scene.getDataGetter().setPlayerController(scene.getPlayerController());
        }
        if(saveLoader.load(saveId)) {
            mainScreen.loadSaveMenu.hide();
            mainScreen.saveNameDialog.hide();
            mainScreen.setActive(false);
            
            screenController.switchTo(null);
            screenController.disableCursor();

            stateController.setInMenu(false);
            stateController.setPaused(false);
        }
    }

    public void loadLastSave() {
        List<SaveInfo> saves = saveLoader.listAvailableSaves();
        AudioLoader.getInstance().stop(MainScreenScene.ENV_MENU_AUDIO);
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
        System.exit(0);
    }
}
