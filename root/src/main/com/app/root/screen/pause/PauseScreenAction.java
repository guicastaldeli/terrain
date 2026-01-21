package main.com.app.root.screen.pause;
import main.com.app.root.Console;
import main.com.app.root.StateController;
import main.com.app.root._save.DataGetter;
import main.com.app.root._save.SaveGenerator;
import main.com.app.root.screen.Screen;
import main.com.app.root.screen.ScreenController;
import main.com.app.root.screen.ScreenController.SCREENS;

public class PauseScreenAction {
    private final ScreenController screenController;
    private final PauseScreen pauseScreen;
    private final StateController stateController;
    private final SaveGenerator saveGenerator;
    private final DataGetter dataGetter;

    public PauseScreenAction(
        ScreenController screenController, 
        PauseScreen pauseScreen,
        StateController stateController,
        SaveGenerator saveGenerator,
        DataGetter dataGetter
    ) {
        this.screenController = screenController;
        this.pauseScreen = pauseScreen;
        this.stateController = stateController;
        this.saveGenerator = saveGenerator;
        this.dataGetter = dataGetter;
    }

    /**
     * Toggle Pause
     */
    public void togglePause() {
        boolean isCurrentlyActive = pauseScreen.isActive();
        pauseScreen.setActive(!isCurrentlyActive);
        
        if(!isCurrentlyActive) {
            Console.getInstance().pause();
            pauseScreen.setActive(true);
            screenController.switchTo(SCREENS.PAUSE);
            screenController.enableCursor();
        } else {
            Console.getInstance().resume();
            pauseScreen.setActive(false);
            screenController.switchTo(null);
            screenController.disableCursor();
        }
    }

    /**
     * Save
     */
    public void save() {
        if(dataGetter.playerController == null && Screen.getScene() != null) {
            dataGetter.setPlayerController(Screen.getScene().getPlayerController());
        }
        String currentSaveId = stateController.getCurrentSaveId();
        if(currentSaveId != null) {
            try {
                saveGenerator.save(currentSaveId);
                System.out.println("Game saved successfully!");
            } catch(Exception err) {
                System.err.println("Failed to save game: " + err.getMessage());
                err.printStackTrace();
            }
        } else {
            System.err.println("No active save to save to!");
        }
    }

    /**
     * Open Settings
     */
    public void openSettings() {
        /////
    }

    /**
     * Exit to Menu
     */
    public void exitToMenu() {
        save();
        
        pauseScreen.setActive(false);
        
        screenController.switchTo(ScreenController.SCREENS.MAIN);
        screenController.enableCursor();
        
        stateController.setInMenu(true);
        stateController.setPaused(false);
        
        if(Screen.scene != null) {
            Screen.scene.init = false;
        }
        Console.getInstance().switchToMainScreen();
    }
}
