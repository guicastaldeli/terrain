package main.com.app.root.player_controller;
import main.com.app.root.Spawner;
import main.com.app.root.Tick;
import main.com.app.root.Upgrader;
import main.com.app.root.env.EnvController;
import main.com.app.root.env.tree.TreeInteractor;
import static org.lwjgl.glfw.GLFW.*;

public class PlayerInputMap {
    private final Tick tick;
    private final PlayerController playerController;
    private final Spawner spawner;
    private final Upgrader upgrader;
    private final EnvController envController;
    private final TreeInteractor treeInteractor;

    private boolean[] keyPressed = new boolean[GLFW_KEY_LAST + 1];
    private boolean fKeyPressed = false;
    private boolean rightMousePressed = false;
    private boolean leftMousePressed = false;
    private boolean leftMouseDown = false;

    public PlayerInputMap(
        Tick tick, 
        PlayerController playerController,
        Spawner spawner,
        Upgrader upgrader,
        EnvController envController
    ) {
        this.tick = tick;
        this.playerController = playerController;
        this.spawner = spawner;
        this.upgrader = upgrader;
        this.envController = envController;
        this.treeInteractor = new TreeInteractor(
            tick, 
            playerController, 
            spawner, 
            upgrader, 
            envController
        );
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
        /* Button Left */
        if(button == GLFW_MOUSE_BUTTON_LEFT) {
            leftMousePressed = pressed;
            leftMouseDown = pressed;
            if(pressed && treeInteractor != null) {
                treeInteractor.attemptBreak();
            }
        }
        /* Button Right */
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
        if(keyPressed[GLFW_KEY_F] && !fKeyPressed) {
            playerController.toggleFlyMode();
            fKeyPressed = true;
        } else if(!keyPressed[GLFW_KEY_F]) {
            fKeyPressed = false;
        }
        if(keyPressed[GLFW_KEY_P]) {
            //treeInteractor.debugCheckTrees();
        }

        playerController.updatePosition(PlayerController.MovDir.FORWARD, keyPressed[GLFW_KEY_W]);
        playerController.updatePosition(PlayerController.MovDir.BACKWARD, keyPressed[GLFW_KEY_S]);
        playerController.updatePosition(PlayerController.MovDir.LEFT, keyPressed[GLFW_KEY_A]);
        playerController.updatePosition(PlayerController.MovDir.RIGHT, keyPressed[GLFW_KEY_D]);
        
        if(playerController.isInFlyMode()) {
            playerController.updatePosition(PlayerController.MovDir.UP, keyPressed[GLFW_KEY_SPACE]);
            playerController.updatePosition(PlayerController.MovDir.DOWN, keyPressed[GLFW_KEY_LEFT_SHIFT]);
        } else {
            if(keyPressed[GLFW_KEY_SPACE]) {
                playerController.updatePosition(PlayerController.MovDir.UP, true);
                keyPressed[GLFW_KEY_SPACE] = false;
            }
            if(keyPressed[GLFW_KEY_LEFT_SHIFT]) {
                playerController.updatePosition(PlayerController.MovDir.DOWN, true);
            }
        }
    }

    /**
     * Get Tree Interactor
     */
    public TreeInteractor getTreeInteractor() {
        return treeInteractor;
    }

    /**
     * Player Controller
     */
    public PlayerController getPlayerController() {
        return playerController;
    }
}
