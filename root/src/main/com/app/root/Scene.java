package main.com.app.root;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.player_controller.PlayerController;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.collision.CollisionManager;
import main.com.app.root.env.EnvController;
import main.com.app.root.env.EnvRenderer;

public class Scene {
    private final Window window;
    private final Tick tick;
    private final DataController dataController;
    private final StateController stateController;

    private Mesh mesh;
    private ShaderProgram shaderProgram;
    private PlayerController playerController;
    private EnvController envController;
    private EnvRenderer envRenderer;
    private DependencyContainer dependencyContainer;
    private CollisionManager collisionManager;

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

            this.collisionManager = new CollisionManager();

            this.mesh = new Mesh(tick, shaderProgram);
            this.playerController = new PlayerController(
                tick, 
                window,
                mesh,
                collisionManager
            );
            mesh.setPlayerController(playerController);

            this.dependencyContainer = new DependencyContainer();
            dependencyContainer.registerAll(
                tick,
                shaderProgram,
                mesh,
                mesh.getMeshRenderer(),
                dataController,
                stateController,
                collisionManager
            );
    
            this.envController = new EnvController(dependencyContainer);
            this.envRenderer = new EnvRenderer(envController, collisionManager);
            start();
            
            this.init = true;
        }
    }

    /**
     * Start
     */
    private void start() {
        envRenderer.render();
    }

    /**
     * Update
     */
    public void update() {
        if(!init) return;
        playerController.update();
        collisionManager.updateDynamicColliders(tick.getDeltaTime());
        mesh.update();
        envRenderer.update();
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
