package main.com.app.root.screen.main.scene;
import main.com.app.root.DependencyContainer;
import main.com.app.root.SceneLight;
import main.com.app.root.Tick;
import main.com.app.root.Window;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.env.EnvCall;
import main.com.app.root.env.EnvController;
import main.com.app.root.env.EnvData;
import main.com.app.root.env.EnvRenderer;
import main.com.app.root.lightning.AmbientLight;
import main.com.app.root.lightning.LightningController;
import main.com.app.root.lightning.LightningData;
import main.com.app.root.lightning.LightningRenderer;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.mesh.particle.ParticleManager;
import main.com.app.root.player.Camera;
import org.joml.Vector3f;

public class MainScreenScene {
    private final Window window;
    private final Tick tick;
    private final ShaderProgram shaderProgram;
    private Camera camera;

    private Mesh mesh;
    private MainScreenWorld world;
    private EnvController envController;
    private EnvRenderer envRenderer;
    private DependencyContainer dependencyContainer;
    private ParticleManager particleManager;

    public boolean init = false;

    private LightningController lightningController;
    private LightningRenderer lightningRenderer;
    private SceneLight sceneLight;

    private Object skyboxInstance;

    private float cameraX = 0.0f;
    private float cameraY = 450.0f;
    private float cameraZ = 150.0f;

    public MainScreenScene(
        Window window, 
        Tick tick,
        ShaderProgram shaderProgram
    ) {
        this.window = window;
        this.tick = tick;
        this.shaderProgram = shaderProgram;
    }

    public boolean isInit() {
        return init;
    }

    public MainScreenWorld getWorld() {
        return world;
    }

    public Mesh getMesh() {
        return mesh;
    }

    /**
     * Setup
     */
    public void setup() {
        if(!init) {
            this.mesh = new Mesh(tick, shaderProgram);

            this.dependencyContainer = new DependencyContainer();
            dependencyContainer.registerAll(
                tick,
                shaderProgram,
                mesh,
                mesh.getMeshRenderer()
            );
            this.envController = new EnvController(dependencyContainer);

            this.skyboxInstance = envController.getEnv(EnvData.SKYBOX).getInstance();

            this.camera = new Camera();
            camera.setPosition(cameraX, cameraY, cameraZ);
            camera.setRotation(0, -30);

            mesh.getMeshRenderer().setEnvController(envController);
            mesh.getMeshRenderer().setCamera(camera);
            mesh.setCamera(camera);

            this.lightningController = new LightningController();
            this.lightningRenderer = new LightningRenderer(lightningController, shaderProgram);
            mesh.setLightningRenderer(lightningRenderer);
            mesh.getMeshRenderer().setLightningRenderer(lightningRenderer);

            this.sceneLight = new SceneLight(tick, lightningController, envController);
            lightningController.add(LightningData.AMBIENT, new AmbientLight());
            sceneLight.set();

            this.particleManager = new ParticleManager(tick, mesh);
            
            start();
            
            this.init = true;
        }
    }

    /**
     * Start
     */
    private void start() {
        world = new MainScreenWorld(
            tick, 
            mesh, 
            mesh.getMeshRenderer(), 
            shaderProgram,
            particleManager
        );
    }

    /**
     * Update
     */
    public void update() {
        if(!init) return;

        Vector3f target = new Vector3f(0.0f, 200.0f, 0.0f);
        camera.orbitAroundPoint(target, new Vector3f(0.0f, 1.0f, 0.0f), 0.006f);
        
        mesh.update();
        particleManager.update();
        lightningController.update();
        if(skyboxInstance != null) {
            Object skyboxMesh = EnvCall.callReturn(skyboxInstance, "getMesh");
            if(skyboxMesh != null) {
                EnvCall.call(skyboxMesh, "update");
            }
        }

        world.update(cameraX, cameraY, cameraZ);

        sceneLight.updateColors();
    }

    /**
     * Render
     */
    public void render() {
        if(!init) return;
        if(skyboxInstance != null) {
            Object skyboxMesh = EnvCall.callReturn(skyboxInstance, "getMesh");
            if(skyboxMesh != null) {
                EnvCall.call(skyboxMesh, "render");
            }
        }

        Vector3f cameraPosition = camera.getPosition();
        lightningRenderer.updateShaderUniforms(cameraPosition);
        
        world.render(camera.getPosition().x, camera.getPosition().z);
        mesh.renderAll();
        mesh.getMeshRenderer().applyFog();
    }

    /**
     * Init
     */
    public void init() {
        cleanup();
        setup();
    }

    public void cleanup() {
        init = false;
        
        if(mesh != null) mesh.cleanup();
        mesh = null;
    }
}