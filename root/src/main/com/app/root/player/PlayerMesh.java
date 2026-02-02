package main.com.app.root.player;
import main.com.app.root.mesh.AnimatedModel;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.mesh.MeshData;
import main.com.app.root.mesh.MeshLoader;
import main.com.app.root.mesh.ModelInfo;
import main.com.app.root.mesh.ModelMap;
import main.com.app.root.Tick;
import main.com.app.root._resources.TextureLoader;
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

    public static final String PLAYER_MESH_ID = "susie_flower_base";
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
        animatedModel = MeshLoader.loadAnimatedModel("susie", PLAYER_MESH_ID);
        if(animatedModel != null) {
            animatedModel.getMeshData().setIsDynamic(true);
            mesh.addAnimatedModel(PLAYER_MESH_ID, animatedModel);
            meshData = animatedModel.getMeshData();
        }
        loadTex();
    }

    /**
     * Load Texure
     */
    private void loadTex() {
        ModelMap modelMap = MeshLoader.getModelMap();
        ModelInfo info = modelMap.getModelInfo(PLAYER_MESH_ID);
        TEX_PATH = info.getTexture();
        System.out.println("texture!!: " + info.getTexture());

        int id = TextureLoader.load(TEX_PATH);
        if(id <= 0) {
            System.err.println("FAILED to load texture!");
            return;
        }
        
        mesh.setTex(PLAYER_MESH_ID, id);
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

        Camera camera = playerController.getCamera();
        Vector3f playerPos = playerController.getPosition();
        float distanceFromPlayer = camera.distanceFromTarget;

        Vector3f forward = camera.getFront();
        forward.y = 0.0f;
        forward.normalize();

        Vector3f meshPos = new Vector3f(playerPos)
            .add(new Vector3f(forward).mul(distanceFromPlayer))
            .add(meshOffset);

        float dirX = meshRotation.x * 2.0f;
        float dirY = meshRotation.y / 2.0f;
        float dirZ = meshRotation.z;

        Matrix4f model = new Matrix4f()
            .translate(meshPos)
            .rotateX((float) Math.toRadians(dirX))
            .rotateY((float) Math.toRadians(dirY)) 
            .rotateZ((float) Math.toRadians(dirZ))
            .scale(meshScale);
        
        mesh.setModelMatrix(PLAYER_MESH_ID, model);
    }

    public void update() {
        if(mesh.getMeshRenderer() != null) {
            updateMeshModelMatrix();
        }
        if (animatedModel != null && mesh.getAnimationController() != null) {
            mesh.getAnimationController().update(PLAYER_MESH_ID);
        }
    }

    public void render() {
        if(mesh.getMeshRenderer() != null) {
            //updateMeshModelMatrix();
            mesh.getMeshRenderer().shaderProgram.setUniform("hasAnimation", 1);
            mesh.render(PLAYER_MESH_ID, 0);
        }
    }
}
