package main.com.app.root.screen_controller.title;
import main.com.app.root.Scene;
import main.com.app.root.StateController;
import main.com.app.root._save.SaveLoader;
import main.com.app.root.screen_controller.ScreenController;

public class TitleScreenAction {
    private TitleScreen titleScreen;
    private Scene scene;
    private ScreenController screenController;
    private SaveLoader saveLoader;
    private StateController stateController;

    public TitleScreenAction(
        TitleScreen titleScreen,
        Scene scene,
        ScreenController screenController,
        SaveLoader saveLoader,
        StateController stateController
    ) {
        this.titleScreen = titleScreen;
        this.scene = scene;
        this.screenController = screenController;
        this.saveLoader = saveLoader;
        this.stateController = stateController;
    }

    /**
     * Start
     */
    public void start() {
        scene.init = true;

        screenController.switchTo(null);
        screenController.disableCursor();
        scene.init();
    }

    /**
     * Load
     */
    public void load(String saveId) {
        if(saveLoader.loadSave(saveId)) {
            screenController.switchTo(null);
            screenController.disableCursor();
            stateController.setInMenu(false);
        }
    }

    /**
     * Delete Save
     */
    public void deleteSave(String saveId) {
        if(saveLoader.deleteSave(saveId)) {
            titleScreen.refreshSaveList();
            titleScreen.renderSaveMenu();
        }
    }
}
