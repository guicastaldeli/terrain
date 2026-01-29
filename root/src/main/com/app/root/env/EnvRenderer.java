package main.com.app.root.env;
import main.com.app.root.DataController;
import main.com.app.root.collision.CollisionManager;
import main.com.app.root.player.PlayerController;
import org.joml.Vector3f;

public class EnvRenderer {
    private final EnvController envController;
    private final CollisionManager collisionManager;
    private final PlayerController playerController;
    private final DataController dataController;
    
    private final Object skyboxInstance;
    private final Object cloudInstance;
    private final Object worldInstance;
    private final Object axeInstance;
    
    public EnvRenderer(
        EnvController envController, 
        CollisionManager collisionManager,
        PlayerController playerController,
        DataController dataController
    ) {
        this.envController = envController;
        this.collisionManager = collisionManager;
        this.playerController = playerController;
        this.dataController = dataController;
        
        this.skyboxInstance = envController.getEnv(EnvData.SKYBOX).getInstance();
        this.cloudInstance = envController.getEnv(EnvData.CLOUD).getInstance();
        this.worldInstance = envController.getEnv(EnvData.MAP).getInstance();
        this.axeInstance = envController.getEnv(EnvData.AXE).getInstance();
    }

    /**
     * Render
     */
    public void render() {
        /* Skybox */
        EnvCall.call(skyboxInstance, "getMesh", "render");

        /* Map */
        Vector3f pos = playerController.getPosition();
        Object[] playerPosParams = { pos.x, pos.z };
        Object generator = EnvCall.callReturn(worldInstance, "getGenerator");
        if(generator != null) {
            EnvCall.callWithParams(generator, playerPosParams, "render");
        }

        /* Axe */
        EnvCall.call(axeInstance, "render");

        /* Clouds */
        EnvCall.call(cloudInstance, "render");
        if(cloudInstance != null) EnvCall.callWithParams(cloudInstance, new Object[]{ dataController.getWorldSeed() }, "setSeed");
    }
    
    /**
     * Update
     */
    public void update() {
        /* Skybox */
        EnvCall.call(skyboxInstance, "getMesh", "update");
    }
}