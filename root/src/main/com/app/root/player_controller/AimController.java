package main.com.app.root.player_controller;
import org.joml.Vector3f;

import main.com.app.root.player_controller.Camera;

public class AimController {
    private final Camera camera;

    public boolean mode = false;
    public float yaw = 0.0f;
    public float pitch = 0.0f;
    public float sensv = 0.15f;

    public Vector3f rotation = new Vector3f(0.0f, 0.0f, 0.0f);
    public boolean isAiming = false;
    public float rotationSpeed = 5.0f;

    AimController(Camera camera) {
        this.camera = camera;    
    }

    /**
     * Handle Mouse
     */
    public void handleMouse(float xOffset, float yOffset) {
        yaw += xOffset * sensv;
        pitch += yOffset * sensv;

        if(pitch > 89.0f) pitch = 89.0f;
        if(pitch < -89.0f) pitch = -89.0f;
        yaw = yaw % 360;
        if(yaw < 0) yaw += 360;
    }
    
    /**
     * Set Mode
     */
    public void setMode(boolean enabled) {
        this.mode = enabled;
        if(enabled) {
            this.yaw = camera.getYaw();
            this.pitch = camera.getPitch();
        }
    }

    public boolean isMode() {
        return mode;
    }

    public float getYaw() {
        return yaw;
    }
}
