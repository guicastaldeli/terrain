package main.com.app.root.screen.main;
import main.com.app.root.DocParser;
import main.com.app.root.KeyboardInputHandler;
import main.com.app.root.screen.Screen;
import main.com.app.root.screen.ScreenController;
import main.com.app.root.screen.ScreenElement;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

public class SaveNameDialog extends Screen {
    public static final String DIALOG_PATH = DIR + "main/save_name_dialog.xml";
    
    private final MainScreenAction mainScreenAction;
    private KeyboardInputHandler keyboardInputHandler;
    
    private long lastCursorTime = 0;
    private boolean cursorVisible = false;
    private static final long CURSOR_BLINK_INTERVAL = 500;
    
    public SaveNameDialog(MainScreenAction mainScreenAction) {
        super(DIALOG_PATH, "save_name_dialog");
        this.mainScreenAction = mainScreenAction;
        this.keyboardInputHandler = new KeyboardInputHandler(15);
    }

    /**
     * Confirm Save Name
     */
    private void confirmName() {
        String saveName = keyboardInputHandler.getText().trim();
        if(!saveName.isEmpty()) mainScreenAction.start(saveName);
        hide();
    }

    /**
     * Show
     */
    public void show() {
        setActive(true);
        active = true;
        keyboardInputHandler.clear();

        try {
            this.screenData = DocParser.parseScreen(
                DIALOG_PATH,
                window.getWidth(),
                window.getHeight()
            );
            updateNameDisplay();
            screenController.switchTo(ScreenController.SCREENS.SAVE_NAME_DIALOG);
        } catch (Exception e) {
            System.err.println("Failed to parse save name dialog: " + e.getMessage());
        }
    }

    public boolean isActive() {
        return active;
    }

    public void clearEl() {
        screenData.elements.clear();
    }

    /**
     * Hide
     */
    public void hide() {
        setActive(false);
        active = false;
        keyboardInputHandler.clear();
        clearEl();
    }

    /**
     * Cancel
     */
    private void cancel() {
        hide();
        screenController.switchTo(ScreenController.SCREENS.MAIN);
    }

    /**
     * Update Name Display
     */
    private void updateNameDisplay() {
        ScreenElement nameDisplay = DocParser.getElementById(screenData, "nameDisplay");
        if(nameDisplay != null) {
            String currentText = keyboardInputHandler.getText();
            if(currentText.isEmpty() && nameDisplay.attr.containsKey("placeholder")) {
                nameDisplay.text = nameDisplay.attr.get("placeholder");
            } else {
                if(active && cursorVisible) {
                    nameDisplay.text = currentText + "|";
                } else {
                    nameDisplay.text = currentText;
                }
            }
        }
    }

    /**
     * Update Cursor
     */
    private void updateCursor() {
        long currentTime = System.currentTimeMillis();
        if(currentTime - lastCursorTime > CURSOR_BLINK_INTERVAL) {
            cursorVisible = !cursorVisible;
            lastCursorTime = currentTime;
            updateNameDisplay();
        }
    }

    @Override
    public void setActive(boolean active) {
        super.setActive(active);
        if(!active) {
            keyboardInputHandler.clear();
        }
    }

    @Override
    public void handleAction(String action) {
        if(!active) return;
        switch(action) {
            case "confirm":
                confirmName();
                break;
            case "cancel":
                cancel();
                break;
            case "clear":
                keyboardInputHandler.clear();
                updateNameDisplay();
                break;
        }
    }

    @Override
    public void handleKeyPress(int key, int action) {
        if(!active) return;

        boolean textChanged = keyboardInputHandler.handleKey(key, action);
        if(action == GLFW_PRESS) {
            if(key == GLFW_KEY_ENTER) {
                confirmName();
                return;
            }
            if(key == GLFW_KEY_ESCAPE) {
                cancel();
                return;
            }
        }
        if(textChanged) {
            updateNameDisplay();
        }
    }

    @Override
    public void render() {
        if(active) {
            super.render();
        }
    }

    @Override
    public void onWindowResize(int width, int height) {
        if(getTextRenderer() != null) {
            getTextRenderer().updateScreenSize(width, height);
        }
        
        try {
            this.screenData = DocParser.parseScreen(
                DIALOG_PATH,
                width,
                height
            );
            updateNameDisplay();
        } catch (Exception err) {
            System.err.println("Failed to re-parse save menu on resize: " + err.getMessage());
        }
    }
}
