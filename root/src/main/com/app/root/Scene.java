package main.com.app.root;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.player_controller.PlayerController;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.env.EnvCall;
import main.com.app.root.env.EnvController;
import main.com.app.root.env.EnvData;

public class Scene {
    private final Window window;
    private final Tick tick;
    private final DataController dataController;
    private final StateController stateController;

    private Mesh mesh;
    private ShaderProgram shaderProgram;
    private EnvController envController;
    private PlayerController playerController;
    private DependencyContainer dependencyContainer;

    public boolean init = false;

    public Scene(
        Window window, 
        Tick tick,
        DataController dataController,
        StateController stateController, 
        ShaderProgram shaderProgram
    ) {
        this.window = window;
        this.tick = tick;
        this.dataController = dataController;
        this.stateController = stateController;
        this.shaderProgram = shaderProgram;
    }

    public boolean isInit() {
        return init;
    }

    public EnvController getEnvController() {
        return envController;
    }

    public PlayerController getPlayerController() {
        return playerController;
    }

    /**
     * Setup
     */
    public void init() {
        if(!init) {
            System.out.println("------- Scene Started!!! -------");

            this.mesh = new Mesh(tick, shaderProgram);
            this.playerController = new PlayerController(
                tick, 
                window, 
                shaderProgram,
                mesh
            );
            mesh.setPlayerController(playerController);

            this.dependencyContainer = new DependencyContainer();
            dependencyContainer.registerAll(
                tick,
                shaderProgram,
                mesh,
                mesh.getMeshRenderer(),
                dataController,
                stateController
            );
    
            this.envController = new EnvController(dependencyContainer);
            start();
            
            this.init = true;
        }
    }

    /**
     * Start
     */
    private void start() {
        Object instance = envController.getEnv(EnvData.MAP).getInstance();
        EnvCall.call(instance, "getGenerator", "render");
    }

    /**
     * Update
     */
    public void update() {
        if(!init) return;
        playerController.update();
        mesh.update();
    }

    /**
     * Render
     */
    public void render() {
        if(!init) return;

        playerController.render();
        mesh.renderAll();
    }
}
