package main.com.app.root.player_controller;
import static org.lwjgl.glfw.GLFW.*;

public class PlayerInputMap {
    private final PlayerController playerController;
    private boolean[] keyPressed = new boolean[GLFW_KEY_LAST + 1];

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
            playerController.getCamera().handleMouse(xOffset, yOffset);
        }
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
    }
}
