package main.com.app.root.env.world;
import main.com.app.root.Tick;
import main.com.app.root.collision.CollisionManager;
import main.com.app.root.collision.types.DynamicObject;
import main.com.app.root.mesh.MeshData;
import main.com.app.root.mesh.MeshLoader;
import main.com.app.root.player.RigidBody;
import org.joml.Vector3f;

public class Water {
    public static DynamicObject collider;

    public static final float LEVEL = 50.0f;
    public static final float MIN_DEPTH = 5.0f;
    public static final float MIN_Y = LEVEL - MIN_DEPTH;

    public static String getId(int chunkX, int chunkZ) {
        return "water_" + chunkX + "_" + chunkZ;
    }

    /**
     * Add Collider
     */
    public static void addCollider(CollisionManager collisionManager) {
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

        Water.collider = new DynamicObject(rigidBody, "WATER");
        collisionManager.addStaticCollider(collider);
    }

    /**
     * Create Mesh Data
     */
    public static MeshData createMeshData(int chunkX, int chunkZ) {
        MeshData meshData = MeshLoader.load(MeshData.MeshType.MAP, getId(chunkX, chunkZ));

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
            colors[colorIdx + 3] = 0.6f;
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

        return meshData;
    }
}
