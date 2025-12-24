package main.com.app.root.env;
import main.com.app.root.collision.CollisionManager;
import main.com.app.root.player_controller.PlayerController;
import org.joml.Vector3f;

public class EnvRenderer {
    private final EnvController envController;
    private final CollisionManager collisionManager;
    private final PlayerController playerController;
    
    public EnvRenderer(
        EnvController envController, 
        CollisionManager collisionManager,
        PlayerController playerController
    ) {
        this.envController = envController;
        this.collisionManager = collisionManager;
        this.playerController = playerController;
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

        /* Axe */
        Object axeInstance = envController.getEnv(EnvData.AXE).getInstance();
        EnvCall.call(axeInstance, "render");
    }
    
    /**
     * Update
     */
    public void update() {
        /* Skybox */
        Object skyboxInstance = envController.getEnv(EnvData.SKYBOX).getInstance();
        EnvCall.call(skyboxInstance, "getMesh", "update");

        Object axeInstance = envController.getEnv(EnvData.AXE).getInstance();
        Vector3f playerPos = playerController.getPosition();
        EnvCall.callWithParams(axeInstance, new Object[]{playerPos}, "setPosition");
    }
}