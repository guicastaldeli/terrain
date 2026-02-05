package main.com.app.root;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.mesh.particle.ParticleManager;
import main.com.app.root.player.PlayerController;
import main.com.app.root.ui.UIController;
import main.com.app.root.utils.Fog;
import main.com.app.root.utils.SceneLight;
import main.com.app.root._save.DataGetter;
import main.com.app.root._save.SaveGenerator;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.collision.CollisionManager;
import main.com.app.root.env.EnvController;
import main.com.app.root.env.EnvRenderer;
import main.com.app.root.lightning.AmbientLight;
import main.com.app.root.lightning.LightningController;
import main.com.app.root.lightning.LightningData;
import main.com.app.root.lightning.LightningRenderer;
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

    private LightningController lightningController;
    private LightningRenderer lightningRenderer;
    private SceneLight sceneLight;

    private Spawner spawner;
    private Upgrader upgrader;

    private ParticleManager particleManager;

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
                    collisionManager,
                    new Vector3f(0, 150, 0), 500.0f
                );
                this.spawner.setEnvController(envController);
            } else {
                this.spawner.setMesh(mesh);
                this.spawner.setEnvController(envController);
            }

            this.lightningController = new LightningController();

            this.particleManager = new ParticleManager(tick, mesh);

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
                playerController,
                lightningController,
                particleManager,
                uiController
            );

            if(reset || envController == null) {
                this.envController = new EnvController(dependencyContainer);
                mesh.getMeshRenderer().setEnvController(envController);
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
            playerController.getInputMap().getTreeInteractor().setUIController(uiController);

            mesh.setPlayerController(playerController);

            this.envRenderer = new EnvRenderer(
                envController, 
                collisionManager,
                playerController,
                dataController
            );

            this.lightningRenderer = new LightningRenderer(lightningController, shaderProgram);
            mesh.setLightningRenderer(lightningRenderer);
            mesh.getMeshRenderer().setLightningRenderer(lightningRenderer);
  
            this.sceneLight = new SceneLight(tick, lightningController, envController);
            lightningController.add(LightningData.AMBIENT, new AmbientLight());
            sceneLight.set();

            start();
            
            this.init = true;
        }
    }

    /**
     * Start
     */
    private void start() {
        envRenderer.render();
        spawner.registerHandlers(envController, lightningController);
        spawner.setActive(true);
        spawner.setUIController(uiController);
        //particleManager.render();
    }

    /**
     * Update
     */
    public void update() {
        if(!init) return;
        playerController.update();
        collisionManager.updateDynamicColliders(tick.getDeltaTime());
        
        mesh.update();
        particleManager.update();
        envRenderer.update();
        spawner.update();

        if(uiController != null) uiController.update();
        playerController.getInputMap().getTreeInteractor().update();
        sceneLight.updateColors();
    }

    /**
     * Render
     */
    public void render() {
        if(!init) return;

        Vector3f cameraPosition = playerController.getCamera().getPosition();
        lightningRenderer.updateShaderUniforms(cameraPosition);
        
        mesh.renderAll();
        playerController.render();
        Fog.apply(
            playerController.getCamera(), 
            playerController, 
            shaderProgram, 
            envController
        );
        uiController.render();
        if(spawner != null) spawner.render();
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
        spawner = null;
    }
}