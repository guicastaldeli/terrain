package main.com.app.root;
import main.com.app.root.player_controller.PlayerInputMap;
import main.com.app.root.screen_controller.ScreenController;
import static org.lwjgl.glfw.GLFW.*;

public class InputController {
    private final Window window;
    private PlayerInputMap playerInputMap;
    private ScreenController screenController;
    
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

    private void setupCallbacks() {
        long windowHandle = window.getWindow();
        
        glfwSetKeyCallback(windowHandle, (w, key, scancode, action, mods) -> {
            if(key >= 0 && key < keyPressed.length) {
                keyPressed[key] = action != GLFW_RELEASE;
            }
            
            /**
             * Screen Input
             */
            screenController.getCurrentInputHandler().handleKeyPress(key, action);

            updateCursorState();
            
            /**
             * Player Input Map
             */
            if(playerInputMap != null) {
                playerInputMap.setKeyState(key, keyPressed[key]);
            }
        });

        glfwSetCursorPosCallback(windowHandle, (w, xPos, yPos) -> {
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
            }
        });

        glfwSetMouseButtonCallback(windowHandle, (w, button, action, mods) -> {
            /* Right Button */
            if(button == GLFW_MOUSE_BUTTON_RIGHT) {
                boolean pressed = (action == GLFW_PRESS || action == GLFW_REPEAT);
                if(playerInputMap != null) playerInputMap.setMouseButtonState(button, pressed);
                if(pressed) return;
            }
            /* Left Button */
            if(button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
                double[] xPos = new double[1];
                double[] yPos = new double[1];
                glfwGetCursorPos(w, xPos, yPos);
                
                String clickedAction = screenController.checkClick(
                    (int)xPos[0], 
                    (int)yPos[0]
                );
                if(clickedAction != null) {
                    screenController.getCurrentInputHandler().handleAction(clickedAction);
                }
            }
        });

        updateCursorState();
    }

    private void updateCursorState() {
        boolean showCursor = false;
        boolean inAimMode = false;
        if(playerInputMap != null) {
            inAimMode = playerInputMap.isRightMousePressed();
            if(inAimMode) showCursor = true;
        }

        if(screenController.shouldCursorBeEnabled() || inAimMode) {
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