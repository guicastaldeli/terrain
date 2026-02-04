package main.com.app.root.env.world;
import main.com.app.root.Tick;
import main.com.app.root._resources.TextureLoader;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.collision.CollisionManager;
import main.com.app.root.collision.types.DynamicObject;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.mesh.MeshData;
import main.com.app.root.mesh.MeshLoader;
import main.com.app.root.player.RigidBody;
import java.util.Random;
import org.joml.Vector3f;

public class Water {
    private final Mesh mesh;
    private final ShaderProgram shaderProgram;
    public static DynamicObject collider;

    public static final String TEX_PATH ="root/src/main/com/app/root/_resources/texture/env/water.png";

    public static final float LEVEL = 50.0f;
    public static final float SPAWN_LEVEL = 48.0f;
    public static final float SHADER_LEVEL = LEVEL - 0.00005f;
    public static final float MIN_DEPTH = 5.0f;
    public static final float MIN_Y = LEVEL - MIN_DEPTH;

    public Water(Mesh mesh, ShaderProgram shaderProgram) {
        this.mesh = mesh;
        this.shaderProgram = shaderProgram;
    }

    public static String getId(int chunkX, int chunkZ) {
        return "water_" + chunkX + "_" + chunkZ;
    }

    /**
     * Add Collider
     */
    public static void addCollider(WorldGenerator worldGenerator, CollisionManager collisionManager) {
        RigidBody rigidBody = new RigidBody(
            Tick.instance,
            new Vector3f(0, MIN_Y + (MIN_DEPTH / 2), 0),
            new Vector3f(
                WorldGenerator.WORLD_SIZE, 
                MIN_DEPTH, 
                WorldGenerator.WORLD_SIZE
            )
        );
        rigidBody.setStatic(true);
        rigidBody.setGravityEnabled(false);

        Water.collider = new DynamicObject(
            rigidBody,
            worldGenerator,
            "WATER"
        );
        collisionManager.addStaticCollider(collider);
    }

    /**
     * Load Texture
     */
    public void loadTex(int chunkX, int chunkZ) {
        int texId = TextureLoader.load(TEX_PATH);
        if(texId <= 0) {
            System.err.println("FAILED to load water texture!");
            return;
        }
        
        String id = getId(chunkX, chunkZ);
        mesh.setTex(id, texId);
    }

    /**
     * Create Mesh Data
     */
    public static MeshData createMeshData(int chunkX, int chunkZ) {
        MeshData meshData = MeshLoader.load(MeshData.MeshType.WORLD, getId(chunkX, chunkZ));

        float worldOffsetX = (chunkX * Chunk.CHUNK_SIZE) - (WorldGenerator.WORLD_SIZE / 2.0f);
        float worldOffsetZ = (chunkZ * Chunk.CHUNK_SIZE) - (WorldGenerator.WORLD_SIZE / 2.0f);
        int heightDataSize = Chunk.CHUNK_SIZE + 1;
        
        float[] vertices = new float[heightDataSize * heightDataSize * 3];
        
        for(int x = 0; x < heightDataSize; x++) { 
            for(int z = 0; z < heightDataSize; z++) {
                int i = (x * heightDataSize + z) * 3;
                vertices[i] = worldOffsetX + x;
                vertices[i+1] = LEVEL;
                vertices[i+2] = worldOffsetZ + z;
            }
        }

        int[] indices = new int[Chunk.CHUNK_SIZE * Chunk.CHUNK_SIZE * 6];
        int i = 0;
        for(int x = 0; x < Chunk.CHUNK_SIZE; x++) {
            for(int z = 0; z < Chunk.CHUNK_SIZE; z++) {
                int topLeft = x * heightDataSize + z;
                int topRight = topLeft + 1;
                int bottomLeft = (x + 1) * heightDataSize + z;
                int bottomRight = bottomLeft + 1;

                indices[i++] = topLeft;
                indices[i++] = bottomLeft;
                indices[i++] = topRight;
                indices[i++] = topRight;
                indices[i++] = bottomLeft;
                indices[i++] = bottomRight;
            }
        }

        float[] colors = new float[heightDataSize * heightDataSize * 4];
        for(int j = 0; j < heightDataSize * heightDataSize; j++) {
            int colorIdx = j * 4;
            colors[colorIdx] = 0.0f;
            colors[colorIdx + 1] = 0.1f;
            colors[colorIdx + 2] = 0.4f;
            colors[colorIdx + 3] = 0.3f;
        }

        float[] texCoords = new float[heightDataSize * heightDataSize * 2];
        float uvScale = 2.0f;
        for(int x = 0; x < heightDataSize; x++) {
            for(int z = 0; z < heightDataSize; z++) {
                int idx = (x * heightDataSize + z) * 2;
                texCoords[idx] = (x / (float)Chunk.CHUNK_SIZE) * uvScale;
                texCoords[idx + 1] = (z / (float)Chunk.CHUNK_SIZE) * uvScale;
            }
        }

        float[] normals = new float[heightDataSize * heightDataSize * 3];
        for(int j = 0; j < heightDataSize * heightDataSize; j++) {
            int idx = j * 3;
            normals[idx] = 0.0f;
            normals[idx + 1] = 1.0f;
            normals[idx + 2] = 0.0f;
        }

        meshData.setVertices(vertices);
        meshData.setIndices(indices);
        meshData.setColors(colors);
        meshData.setNormals(normals);
        meshData.setTexCoords(texCoords);
        meshData.setIsTransparent(true);

        return meshData;
    }

    /**
     * Water Effect
     */
    public void createSwimEffect(Vector3f position) {
        Random random = new Random();
        
        int amount = 20;
        float size = 0.3f;
        float speed = 0.01f + (LEVEL * 0.1f);
        float lifetime = 2.5f + (LEVEL * 0.3f);
        
        mesh.getParticleManager()
            .create(
                position, 
                new Vector3f(), 
                amount, 
                size, 
                speed, 
                lifetime
            );
        
        Vector3f velNum = new Vector3f(20.0f, 5.0f, 20.0f);
        mesh.getParticleManager().getParticleSystem().setVelNum(velNum);
        
        mesh.getParticleManager().getParticleSystem().emit(position, true, () -> {
            float blueIntensity = 0.2f + random.nextFloat() * 0.8f;
            return new Vector3f(0.0f, 0.0f, blueIntensity);
        });
    }
}
