package main.com.app.root.env.torch;
import main.com.app.root.Spawner;
import main.com.app.root._resources.TextureLoader;
import main.com.app.root.lightning.LightningController;
import main.com.app.root.lightning.PointLight;
import main.com.app.root.mesh.Mesh;
import org.joml.Vector3f;

public class TorchGenerator {
    public final Vector3f position;
    public final Spawner spawner;
    public TorchController torchController;
    public Mesh mesh;

    private final LightningController lightningController;
    private PointLight pointLight;
    private String id;

    public String MESH_ID;
    public static final String TEX_PATH ="root/src/main/com/app/root/_resources/texture/env/";

    public TorchGenerator(
        Vector3f position,
        Mesh mesh,
        Spawner spawner,
        LightningController lightningController
    ) {
        this.position = position;
        this.spawner = spawner;
        this.lightningController = lightningController;
        this.mesh = mesh;

        this.MESH_ID = "torch_" + System.currentTimeMillis();
    }

    /**
     * Light
     */
    public void setLight(PointLight pointLight) {
        this.pointLight = pointLight;
    }
    
    public PointLight getLight() {
        return pointLight;
    }

    /**
     * Create Mesh
     */
    public void createMesh() {
        try {
            mesh.addModel(MESH_ID, "torch");
            mesh.setPosition(MESH_ID, position);
            loadTex("torch");
        } catch(Exception err) {
            System.err.println("Failed to create mesh for torch: " + err.getMessage());
        }
    }

    /**
     * Load texture
     */
    public void loadTex(String name) {
        int texId = TextureLoader.load(TEX_PATH + name + ".png");
        if(texId <= 0) {
            System.err.println("FAILED to load torch texture!");
            return;
        }
        mesh.setTex(MESH_ID, texId);
    }

    public void destroyMesh() {
        if(mesh.hasMesh(MESH_ID)) {
            mesh.remove(MESH_ID);
        }
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getId() {
        return id;
    }
    
    public Vector3f getPosition() {
        return new Vector3f(position);
    }

    /**
     * Update
     */
    public void update(float deltaTime) {
        if(pointLight != null) {
            float flicker = (float) (Math.sin(System.currentTimeMillis() * 0.01f) * 0.1f + 1.0f);
            pointLight.setIntensity(2.0f * flicker);
        }
    }

    /**
     * Render
     */
    public void render() {}

    /**
     * Cleanup
     */
    public void cleanup() {
        destroyMesh();
    }
}
