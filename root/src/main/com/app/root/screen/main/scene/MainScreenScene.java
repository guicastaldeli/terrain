package main.com.app.root.screen.main.scene;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.Tick;
import main.com.app.root.Window;
import main.com.app.root._shaders.ShaderProgram;

public class MainScreenScene {
    private final Window window;
    private final Tick tick;

    private Mesh mesh;
    private World world;
    private ShaderProgram shaderProgram;

    public boolean init = false;

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

    /**
     * Setup
     */
    public void setup() {
        if(!init) {
            this.mesh = new Mesh(tick, shaderProgram);

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
    }

    /**
     * Render
     */
    public void render() {
        if(!init) return;

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
