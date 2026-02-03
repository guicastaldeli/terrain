package main.com.app.root.player;
import main.com.app.root.mesh.AnimatedModel;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.mesh.MeshData;
import main.com.app.root.mesh.MeshLoader;
import main.com.app.root.mesh.ModelInfo;
import main.com.app.root.mesh.ModelMap;
import main.com.app.root.Tick;
import main.com.app.root._resources.TextureLoader;

import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class PlayerMesh {
    private final Tick tick;
    private final PlayerController playerController;

    private final Mesh mesh;
    private MeshData meshData;
    private Vector3f meshOffset;
    private Vector3f meshScale;
    private Vector3f meshRotation;

    private AnimatedModel animatedModel;

    public static final Map<String, Boolean> PLAYER_MESH_MAP = Map.of(
        "susie", true,
        "susie_flower_base", false,
        "susie_flower", false
    );
    private static String TEX_PATH;

    public PlayerMesh(
        Tick tick, 
        PlayerController playerController,
        Mesh mesh
    ) {
        this.tick = tick;
        this.playerController = playerController;
        this.mesh = mesh;

        setMesh();
        
        this.meshOffset = new Vector3f(0.0f, 0.0f, 0.0f);
        this.meshScale = new Vector3f(1.0f, 1.0f, 1.0f);
        this.meshRotation = new Vector3f(0.0f, 0.0f, 0.0f);
    }

    /**
     * Set Mesh
     */
    public void setMesh() {
        for(Map.Entry<String, Boolean> val : PLAYER_MESH_MAP.entrySet()) {
            if(val.getValue() == true) {
                animatedModel = MeshLoader.loadAnimatedModel(val.getKey(), val.getKey());
                if(animatedModel != null) {
                    animatedModel.getMeshData().setIsDynamic(true);
                    mesh.addAnimatedModel(val.getKey(), animatedModel);
                    meshData = animatedModel.getMeshData();
                }
            } else if(val.getValue() == false) {
                MeshData data = MeshLoader.loadModel(val.getKey(), val.getKey());
                if(data != null) {
                    data.setIsDynamic(true);
                    mesh.add(val.getKey(), data);
                    meshData = data;
                }
            }
        }
        loadTex();
    }

    /**
     * Load Texure
     */
    private void loadTex() {
        for(String val : PLAYER_MESH_MAP.keySet()) {
            ModelMap modelMap = MeshLoader.getModelMap();
            ModelInfo info = modelMap.getModelInfo(val);
            TEX_PATH = info.getTexture();
            System.out.println("texture!!: " + info.getTexture());
    
            int id = TextureLoader.load(TEX_PATH);
            if(id <= 0) {
                System.err.println("FAILED to load texture!");
                return;
            }
            
            mesh.setTex(val, id);
        }
    }

    public MeshData getMeshData() {
        return meshData;
    }
    
    public boolean isMeshLoaded() {
        return mesh.getMeshRenderer() != null;
    }

    /**
     * Offset
     */
    public void setMeshOffset(float x, float y, float z) {
        meshOffset.set(x, y, z);
    }

    public Vector3f getMeshOffset() {
        return new Vector3f(meshOffset);
    }

    /**
     * Scale
     */
    public void setMeshScale(float x, float y, float z) {
        meshScale.set(x, y, z);
    }
    
    public Vector3f getMeshScale() {
        return new Vector3f(meshScale);
    }

    /**
     * Rotation
     */
    public void setMeshRotation(float x, float y, float z) {
        meshRotation.set(x, y, z);
    }

    public Vector3f getMeshRotation() {
        return new Vector3f(meshRotation);
    }

    private void applyCameraRotation(Matrix4f modelMatrix) {
        Camera camera = playerController.getCamera();
    }

    /**
     * Update Model Matrix
     */
    private void updateMeshModelMatrix() {
        if(mesh.getMeshRenderer() == null) return;

        Vector3f playerPos = playerController.getPosition();

        for(Map.Entry<String, Boolean> val : PLAYER_MESH_MAP.entrySet()) {
            String meshName = val.getKey();
            Vector3f meshPos = new Vector3f(playerPos);
            
            float dirX = meshRotation.x * 2.0f;
            float dirY = meshRotation.y / 2.0f;
            float dirZ = meshRotation.z;

            Matrix4f model = new Matrix4f()//
                .translate(meshPos)
                .rotateX((float) Math.toRadians(dirX))
                .rotateY((float) Math.toRadians(dirY)) 
                .rotateZ((float) Math.toRadians(dirZ));
            
            mesh.setModelMatrix(meshName, model);
        }
    }

    public void update() {
        if(mesh.getMeshRenderer() != null) {
            updateMeshModelMatrix();
        }
        if(animatedModel != null && mesh.getAnimationController() != null) {
            for(Map.Entry<String, Boolean> val : PLAYER_MESH_MAP.entrySet()) {
                if(val.getValue()) {
                    mesh.getAnimationController().update(val.getKey());
                }
            }
        }
    }

    public void render() {
        if(mesh.getMeshRenderer() != null) {
            updateMeshModelMatrix();
            for(Map.Entry<String, Boolean> val : PLAYER_MESH_MAP.entrySet()) {
                int hasAnimation = val.getValue() ? 1 : 0;
                mesh.getMeshRenderer().shaderProgram.setUniform("hasAnimation", hasAnimation);
                mesh.render(val.getKey(), 0);
            }
        }
    }
}
