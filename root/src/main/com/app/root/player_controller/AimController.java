package main.com.app.root.player_controller;
import main.com.app.root.player_controller.Camera;

public class AimController {
    private final Camera camera;

    public boolean mode = false;
    public float yaw = 0.0f;
    public float pitch = 0.0f;
    public float sensv = 0.15f;

    AimController(Camera camera) {
        this.camera = camera;    
    }

    public void setMode(boolean enabled) {
        this.mode = enabled;
        if(enabled) {
            this.yaw = camera.getYaw();
            this.pitch = camera.getPitch();
        }
    }
}
