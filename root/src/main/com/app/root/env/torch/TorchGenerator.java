package main.com.app.root.env.torch;
import main.com.app.root.Spawner;
import main.com.app.root._resources.TextureLoader;
import main.com.app.root.lightning.LightningController;
import main.com.app.root.lightning.PointLight;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.mesh.MeshLoader;
import main.com.app.root.mesh.ModelInfo;
import main.com.app.root.mesh.ModelMap;

import org.joml.Vector3f;

public class TorchGenerator {
    public final Vector3f position;
    public final Spawner spawner;
    public TorchController torchController;
    public Mesh mesh;

    private final LightningController lightningController;
    private PointLight pointLight;
    private String id;

    public final String MESH_ID;

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
            loadTex(MESH_ID, "torch");
        } catch(Exception err) {
            System.err.println("Failed to create mesh for torch: " + err.getMessage());
        }
    }

    /**
     * Load texture
     */
    public void loadTex(String meshId, String name) {
        ModelMap modelMap = MeshLoader.getModelMap();
        ModelInfo info = modelMap.getModelInfo(name);
        String texPath = info.getTexture();
    
        int id = TextureLoader.load(texPath);
        if(id <= 0) {
            System.err.println("FAILED to load texture!");
            return;
        }
            
        mesh.setTex(meshId, id);
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
