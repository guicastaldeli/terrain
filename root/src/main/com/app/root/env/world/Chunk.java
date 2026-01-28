package main.com.app.root.env.world;
import main.com.app.root.Spawner;
import main.com.app.root.collision.CollisionManager;
import main.com.app.root.collision.types.StaticObject;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.mesh.MeshData;
import main.com.app.root.mesh.MeshLoader;
import main.com.app.root.player.Camera;

import java.util.*;

public class Chunk {
    private final WorldGenerator worldGenerator;
    private final CollisionManager collisionManager;
    private final Spawner spawner;
    private final Mesh mesh;
    private MeshData meshData;

    public final Object chunkLock = new Object();
    public Map<String, ChunkData> loadedChunks = new HashMap<>();
    public Map<String, ChunkData> cachedChunks = new HashMap<>();
    
    private List<String> chunksToLoad = new ArrayList<>();
    private int chunksPerFrame = 1;
    private int lastProcessedIndex = 0;
    private static final long MIN_TIME_BETWEEN_CHUNKS = 16;

    public static final int CHUNK_SIZE = 90;

    public Chunk(
        WorldGenerator worldGenerator, 
        CollisionManager collisionManager,
        Mesh mesh,
        MeshData meshData,
        Spawner spawner
    ) {
        this.worldGenerator = worldGenerator;
        this.collisionManager = collisionManager;
        this.mesh = mesh;
        this.meshData = meshData;
        this.spawner = spawner;
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

        return Math.abs(chunkX - centerX) <= Camera.RENDER_DISTANCE &&
            Math.abs(chunkZ - centerZ) <= Camera.RENDER_DISTANCE;
    }

    public boolean isValid(int chunkX, int chunkZ) {
        int maxChunks = WorldGenerator.WORLD_SIZE / CHUNK_SIZE;
        return chunkX >= 0 && chunkX < maxChunks &&
            chunkZ >= 0 && chunkZ < maxChunks;
    }

    /**
     * Generate Colors
     */
    private float[] generateColors(float[] heightData, int chunkX, int chunkZ) {
        int heightDataSize = CHUNK_SIZE + 1;
        float[] colors = new float[heightDataSize * heightDataSize * 4];
        
        int worldStartX = chunkX * CHUNK_SIZE;
        int worldStartZ = chunkZ * CHUNK_SIZE;
        
        float OCEAN_DEPTH = 100.0f;
        float GRASS_LEVEL = 65.0f;
        float MOUNTAIN_LEVEL = 250.0f;
        
        for(int x = 0; x < heightDataSize; x++) {
            for(int z = 0; z < heightDataSize; z++) {
                int i = x * heightDataSize + z;
                int colorIdx = i * 4;
                
                float worldX = worldStartX + x;
                float worldZ = worldStartZ + z;
                
                float heightVal = heightData[i];
                colors[colorIdx + 3] = 1.0f;
                
                if(heightVal < Water.LEVEL) {
                    colors[colorIdx] = 0.0f;
                    colors[colorIdx + 1] = 0.1f;
                    colors[colorIdx + 2] = 0.4f;;
                } else if(heightVal < GRASS_LEVEL) {
                    float noise = 
                        worldGenerator
                            .noiseGeneratorWrapper
                            .fractualSimplexNoise(
                                worldX * 0.05f, worldZ * 0.05f, 
                                3, 
                                0.4f, 
                                2.0f
                            );

                    float baseGreen = 0.7f + noise * 0.15f;
                    float redTint = 0.3f + noise * 0.1f;
                    colors[colorIdx] = redTint;
                    colors[colorIdx + 1] = baseGreen;
                    colors[colorIdx + 2] = 0.3f + noise * 0.1f;
                } else if(heightVal < MOUNTAIN_LEVEL) {
                    float noise = 
                        worldGenerator
                            .noiseGeneratorWrapper
                            .fractualSimplexNoise(
                                worldX * 0.1f, worldZ * 0.1f, 
                                2, 
                                0.3f, 
                                2.0f
                            ) * 0.15f;

                    float gray = 0.5f + noise;
                    colors[colorIdx] = gray;
                    colors[colorIdx + 1] = gray;
                    colors[colorIdx + 2] = gray;
                } else {
                    float snowHeight = (heightVal - MOUNTAIN_LEVEL) / 20.0f;
                    if(snowHeight > 1.0f) snowHeight = 1.0f;
                    
                    float baseGray = 0.6f;
                    float color = baseGray + (1.0f - baseGray) * snowHeight;
                    
                    float snowNoise = 
                        worldGenerator
                            .noiseGeneratorWrapper
                            .fractualSimplexNoise(
                                worldX * 0.08f, worldZ * 0.08f, 
                                3, 
                                0.3f, 
                                2.0f 
                            ) * 0.08f;

                    color += snowNoise;
                    
                    if(color > 1.0f) color = 1.0f;
                    if(color < baseGray) color = baseGray;
                    
                    colors[colorIdx] = color;
                    colors[colorIdx + 1] = color;
                    colors[colorIdx + 2] = color;
                }
            }
        }
        
        return colors;
    }

