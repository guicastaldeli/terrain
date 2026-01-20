package main.com.app.root.player;
import org.joml.Math;
import org.joml.Vector3f;

public class AimController {
    public boolean mode = false;
    public float yaw = 0.0f;
    public float pitch = 0.0f;
    public float sensv = 0.15f;

    public Vector3f rotation = new Vector3f(0.0f, 0.0f, 0.0f);
    public boolean isAiming = false;
    public float rotationSpeed = 5.0f;

    /**
     * Handle Mouse
     */
    public void handleMouse(float xOffset, float yOffset) {
        if(Math.abs(xOffset) > 0 || Math.abs(yOffset) > 0) {
            yaw += xOffset * sensv;
            pitch += yOffset * sensv;

            if(pitch > 89.0f) pitch = 89.0f;
            if(pitch < -89.0f) pitch = -89.0f;

            yaw = yaw % 360;
            if(yaw < 0) yaw += 360;

            rotation.x = yaw;
            rotation.y = pitch;
        }
    }
    
    /**
     * Set Mode
     */
    public void setMode(boolean enabled) {
        this.mode = enabled;
        if(enabled) {
            this.yaw = 0.0f;
            this.pitch = 0.0f;
            this.rotation.set(0.0f, 0.0f, 0.0f);
        }
    }

    public boolean isMode() {
        return mode;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }
}
