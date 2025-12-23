package main.com.app.root.env;
import main.com.app.root.collision.CollisionManager;

public class EnvRenderer {
    private EnvController envController;
    private CollisionManager collisionManager;
    
    public EnvRenderer(EnvController envController, CollisionManager collisionManager) {
        this.envController = envController;
        this.collisionManager = collisionManager;
    }

    /**
     * Render
     */
    public void render() {
        /* Skybox */
        Object skyboxInstance = envController.getEnv(EnvData.SKYBOX).getInstance();
        EnvCall.call(skyboxInstance, "getMesh", "render");

        /* Map */
        Object mapInstance = envController.getEnv(EnvData.MAP).getInstance();
        EnvCall.call(mapInstance, "getGenerator", "render");
    }
    
    /**
     * Update
     */
    public void update() {
        /* Skybox */
        Object skyboxInstance = envController.getEnv(EnvData.SKYBOX).getInstance();
        EnvCall.call(skyboxInstance, "getMesh", "update");
    }
}