    /**
     * Generate Normals
     */
    private float[] generateNormals(float[] vertices, int[] indices) {
        int heightDataSize = CHUNK_SIZE + 1;
        float[] normals = new float[heightDataSize * heightDataSize * 3]; 
        
        for(int i = 0; i < heightDataSize * heightDataSize; i++) {
            int idx = i * 3;
        }
        for(int i = 0; i < normals.length; i++) {
            normals[i] = 0.0f;
        }
        for(int i = 0; i < indices.length; i += 3) {
            int idx1 = indices[i] * 3;
            int idx2 = indices[i + 1] * 3;
            int idx3 = indices[i + 2] * 3;
            
            float v1x = vertices[idx2] - vertices[idx1];
            float v1y = vertices[idx2 + 1] - vertices[idx1 + 1];
            float v1z = vertices[idx2 + 2] - vertices[idx1 + 2];
            
            float v2x = vertices[idx3] - vertices[idx1];
            float v2y = vertices[idx3 + 1] - vertices[idx1 + 1];
            float v2z = vertices[idx3 + 2] - vertices[idx1 + 2];

            float nx = v1y * v2z - v1z * v2y;
            float ny = v1z * v2x - v1x * v2z;
            float nz = v1x * v2y - v1y * v2x;
            
            float len = (float)Math.sqrt(nx * nx + ny * ny + nz * nz);
            if(len > 0) {
                nx /= len;
                ny /= len;
                nz /= len;
            }
            
            normals[idx1] += nx;
            normals[idx1 + 1] += ny;
            normals[idx1 + 2] += nz;
            
            normals[idx2] += nx;
            normals[idx2 + 1] += ny;
            normals[idx2 + 2] += nz;
            
            normals[idx3] += nx;
            normals[idx3 + 1] += ny;
            normals[idx3 + 2] += nz;
        }
        
        for(int i = 0; i < heightDataSize * heightDataSize; i++) {
            int idx = i * 3;
            float len = (float)Math.sqrt(
                normals[idx] * normals[idx] +
                normals[idx + 1] * normals[idx + 1] +
                normals[idx + 2] * normals[idx + 2]
            );
            if(len > 0) {
                normals[idx] /= len;
                normals[idx + 1] /= len;
                normals[idx + 2] /= len;
            }
        }
        
        return normals;
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

        float worldOffsetX = (chunkX * CHUNK_SIZE) - (WorldGenerator.WORLD_SIZE / 2.0f);
        float worldOffsetZ = (chunkZ * CHUNK_SIZE) - (WorldGenerator.WORLD_SIZE / 2.0f);
        int heightDataSize = CHUNK_SIZE + 1;
        
        float[] vertices = new float[heightDataSize * heightDataSize * 3];
        
        for(int x = 0; x < heightDataSize; x++) { 
            for(int z = 0; z < heightDataSize; z++) {
                int i = (x * heightDataSize + z) * 3;
                float terrainHeight = heightData[x * heightDataSize + z];
                
                vertices[i] = worldOffsetX + x;
                vertices[i+1] = terrainHeight;
                vertices[i+2] = worldOffsetZ + z;
            }
        }

        int[] indices = new int[CHUNK_SIZE * CHUNK_SIZE * 6];
        int i = 0;
        for(int x = 0; x < CHUNK_SIZE; x++) {
            for(int z = 0; z < CHUNK_SIZE; z++) {
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

        float[] colors = generateColors(heightData, chunkX, chunkZ);
        float[] normals = generateNormals(vertices, indices);

        meshData.setVertices(vertices);
        meshData.setIndices(indices);
        meshData.setColors(colors);
        meshData.setNormals(normals);

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
        int chunkSize = CHUNK_SIZE + 1;
        float worldOffsetX = (chunkX * CHUNK_SIZE) - (WorldGenerator.WORLD_SIZE) / 2.0f;
        float worldOffsetZ = (chunkZ * CHUNK_SIZE) -  (WorldGenerator.WORLD_SIZE) / 2.0f;

        return new StaticObject(
            heightData,
            chunkSize,
            chunkSize,
            getId(chunkX, chunkZ)
        ) {
            @Override
            public float getHeightAtWorld(float worldX, float worldZ) {
                int localX = (int)(worldX - worldOffsetX);
                int localZ = (int)(worldZ - worldOffsetZ);
                if(localX < 0 || localX >= chunkSize ||
                    localZ < 0 || localZ >= chunkSize
                ) {
                    return -100.0f;
                }
                return heightData[localX * chunkSize + localZ];
            }
        };
    }

    /**
     * Generate Height Data
     */
    public float[] generateHeightData(int chunkX, int chunkZ) {
        int size = CHUNK_SIZE + 1;
        float[] heightData = new float[size * size];
        
        int worldStartX = chunkX * CHUNK_SIZE;
        int worldStartZ = chunkZ * CHUNK_SIZE;
        
        for(int x = 0; x < size; x++) {
            for(int z = 0; z < size; z++) {
                float worldX = worldStartX + x;
                float worldZ = worldStartZ + z;
                heightData[x * size + z] = 
                    worldGenerator
                    .noiseGeneratorWrapper
                    .getHeightAt(
                        worldX, 
                        worldZ, 
                        WorldGenerator.WORLD_SIZE
                    );
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

            chunksToLoad.clear();
            for(int x = playerChunkX - Camera.RENDER_DISTANCE; x <= playerChunkX + Camera.RENDER_DISTANCE; x++) {
                for(int z = playerChunkZ - Camera.RENDER_DISTANCE; z <= playerChunkZ + Camera.RENDER_DISTANCE; z++) {
                    String chunkId = getId(x, z);
                    if(!loadedChunks.containsKey(chunkId) && isValid(x, z)) {
                        chunksToLoad.add(chunkId);
                    }
                }
            }

            chunksToLoad.sort((id1, id2) -> {
                String[] parts1 = id1.split("_");
                String[] parts2 = id2.split("_");
                int x1 = Integer.parseInt(parts1[1]);
                int z1 = Integer.parseInt(parts1[2]);
                int x2 = Integer.parseInt(parts2[1]);
                int z2 = Integer.parseInt(parts2[2]);
                
                float dist1 = (float)Math.sqrt(
                    Math.pow(x1 - playerChunkX, 2) + 
                    Math.pow(z1 - playerChunkZ, 2)
                );
                float dist2 = (float)Math.sqrt(
                    Math.pow(x2 - playerChunkX, 2) + 
                    Math.pow(z2 - playerChunkZ, 2)
                );
                
                return Float.compare(dist1, dist2);
            });

            lastProcessedIndex = 0;
        }
    }

    public void processChunkLoading() {
        synchronized(chunkLock) {
            int chunkLoadedThisFrame = 0;
            
            for(int i = lastProcessedIndex; i < chunksToLoad.size() && chunkLoadedThisFrame < chunksPerFrame; i++) {
                String chunkId = chunksToLoad.get(i);
                String[] parts = chunkId.split("_");

                int chunkX = Integer.parseInt(parts[1]);
                int chunkZ = Integer.parseInt(parts[2]);

                if(!loadedChunks.containsKey(chunkId) && isValid(chunkX, chunkZ)) {
                    load(chunkX, chunkZ);
                    chunkLoadedThisFrame++;
                }

                lastProcessedIndex++;
            }

            if(lastProcessedIndex >= chunksToLoad.size()) {
                chunksToLoad.clear();
                lastProcessedIndex = 0;
            }
        }
    }

    /**
     * Load
     */
    public void load(int chunkX, int chunkZ) {
        String chunkId = getId(chunkX, chunkZ);
        String waterId = Water.getId(chunkX, chunkZ);
        
        if(cachedChunks.containsKey(chunkId)) {
            ChunkData cached = cachedChunks.remove(chunkId);
            loadedChunks.put(chunkId, cached);
            render(chunkId);
            return;
        }

        if(spawner != null) {
            spawner.generate(
                chunkX, 
                chunkZ
            );
        }

        try {
            float[] chunkHeightData = generateHeightData(chunkX, chunkZ);
            MeshData chunkMeshData = createMeshData(chunkHeightData, chunkX, chunkZ);
            MeshData waterMeshData = Water.createMeshData(chunkX, chunkZ);
            StaticObject chunkCollider = createCollider(chunkHeightData, chunkX, chunkZ);

            ChunkData chunkData = new ChunkData(chunkMeshData, chunkCollider);
            loadedChunks.put(chunkId, chunkData);

            mesh.add(chunkId, chunkMeshData);
            mesh.add(waterId, waterMeshData);
            
            if(chunkCollider != null) collisionManager.addStaticCollider(chunkCollider);

            render(chunkId);
            //System.out.println("Loaded chunk: " + chunkId);
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
            String waterId = chunkId.replace("chunk_", "water_");
            
            mesh.removeMesh(chunkId);
            mesh.removeMesh(waterId);
            
            if(chunkData.collider != null) {
                collisionManager.removeCollider(chunkData.collider);
            }

            chunkData.isRendered = false;
            cachedChunks.put(chunkId, chunkData);

            if(cachedChunks.size() > 20) removeOldestCachedChunk();

            //System.out.println("Unloaded chunk: " + chunkId);
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
            if(mesh.hasMesh(chunkId)) {
                mesh.render(chunkId, 0);
            }
            
            String waterId = chunkId.replace("chunk_", "water_");
            if(mesh.hasMesh(waterId)) {
                mesh.render(waterId, 0);
            }

            if(spawner != null) {
                spawner.render();
            }
        }
    }

    /**
     * Clear
     */
    public void clear() {
        synchronized(chunkLock) {
            for(String chunkId : new ArrayList<>(loadedChunks.keySet())) {
                unload(chunkId);
            }
            loadedChunks.clear();
    
            for(String chunkId : new ArrayList<>(cachedChunks.keySet())) {
                ChunkData chunkData = cachedChunks.get(chunkId);
                if(chunkData.collider != null) {
                    collisionManager.removeCollider(chunkData.collider);
                }
                cachedChunks.remove(chunkId);
            }
            cachedChunks.clear();
    
            chunksToLoad.clear();
            lastProcessedIndex = 0;
        }
    }
}