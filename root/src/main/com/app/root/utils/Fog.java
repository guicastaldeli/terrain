package main.com.app.root.utils;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.env.EnvController;
import main.com.app.root.player.Camera;
import main.com.app.root.player.PlayerController;
import org.joml.Vector3f;

public class Fog {
    public static void apply(
        Camera camera,
        PlayerController playerController,
        ShaderProgram shaderProgram,
        EnvController envController
    ) {
        float[] skyColor = SkyboxColor.get(envController);
        Vector3f fogColor = new Vector3f(skyColor[0], skyColor[1], skyColor[2]);

        Camera renderCamera;
        if(playerController != null) {
            renderCamera = playerController.getCamera();
        } else if(camera != null) {
            renderCamera = camera;
        } else {
            renderCamera = new Camera();
        }
        
        Vector3f cameraPos = renderCamera.getPosition();

        shaderProgram.setUniform("uRenderDistance", Camera.FOG);
        shaderProgram.setUniform("uFogColor", fogColor.x, fogColor.y, fogColor.z);
        shaderProgram.setUniform("uCameraPos", cameraPos.x, cameraPos.y, cameraPos.z);
        shaderProgram.setUniform("uFogDensity", 1.0f);
    }
}
