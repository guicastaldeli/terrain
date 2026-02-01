package main.com.app.root.player;
import main.com.app.root.mesh.AnimatedModel;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.mesh.MeshData;
import main.com.app.root.mesh.MeshLoader;
import main.com.app.root.Tick;
import main.com.app.root._resources.TextureLoader;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class PlayerMesh {
    private final Tick tick;
    private final PlayerController playerController;

    public static final String PLAYER_MESH_ID = "PLAYER_MESH";
    private static final String TEX_PATH = "root/src/main/com/app/root/_resources/texture/misc/dino.png";
    private final Mesh mesh;
    private MeshData meshData;
    private Vector3f meshOffset;
    private Vector3f meshScale;
    private Vector3f meshRotation;

    private AnimatedModel animatedModel;

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
        this.meshRotation = new Vector3f(-90.0f, 90.0f, -180.0f);
    }

    /**
     * Set Mesh
     */
    public void setMesh() {
        animatedModel = MeshLoader.loadAnimatedModel("ball", PLAYER_MESH_ID);
        if(animatedModel != null) {
            animatedModel.getMeshData().setIsDynamic(true);
            mesh.addAnimatedModel(PLAYER_MESH_ID, animatedModel);
            meshData = animatedModel.getMeshData();
        }
        /*
        MeshData data = MeshLoader.loadModel("cloud3", PLAYER_MESH_ID);
        if(data != null) {
            //data.setColorHex("#b45353ff");
            data.setIsDynamic(true);
            mesh.add(PLAYER_MESH_ID, data);
            meshData = data;
            //loadTex();
        }
            */
    }

    /**
     * Load Texure
     */
    private void loadTex() {
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

        Vector3f rotation = getMeshRotation();

        Matrix4f model = new Matrix4f()
            .translate(meshPos)
            .rotateX((float) Math.toRadians(meshRotation.x))
            .rotateY((float) Math.toRadians(meshRotation.y)) 
            .rotateZ((float) Math.toRadians(meshRotation.z))
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
            updateMeshModelMatrix();
            mesh.getMeshRenderer().shaderProgram.setUniform("hasAnimation", 1);
            mesh.render(PLAYER_MESH_ID, 0);
        }
    }
}
