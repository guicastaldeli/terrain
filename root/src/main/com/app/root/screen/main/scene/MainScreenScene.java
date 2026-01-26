package main.com.app.root.screen.main.scene;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.player.Camera;
import main.com.app.root.DependencyContainer;
import main.com.app.root.Tick;
import main.com.app.root.Window;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.env.EnvCall;
import main.com.app.root.env.EnvController;
import main.com.app.root.env.EnvData;
import main.com.app.root.env.EnvRenderer;

public class MainScreenScene {
    private final Window window;
    private final Tick tick;
    private final ShaderProgram shaderProgram;
    private Camera camera;

    private Mesh mesh;
    private World world;
    private EnvController envController;
    private EnvRenderer envRenderer;
    private DependencyContainer dependencyContainer;

    public boolean init = false;

    private Object skyboxInstance;

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

    public World getWorld() {
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
            camera.setPosition(0, 550, 0);
            mesh.getMeshRenderer().setCamera(camera);
            mesh.setCamera(camera);
            
            start();
            
            this.init = true;
        }
    }

    /**
     * Start
     */
    private void start() {
        world = new World(
            tick, 
            mesh, 
            mesh.getMeshRenderer(), 
            shaderProgram
        );
    }

    /**
     * Update
     */
    public void update() {
        if(!init) return;
        mesh.update();
        if(skyboxInstance != null) {
            Object skyboxMesh = EnvCall.callReturn(skyboxInstance, "getMesh");
            if(skyboxMesh != null) {
                EnvCall.call(skyboxMesh, "update");
            }
        }
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
        getWorld().render(0, 0);
        mesh.renderAll();
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
