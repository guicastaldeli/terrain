package main.com.app.root.player_controller;
import static org.lwjgl.glfw.GLFW.*;

public class PlayerInputMap {
    private final PlayerController playerController;

    private boolean[] keyPressed = new boolean[GLFW_KEY_LAST + 1];
    private boolean rightMousePressed = false;

    public PlayerInputMap(PlayerController playerController) {
        this.playerController = playerController;
    }

    public void setKeyState(int key, boolean pressed) {
        if(key >= 0 && key < keyPressed.length) {
            keyPressed[key] = pressed;
        }
    }

    /**
     * Handle Mouse
     */
    public void handleMouse(float xOffset, float yOffset) {
        if (playerController != null && playerController.getCamera() != null) {
            Camera camera = playerController.getCamera();
            if(rightMousePressed) {
                camera.getAimController().handleMouse(xOffset, yOffset);
            } else {
                camera.handleMouse(xOffset, yOffset);
            }
        }
    }

    public void setMouseButtonState(int button, boolean pressed) {
        if(button == GLFW_MOUSE_BUTTON_RIGHT) {
            rightMousePressed = pressed;
            playerController
                .getCamera()
                .getAimController()
                .setMode(pressed);

            if(pressed) {
                playerController.getCamera().setShowCursor(true);
            } else {
                playerController.getCamera().setShowCursor(false);
            }

            PlayerMesh playerMesh = playerController.getPlayerMesh();
            if(playerMesh != null) playerMesh.setAiming(pressed);
        }
    }

    public boolean isRightMousePressed() {
        return rightMousePressed;
    }

    /**
     * Keyboard Callback
     */
    public void keyboardCallback() {
        /* Forward */
        if(keyPressed[GLFW_KEY_W]) {
            playerController.updatePosition(PlayerController.MovDir.FORWARD);
        }
        /* backward */
        if(keyPressed[GLFW_KEY_S]) {
            playerController.updatePosition(PlayerController.MovDir.BACKWARD);
        }
        /* Left */
        if(keyPressed[GLFW_KEY_A]) {
            playerController.updatePosition(PlayerController.MovDir.LEFT);
        }
        /* Right */
        if(keyPressed[GLFW_KEY_D]) {
            playerController.updatePosition(PlayerController.MovDir.RIGHT);
        }
        /* Up */
        if(keyPressed[GLFW_KEY_SPACE]) {
            playerController.updatePosition(PlayerController.MovDir.UP);
        }
        /* Down */
        if(keyPressed[GLFW_KEY_LEFT_SHIFT]) {
            playerController.updatePosition(PlayerController.MovDir.DOWN);
        }
    }

    /**
     * Player Controller
     */
    public PlayerController getPlayerController() {
        return playerController;
    }
}
