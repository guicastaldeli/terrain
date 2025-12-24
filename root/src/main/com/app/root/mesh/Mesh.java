package main.com.app.root.mesh;
import main.com.app.root.Tick;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.player_controller.PlayerController;
import java.util.HashMap;
import java.util.Map;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Mesh {
    private final Tick tick;
    private final ShaderProgram shaderProgram;

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

    /**
     * Add Mesh
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

    public float[] getModelSize(String modelName) {
        return MeshLoader.getModelSize(modelName);
    }

    public void setTex(String id, int textureId) {
        MeshRenderer renderer = meshRendererMap.get(id);
        if(renderer != null) {
            renderer.setTex(textureId);
        } else {
            System.err.println("No renderer found for mesh ID: " + id);
        }
    }

    private void addToMap(String id, MeshData data) {
        meshDataMap.put(id, data);

        MeshRenderer newRenderer = new MeshRenderer(tick, shaderProgram);
        newRenderer.setData(data);
        if(meshRenderer != null && meshRenderer.getPlayerController() != null) {
            newRenderer.setPlayerController(meshRenderer.getPlayerController());
        }
        
        meshRendererMap.put(id, newRenderer);
    }
    
    public void setPlayerController(PlayerController playerController) {
        this.meshRenderer.setPlayerController(playerController);
        for(MeshRenderer renderer : meshRendererMap.values()) {
            renderer.setPlayerController(playerController);
        }
    }

    public MeshData getData(String id) {
        return meshDataMap.get(id);
    }

    public void setModelMatrix(String id, Matrix4f matrix) {
        MeshRenderer renderer = meshRendererMap.get(id);
        if(renderer != null) {
            renderer.setModelMatrix(matrix);
        }
    }

    /**
     * Update
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
     * Render
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

    public boolean hasMesh(String id) {
        return meshRendererMap.containsKey(id);
    }

    public void setPosition(String id, Vector3f position) {
        MeshRenderer renderer = meshRendererMap.get(id);
        if(renderer != null) {
            renderer.setPosition(position);
        }
    }
    
    /**
     * Remove Mesh
     */
    public void removeMesh(String id) {
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
