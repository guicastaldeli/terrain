package main.com.app.root.mesh;
import main.com.app.root.Tick;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.lightning.LightningRenderer;
import main.com.app.root.player.Camera;
import main.com.app.root.player.PlayerController;
import java.util.HashMap;
import java.util.Map;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Mesh {
    private final Tick tick;
    private final ShaderProgram shaderProgram;
    private LightningRenderer lightningRenderer;

    private MeshRenderer meshRenderer;
    private MeshData meshData;
    private final Map<String, MeshRenderer> meshRendererMap;
    private final Map<String, MeshData> meshDataMap;

    public Mesh(Tick tick, ShaderProgram shaderProgram) {
        this.tick = tick;
        this.shaderProgram = shaderProgram;
        this.meshRendererMap = new HashMap<>();
        this.meshDataMap = new HashMap<>();
        this.meshRenderer = new MeshRenderer(tick, shaderProgram);
    }

    public MeshRenderer getMeshRenderer() {
        return meshRenderer;
    }

    public MeshRenderer getMeshRenderer(String id) {
        return meshRendererMap.get(id);
    }

    public Map<String, MeshRenderer> getMeshRendererMap() {
        return meshRendererMap;
    }

    public boolean hasMesh(String id) {
        return meshRendererMap.containsKey(id);
    }

    public void setLightningRenderer(LightningRenderer lightningRenderer) {
        this.lightningRenderer = lightningRenderer;
        this.meshRenderer.setLightningRenderer(lightningRenderer);
        
        for(MeshRenderer renderer : meshRendererMap.values()) {
            renderer.setLightningRenderer(lightningRenderer);
        }
    }

    public void setPlayerController(PlayerController playerController) {
        this.meshRenderer.setPlayerController(playerController);
        for(MeshRenderer renderer : meshRendererMap.values()) {
            renderer.setPlayerController(playerController);
        }
    }

    public void setCamera(Camera camera) {
        this.meshRenderer.setCamera(camera);
        for(MeshRenderer renderer : meshRendererMap.values()) {
            renderer.setCamera(camera);
        }
    }

    /**
     * 
     * Add Mesh
     * 
     */
    public void add(String id, MeshData meshData) {
        addToMap(id, meshData);
    }
    
    public void add(String id, MeshData.MeshType type) {
        MeshData meshData = MeshLoader.load(type, id);
        addToMap(id, meshData);
    }

    public void addModel(String id, String modelPath) {
        MeshData meshData = MeshLoader.loadModel(modelPath, id);
        addToMap(id, meshData);
    }

    private void addToMap(String id, MeshData data) {
        meshDataMap.put(id, data);

        MeshRenderer newRenderer = new MeshRenderer(tick, shaderProgram);
        newRenderer.setData(data);
        
        if(lightningRenderer != null) {
            newRenderer.setLightningRenderer(lightningRenderer);
        }
        if(meshRenderer != null && meshRenderer.getPlayerController() != null) {
            newRenderer.setPlayerController(meshRenderer.getPlayerController());
        }
        if(meshRenderer != null && meshRenderer.getCamera() != null) {
            newRenderer.setCamera(meshRenderer.getCamera());
        }
        
        meshRendererMap.put(id, newRenderer);
    }

    /**
     * 
     * Size
     * 
     */
    public void setScale(String id, Vector3f scale) {
        MeshRenderer renderer = meshRendererMap.get(id);
        if(renderer != null) renderer.setScale(scale);
    }

    public void setScale(String id, float scale) {
        setScale(id, new Vector3f(scale, scale, scale));
    }

    public void setScale(String id, float x, float y, float z) {
        setScale(id, new Vector3f(x, y, z));
    }

    public float[] getModelSize(String modelName) {
        return MeshLoader.getModelSize(modelName);
    }

    /**
     * 
     * Position
     * 
     */
    public void setPosition(String id, Vector3f position) {
        MeshRenderer renderer = meshRendererMap.get(id);
        if(renderer != null) {
            renderer.setPosition(position);
        }
    }

    public Vector3f getPosition(String id) {
        MeshRenderer renderer = meshRendererMap.get(id);
        if(renderer != null) {
            return renderer.getPosition();
        }
        return new Vector3f(0, 0, 0);
    }

    /**
     * Get Data
     */
    public MeshData getData(String id) {
        return meshDataMap.get(id);
    }
    
    /**
     * Set Texture
     */
    public void setTex(String id, int textureId) {
        MeshRenderer renderer = meshRendererMap.get(id);
        if(renderer != null) {
            renderer.setTex(textureId);
        } else {
            System.err.println("No renderer found for mesh ID: " + id);
        }
    }

    public void setModelMatrix(String id, Matrix4f matrix) {
        MeshRenderer renderer = meshRendererMap.get(id);
        if(renderer != null) {
            renderer.setModelMatrix(matrix);
        }
    }

    /**
     * 
     * Update
     * 
     */
    public void update() {
        for(MeshRenderer meshRenderer : meshRendererMap.values()) {
            //meshRenderer.updateRotation();
        }
    }

    public void updateColors(String id, float[] colors) {
        MeshRenderer renderer = meshRendererMap.get(id);
        if(renderer != null) {
            renderer.updateColors(colors);
        }
    }

    /**
     * 
     * Render
     * 
     */
    public void render(String id, int shaderType) {
        MeshRenderer meshRenderer = meshRendererMap.get(id);
        if(meshRenderer != null) meshRenderer.render(shaderType);
    }

    public void renderAll() {
        for(Map.Entry<String, MeshRenderer> entry : meshRendererMap.entrySet()) {
            String id = entry.getKey();
            MeshData data = meshDataMap.get(id);
            if(data != null) entry.getValue().render(data.getShaderType());
        }
    }
    
    /**
     * 
     * Remove / Cleanup
     * 
     */
    public void remove(String id) {
        MeshRenderer renderer = meshRendererMap.remove(id);
        MeshData data = meshDataMap.remove(id);
        if(renderer != null) {
            renderer.cleanup();
        }
        //System.out.println("Removed mesh: " + id);
    }

    public void cleanup() {
        for(MeshRenderer meshRenderer : meshRendererMap.values()) {
            meshRenderer.cleanup();
        }
        meshRendererMap.clear();
        meshDataMap.clear();
    }
}
