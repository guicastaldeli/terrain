package main.com.app.root.env;
import main.com.app.root.collision.CollisionManager;
import main.com.app.root.player_controller.PlayerController;
import org.joml.Vector3f;

public class EnvRenderer {
    private final EnvController envController;
    private final CollisionManager collisionManager;
    private final PlayerController playerController;
    
    private final Object skyboxInstance;
    private final Object mapInstance;
    private final Object axeInstance;
    
    public EnvRenderer(
        EnvController envController, 
        CollisionManager collisionManager,
        PlayerController playerController
    ) {
        this.envController = envController;
        this.collisionManager = collisionManager;
        this.playerController = playerController;
        
        this.skyboxInstance = envController.getEnv(EnvData.SKYBOX).getInstance();
        this.mapInstance = envController.getEnv(EnvData.MAP).getInstance();
        this.axeInstance = envController.getEnv(EnvData.AXE).getInstance();
    }

    /**
     * Render
     */
    public void render() {
        /* Skybox */
        EnvCall.call(skyboxInstance, "getMesh", "render");

        /* Map */
        EnvCall.call(mapInstance, "getGenerator", "render");

        /* Axe */
        EnvCall.call(axeInstance, "render");
    }
    
    /**
     * Update
     */
    public void update() {
        /* Skybox */
        EnvCall.call(skyboxInstance, "getMesh", "update");

        /* Axe */
        Vector3f playerPos = playerController.getPosition();
        EnvCall.callWithParams(axeInstance, new Object[]{playerPos}, "setPosition");
    }
}