package main.com.app.root.screen_controller.pause;
import main.com.app.root.Console;
import main.com.app.root.StateController;
import main.com.app.root._save.SaveGenerator;
import main.com.app.root.screen_controller.ScreenController;
import main.com.app.root.screen_controller.ScreenController.SCREENS;

public class PauseScreenAction {
    private final ScreenController screenController;
    private final PauseScreen pauseScreen;
    private final StateController stateController;
    private final SaveGenerator saveGenerator;

    public PauseScreenAction(
        ScreenController screenController, 
        PauseScreen pauseScreen,
        StateController stateController,
        SaveGenerator saveGenerator
    ) {
        this.screenController = screenController;
        this.pauseScreen = pauseScreen;
        this.stateController = stateController;
        this.saveGenerator = saveGenerator;
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
        String currentSaveId = stateController.getCurrentSaveId();
        if(currentSaveId != null) {
            try {
                saveGenerator.save(currentSaveId);
                System.out.println("Game saved successfully!");
            } catch (Exception err) {
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
        togglePause();
        save();
        screenController.switchTo(ScreenController.SCREENS.MAIN);
        stateController.setInMenu(true);
    }
}
