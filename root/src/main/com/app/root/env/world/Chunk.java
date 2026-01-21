package main.com.app.root.env.world;
import main.com.app.root.collision.CollisionManager;
import main.com.app.root.collision.types.StaticObject;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.mesh.MeshData;
import main.com.app.root.mesh.MeshLoader;
import java.util.*;

public class Chunk {
    private final WorldGenerator worldGenerator;
    private final CollisionManager collisionManager;
    private final Mesh mesh;
    private MeshData meshData;

    public Map<String, ChunkData> loadedChunks = new HashMap<>();
    public Map<String, ChunkData> cachedChunks = new HashMap<>();
    public final Object chunkLock = new Object();
    
    public static final int CHUNK_SIZE = 64;

    public Chunk(
        WorldGenerator worldGenerator, 
        CollisionManager collisionManager,
        Mesh mesh,
        MeshData meshData
    ) {
        this.worldGenerator = worldGenerator;
        this.collisionManager = collisionManager;
        this.mesh = mesh;
        this.meshData = meshData;
    }

    /**
     * Get Coords
     */
    public static int[] getCoords(float worldX, float worldZ) {
        int x = (int)Math.floor((worldX + WorldGenerator.WORLD_SIZE / 2) / CHUNK_SIZE);
        int z = (int)Math.floor((worldZ + WorldGenerator.WORLD_SIZE / 2) / CHUNK_SIZE);
        return new int[]{ x, z };
    }

    /**
     * Get Id
     */
    public static String getId(int chunkX, int chunkZ) {
        return "chunk_" + chunkX + "_" + chunkZ;
    }

    public boolean isInRange(String chunkId, int centerX, int centerZ) {
        String[] parts = chunkId.split("_");
        int chunkX = Integer.parseInt(parts[1]);
        int chunkZ = Integer.parseInt(parts[2]);

        return Math.abs(chunkX - centerX) <= WorldGenerator.RENDER_DISTANCE &&
            Math.abs(chunkZ - centerZ) <= WorldGenerator.RENDER_DISTANCE;
    }

    public boolean isValid(int chunkX, int chunkZ) {
        int maxChunks = WorldGenerator.WORLD_SIZE / CHUNK_SIZE;
        return chunkX >= 0 && chunkX < maxChunks &&
            chunkZ >= 0 && chunkZ < maxChunks;
    }

    /**
     * Generate Mesh Data
     */
    public MeshData createMeshData(
        float[] heightData,
        int chunkX,
        int chunkZ
    ) {
        meshData = MeshLoader.load(MeshData.MeshType.MAP, getId(chunkX, chunkZ));

        float[] vertices = new float[CHUNK_SIZE * CHUNK_SIZE * 3];
        for(int x = 0; x < CHUNK_SIZE; x++) {
            for(int z = 0; z < CHUNK_SIZE; z++) {
                int i = (x * CHUNK_SIZE + z) * 3;
                vertices[i] = x - CHUNK_SIZE / 2.0f;
                vertices[i+1] = heightData[x * CHUNK_SIZE + z];
                vertices[i+2] = z - CHUNK_SIZE / 2.0f;
            }
        }

        int[] indices = new int[(CHUNK_SIZE - 1) * (CHUNK_SIZE - 1) * 6];
        int i = 0;
        for(int x = 0; x < CHUNK_SIZE - 1; x++) {
            for(int z = 0; z < CHUNK_SIZE - 1; z++) {
                int topLeft = x * CHUNK_SIZE + z;
                int topRight = topLeft + 1;
                int bottomLeft = (x + 1) * CHUNK_SIZE + z;
                int bottomRight = bottomLeft + 1;

                indices[i++] = topLeft;
                indices[i++] = bottomLeft;
                indices[i++] = topRight;
                indices[i++] = topRight;
                indices[i++] = bottomLeft;
                indices[i++] = bottomRight;
            }
        }

        meshData.setVertices(vertices);
        meshData.setIndices(indices);

        return meshData;
    }

    /**
     * Create Collider
     */
    public StaticObject createCollider(
        float[] heightData,
        int chunkX,
        int chunkZ
    ) {
        return new StaticObject(
            heightData,
            CHUNK_SIZE,
            CHUNK_SIZE,
            getId(chunkX, chunkZ)
        );
    }

