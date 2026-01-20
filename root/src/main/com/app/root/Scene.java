package main.com.app.root;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.player.PlayerController;
import main.com.app.root.ui.UIController;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.collision.CollisionManager;
import main.com.app.root.env.EnvController;
import main.com.app.root.env.EnvRenderer;
import org.joml.Vector3f;

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
    private UIController uiController;
    private InputController inputController;

    private Spawner spawner;
    private Upgrader upgrader;

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

    public void setInputController(InputController inputController) {
        this.inputController = inputController;
    } 

    public UIController getUIController() {
        return uiController;
    }

    public Upgrader getUpgrader() {
        return upgrader;
    }

    /**
     * Setup
     */
    public void init() {
        if(!init) {
            System.out.println("------- Scene Started!!! -------");

            this.collisionManager = new CollisionManager();

            this.mesh = new Mesh(tick, shaderProgram);

            this.spawner = new Spawner(
                tick, 
                mesh,
                new Vector3f(0, 0, 0),
                100,
                200.0f
            );

            this.dependencyContainer = new DependencyContainer();
            dependencyContainer.registerAll(
                tick,
                shaderProgram,
                mesh,
                mesh.getMeshRenderer(),
                dataController,
                stateController,
                collisionManager,
                spawner
            );
    
            this.envController = new EnvController(dependencyContainer);
            spawner.setEnvController(envController);
            
            this.upgrader = new Upgrader(envController);

            this.uiController = new UIController(
                window, 
                shaderProgram, 
                upgrader
            );
            inputController.setUiController(uiController);

            this.playerController = new PlayerController(
                tick, 
                window,
                mesh,
                collisionManager,
                spawner,
                upgrader,
                envController,
                dataController
            );
            mesh.setPlayerController(playerController);

            this.envRenderer = new EnvRenderer(
                envController, 
                collisionManager,
                playerController
            );

            start();
            
            this.init = true;
        }
    }

    /**
     * Start
     */
    private void start() {
        envRenderer.render();

        spawner.initialSpawn();
        spawner.setActive(true);
        spawner.printSpawnerStatus();
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
        spawner.update();
        playerController.getInputMap().getTreeInteractor().update();
    }

    /**
     * Render
     */
    public void render() {
        if(!init) return;

        spawner.render();
        mesh.renderAll();
        playerController.render();
    }
}
