package main.com.app.root.screen_controller.pause;
import main.com.app.root.Console;
import main.com.app.root.screen_controller.ScreenController;
import main.com.app.root.screen_controller.ScreenController.SCREENS;

public class PauseScreenAction {
    private final ScreenController screenController;
    private final PauseScreen pauseScreen;

    public PauseScreenAction(ScreenController screenController, PauseScreen pauseScreen) {
        this.screenController = screenController;
        this.pauseScreen = pauseScreen;
    }

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
}
