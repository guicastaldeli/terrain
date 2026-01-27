package main.com.app.root;
import main.com.app.root.player.PlayerInputMap;
import main.com.app.root.screen.ScreenController;
import main.com.app.root.screen.ScreenHandler;
import main.com.app.root.ui.UIController;

import static org.lwjgl.glfw.GLFW.*;

public class InputController {
    private final Window window;
    private PlayerInputMap playerInputMap;
    private ScreenController screenController;
    private UIController uiController;
    
    private boolean[] keyPressed = new boolean[GLFW_KEY_LAST + 1];
    private double lastMouseX = 0;
    private double lastMouseY = 0;
    private boolean firstMouse = true;

    public InputController(Window window) {
        this.window = window;
    }

    public void init(ScreenController screenController) {
        this.screenController = screenController;
        setupCallbacks();
    }

    public void setPlayerInputMap(PlayerInputMap playerInputMap) {
        this.playerInputMap = playerInputMap;
    }

    public void setUiController(UIController uiController) {
        this.uiController = uiController;
    } 

    private void setupCallbacks() {
        long windowHandle = window.getWindow();
        
        glfwSetKeyCallback(windowHandle, (w, key, scancode, action, mods) -> {
            if(key >= 0 && key < keyPressed.length) {
                keyPressed[key] = action != GLFW_RELEASE;
            }

            //uiController.handleKeyPress(key, action);
            if(uiController != null && uiController.handleKeyPress(key, action)) {
                updateCursorState();
                return;
            }
            
            /**
             * Screen Input
             */
            ScreenHandler inputHandler = screenController.getCurrentInputHandler();
            if(inputHandler != null) {
                inputHandler.handleKeyPress(key, action);
            }

            updateCursorState();
            
            /**
             * Player Input Map
             */
            if(playerInputMap != null) {
                playerInputMap.setKeyState(key, keyPressed[key]);
            }
        });

        glfwSetCursorPosCallback(windowHandle, (w, xPos, yPos) -> {
            if(uiController != null && uiController.isVisible()) {
                return;
            }

            boolean inAimMode = false;
            if(playerInputMap != null) inAimMode = playerInputMap.isRightMousePressed();

            if(!screenController.shouldCursorBeEnabled()) {
                if(firstMouse) {
                    lastMouseX = xPos;
                    lastMouseY = yPos;
                    firstMouse = false;
                }

                float xOffset = (float) (xPos - lastMouseX);
                float yOffset = (float) (lastMouseY - yPos);
                lastMouseX = xPos;
                lastMouseY = yPos;

                if(playerInputMap != null) {
                    playerInputMap.handleMouse(xOffset, yOffset);
                }
            } else {
                ScreenHandler inputHandler = screenController.getCurrentInputHandler();
                if(inputHandler != null) {
                    inputHandler.handleMouseMove((int)xPos, (int)yPos);
                }
            }

            lastMouseX = xPos;
            lastMouseY = yPos;
        });

        glfwSetMouseButtonCallback(windowHandle, (w, button, action, mods) -> {
            if(button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
                double[] xPos = new double[1];
                double[] yPos = new double[1];
                glfwGetCursorPos(w, xPos, yPos);
                
                if(uiController != null && uiController.handleMouseClick(xPos[0], yPos[0], button, action)) {
                    return;
                }
                
                if(screenController.shouldCursorBeEnabled()) {
                    String clickedAction = screenController.checkClick(
                        (int)xPos[0], 
                        (int)yPos[0]
                    );
                    if(clickedAction != null) {
                        screenController.getCurrentInputHandler().handleAction(clickedAction);
                        return;
                    }
                }
            }
            if(uiController == null || !uiController.isVisible()) {
                boolean pressed = (action == GLFW_PRESS || action == GLFW_REPEAT);
                if(playerInputMap != null) {
                    playerInputMap.setMouseButtonState(button, pressed);
                }
            }
            
            updateCursorState();
        });
    }

    private void updateCursorState() {
        boolean showCursor = false;
        boolean inAimMode = false;
        if(playerInputMap != null) {
            inAimMode = playerInputMap.isRightMousePressed();
            if(inAimMode) showCursor = true;
        }

        if(uiController != null && uiController.isVisible()) {
            glfwSetInputMode(
                window.getWindow(), 
                GLFW_CURSOR, 
                GLFW_CURSOR_NORMAL
            );
            return;
        }

        /**
         * 
         * System cursor showing its only a placeholder!!!!
         * 
         */
        if(screenController.shouldCursorBeEnabled()) {
            glfwSetInputMode(
                window.getWindow(), 
                GLFW_CURSOR, 
                GLFW_CURSOR_NORMAL
            );
        } else if(inAimMode) {
            glfwSetInputMode(
                window.getWindow(), 
                GLFW_CURSOR, 
                GLFW_CURSOR_NORMAL
            );
        } else {
            glfwSetInputMode(
                window.getWindow(), 
                GLFW_CURSOR, 
                GLFW_CURSOR_DISABLED
            );
            firstMouse = true;
        }
    }

    public void update() {
        if(!screenController.shouldCursorBeEnabled() && playerInputMap != null) {
            playerInputMap.keyboardCallback();
        }
    }
}