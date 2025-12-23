package main.com.app.root.env;

public class EnvRenderer {
    private EnvController envController;
    
    public EnvRenderer(EnvController envController) {
        this.envController = envController;
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
