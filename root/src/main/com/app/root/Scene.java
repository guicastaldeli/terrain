package main.com.app.root;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.player.PlayerController;
import main.com.app.root.ui.UIController;
import main.com.app.root._save.DataGetter;
import main.com.app.root._save.SaveGenerator;
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
    private DataGetter dataGetter;
    private SaveGenerator saveGenerator;

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

    public DataController getDataController() {
        return dataController;
    }

    public EnvController getEnvController() {
        return envController;
    }

    public void setInputController(InputController inputController) {
        this.inputController = inputController;
    } 

    public UIController getUIController() {
        return uiController;
    }

    public Spawner getSpawner() {
        return spawner;
    }

    public void setSaveGenerator(SaveGenerator saveGenerator) {
        this.saveGenerator = saveGenerator;
    }

    /**
     * Data Getter
     */
    public DataGetter getDataGetter() {
        return dataGetter;
    }

    public void setDataGetter(DataGetter dataGetter) {
        this.dataGetter = dataGetter;
    }

    /**
     * Player Controller
     */
    public PlayerController getPlayerController() {
        return playerController;
    }

    public void initSetPlayerController() {
        if(playerController != null) {
            playerController.set();
        }
    }

    /**
     * Upgrader
     */
    public Upgrader getUpgrader() {
        return upgrader;
    }

    private Upgrader initUpgrader(EnvController envController) {
        if(this.upgrader == null) {
            return new Upgrader(envController);
        } else {
            this.upgrader.setEnvController(envController);
            return this.upgrader;
        }
    }

    /**
     * Setup
     */
    public void setup(boolean reset) {
        if(!init) {
            if(reset) cleanup();

            this.collisionManager = new CollisionManager();

            this.mesh = new Mesh(tick, shaderProgram);

            if(reset || spawner == null) {
                this.spawner = new Spawner(
                    tick, 
                    mesh,
                    new Vector3f(0, 0, 0),
                    200.0f
                );
            } else {
                spawner.mesh = mesh;
            }

            this.dependencyContainer = new DependencyContainer();
            dependencyContainer.registerAll(
                tick,
                shaderProgram,
                mesh,
                mesh.getMeshRenderer(),
                dataController,
                stateController,
                collisionManager,
                spawner,
                playerController
            );

            if(reset || envController == null) {
                this.envController = new EnvController(dependencyContainer);
            }
            
            saveGenerator.setEnvController(getEnvController());

            dataGetter.setEnvController(envController);
            spawner.setEnvController(envController);

            this.upgrader = initUpgrader(envController);
            if(reset && upgrader != null) upgrader.setData(new MainData());

            this.playerController = new PlayerController(
                tick, 
                window,
                mesh,
                collisionManager,
                spawner,
                upgrader,
                envController,
                dataController,
                stateController,
                true
            );
            playerController.updateAxePosition();

            this.uiController = new UIController(
                window, 
                shaderProgram, 
                upgrader,
                mesh
            );
            inputController.setUiController(uiController);
            uiController.setupMouse();

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
        if(uiController != null) uiController.update();
        playerController.getInputMap().getTreeInteractor().update();
    }

    /**
     * Render
     */
    public void render() {
        if(!init) return;

        mesh.renderAll();
        playerController.render();
    }

    /**
     * Init
     */
    public void init(boolean reset) {
        cleanup();
        setup(reset);
    }

    public void cleanup() {
        init = false;
        
        if(mesh != null) mesh.cleanup();
        if(playerController != null) playerController.reset();
        
        mesh = null;
        envRenderer = null;
        envController = null;
    }
}
