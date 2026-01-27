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
        mainScreenAction.start(saveName);
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
        } catch(Exception e) {
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
            
            if(currentText.isEmpty()) {
                nameDisplay.text = nameDisplay.attr.get("placeholder");
                nameDisplay.color = parseColor(nameDisplay.attr.get("color"));
            } else {
                if(active && cursorVisible) {
                    nameDisplay.text = currentText + "|";
                } else {
                    nameDisplay.text = currentText;
                }

                String activeColorStr = nameDisplay.attr.get("activeColor");
                if(activeColorStr != null) {
                    nameDisplay.color = parseColor(activeColorStr);
                } else {
                    nameDisplay.color = new float[]{0.0f, 0.0f, 0.0f, 1.0f};
                }
            }
        }
    }

    private float[] parseColor(String colorStr) {
        if(colorStr == null || colorStr.isEmpty()) {
            return new float[]{0.0f, 0.0f, 0.0f, 1.0f};
        }
        
        String[] components = colorStr.split(",");
        float[] color = new float[4];
        for(int i = 0; i < Math.min(components.length, 4); i++) {
            color[i] = Float.parseFloat(components[i].trim());
        }
        
        if(components.length < 4) color[3] = 1.0f;
        
        return color;
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
        } catch(Exception err) {
            System.err.println("Failed to re-parse save menu on resize: " + err.getMessage());
        }
    }

    @Override
    public void update() {
        if(active) {
            updateCursor();
        }
        if(lastMouseX >= 0 && lastMouseY >= 0) {
            handleMouseMove(lastMouseX, lastMouseY);
            System.out.println(lastMouseX);
        }
    }

    @Override
    public void handleMouseMove(int mouseX, int mouseY) {
        if(!active) return;
        
        for(ScreenElement element : screenData.elements) {
            if(element.visible && element.hoverable) {
                boolean wasHovered = element.isHovered;
                boolean isHovered = element.containsPoint(mouseX, mouseY);
                
                if(isHovered && !wasHovered) {
                    element.applyHover();
                } else if(!isHovered && wasHovered) {
                    element.removeHover();
                }
            }
        }
    }
}