    /**
     * Generate Height Data
     */
    public float[] generateHeightData(int chunkX, int chunkZ) {
        int startX = chunkX * CHUNK_SIZE;
        int startZ = chunkZ * CHUNK_SIZE;
        float[] heightData = new float[CHUNK_SIZE * CHUNK_SIZE];
        for(int x = 0; x < CHUNK_SIZE; x++) {
            for(int z = 0; z < CHUNK_SIZE; z++) {
                float worldX = startX + x - WorldGenerator.WORLD_SIZE / 2;
                float worldZ = startZ + z - WorldGenerator.WORLD_SIZE / 2;
                heightData[x * CHUNK_SIZE + z] = worldGenerator.getHeightAt(worldX, worldZ);
            }
        }
        return heightData;
    }

    /**
     * Update Chunks
     */
    public void updateChunks(float playerX, float playerZ) {
        int[] playerChunk = getCoords(playerX, playerZ);
        int playerChunkX = playerChunk[0];
        int playerChunkZ = playerChunk[1];

        synchronized(chunkLock) {
            List<String> chunksToUnload = new ArrayList<>();
            for(String chunkId : loadedChunks.keySet()) {
                if(!isInRange(chunkId, playerChunkX, playerChunkZ)) {
                    chunksToUnload.add(chunkId);
                }
            }
            for(String chunkId : chunksToUnload) {
                unload(chunkId);
            }
            for(int x = playerChunkX - WorldGenerator.RENDER_DISTANCE; x <= playerChunkX + WorldGenerator.RENDER_DISTANCE; x++) {
                for(int z = playerChunkZ - WorldGenerator.RENDER_DISTANCE; z <= playerChunkZ + WorldGenerator.RENDER_DISTANCE; z++) {
                    String chunkId = getId(x, z);
                    if(!loadedChunks.containsKey(chunkId) && isValid(x, z)) {
                        load(x, z);
                    }
                }
            }
        }
    }

    /**
     * Load
     */
    public void load(int chunkX, int chunkZ) {
        String chunkId = getId(chunkX, chunkZ);
        if(cachedChunks.containsKey(chunkId)) {
            ChunkData cached = cachedChunks.remove(chunkId);
            loadedChunks.put(chunkId, cached);
            render(chunkId);
            return;
        }

        try {
            float[] chunkHeightData = generateHeightData(chunkX, chunkZ);
            MeshData chunkMeshData = createMeshData(chunkHeightData, chunkX, chunkZ);
            StaticObject chunkCollider = createCollider(chunkHeightData, chunkX, chunkZ);

            ChunkData chunkData = new ChunkData(chunkMeshData, chunkCollider);
            loadedChunks.put(chunkId, chunkData);

            mesh.add(chunkId, chunkMeshData);

            if(chunkCollider != null) {
                collisionManager.addStaticCollider(chunkCollider);
            }

            render(chunkId);
            System.out.println("Loaded chunk: " + chunkId);
        } catch (Exception err) {
            System.err.println("Failed to load chunk " + chunkId + ": " + err.getMessage());
        }
    }

    /**
     * Unload
     */
    public void unload(String chunkId) {
        ChunkData chunkData = loadedChunks.remove(chunkId);
        if(chunkData != null) {
            mesh.removeMesh(chunkId);
            if(chunkData.collider != null) {
                collisionManager.removeCollider(chunkData.collider);
            }

            chunkData.isRendered = false;
            cachedChunks.put(chunkId, chunkData);

            if(cachedChunks.size() > 20) removeOldestCachedChunk();

            System.out.println("Unloaded chunk: " + chunkId);
        }
    }

    public void removeOldestCachedChunk() {
        String oldestChunkId = null;
        long oldestTime = Long.MAX_VALUE;
        for(Map.Entry<String, ChunkData> entry : cachedChunks.entrySet()) {
            if(entry.getValue().lastAccessTime < oldestTime) {
                oldestTime = entry.getValue().lastAccessTime;
                oldestChunkId = entry.getKey();
            }
        }
        if(oldestChunkId != null) {
            cachedChunks.remove(oldestChunkId);
        }
    }

    /**
     * Render
     */
    public void render(String chunkId) {
        ChunkData chunkData = loadedChunks.get(chunkId);
        if(chunkData != null) {
            chunkData.isRendered = true;
            chunkData.lastAccessTime = System.currentTimeMillis();
        }
    }
}
