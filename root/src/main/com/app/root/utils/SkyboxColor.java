package main.com.app.root.utils;
import main.com.app.root.env.EnvCall;
import main.com.app.root.env.EnvController;
import main.com.app.root.env.EnvData;

public class SkyboxColor {
    public static float[] get(EnvController envController) {
        if(envController == null) return new float[]{1.0f, 1.0f, 1.0f, 1.0f};
        
        try {
            Object skyboxInstance = envController.getEnv(EnvData.SKYBOX);
            Object skyboxMesh = EnvCall.callReturn(skyboxInstance, "getMesh");
            
            if(skyboxMesh != null) {
                Object colorObj = EnvCall.callReturn(skyboxMesh, "getCurrentSkyColor");
                if(colorObj instanceof float[]) return (float[]) colorObj;
            }
        } catch(Exception e) {
            System.err.println("Failed to get skybox color: " + e.getMessage());
        }
        
        return new float[]{0.5f, 0.5f, 0.5f, 1.0f};
    }
}
