package main.com.app.root.env.world;
import main.com.app.root.Spawner;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.collision.CollisionManager;
import main.com.app.root.collision.types.StaticObject;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.mesh.MeshData;
import main.com.app.root.mesh.MeshLoader;
import main.com.app.root.player.Camera;
import main.com.app.root.utils.WorldTexture;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.*;

public class Chunk {
    private final WorldGenerator worldGenerator;
    private final CollisionManager collisionManager;
    private final Spawner spawner;
    private final Mesh mesh;
    private final ShaderProgram shaderProgram;
    private MeshData meshData;

    public final ReadWriteLock chunkLock = new ReentrantReadWriteLock();
    
    public Map<ChunkKey, ChunkData> loadedChunks = new HashMap<>();
    public Map<ChunkKey, ChunkData> cachedChunks = new LinkedHashMap<ChunkKey, ChunkData>(50, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<ChunkKey, ChunkData> eldest) {
            if(size() > 50) {
                ChunkData data = eldest.getValue();
                if(data != null && data.collider != null) {
                    collisionManager.removeCollider(data.collider);
                }
                return true;
            }
            return false;
        }
    };
    
    private List<ChunkKey> chunksToLoad = new ArrayList<>();
    private List<ChunkKey> chunksToUnloadPool = new ArrayList<>();
    private int chunksPerFrame = 1;
    private int lastProcessedIndex = 0;
    private static final long MIN_TIME_BETWEEN_CHUNKS = 16;

    public static final int CHUNK_SIZE = 50;

    public final Water water;

    public static class ChunkKey {
        public final int x;
        public final int z;
        private final int hash;
        
        public ChunkKey(int x, int z) {
            this.x = x;
            this.z = z;
            this.hash = 31 * x + z;
        }
        
        @Override
        public int hashCode() {
            return hash;
        }
        
        @Override
        public boolean equals(Object obj) {
            if(this == obj) return true;
            if(!(obj instanceof ChunkKey)) return false;
            ChunkKey other = (ChunkKey) obj;
            return x == other.x && z == other.z;
        }
        
        @Override
        public String toString() {
            return "chunk_" + x + "_" + z;
        }
    }

    public Chunk(
        WorldGenerator worldGenerator, 
        CollisionManager collisionManager,
        Mesh mesh,
        MeshData meshData,
        Spawner spawner,
        ShaderProgram shaderProgram
    ) {
        Map<String, Integer> loadedTex = WorldTexture.load();
        WorldTexture.setWorldTextures(loadedTex);

        this.worldGenerator = worldGenerator;
        this.collisionManager = collisionManager;
        this.mesh = mesh;
        this.meshData = meshData;
        this.spawner = spawner;
        this.shaderProgram = shaderProgram;

        this.water = new Water(mesh, shaderProgram);
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
     * Get Key
     */
    public static ChunkKey getKey(int chunkX, int chunkZ) {
        return new ChunkKey(chunkX, chunkZ);
    }

    /**
     * Get Id
     */
    public static String getId(int chunkX, int chunkZ) {
        return "chunk_" + chunkX + "_" + chunkZ;
    }

    public boolean isInRange(ChunkKey key, int centerX, int centerZ) {
        return Math.abs(key.x - centerX) <= Camera.RENDER_DISTANCE &&
               Math.abs(key.z - centerZ) <= Camera.RENDER_DISTANCE;
    }

    public boolean isValid(int chunkX, int chunkZ) {
        int maxChunks = WorldGenerator.WORLD_SIZE / CHUNK_SIZE;
        return chunkX >= 0 && chunkX < maxChunks &&
               chunkZ >= 0 && chunkZ < maxChunks;
    }

    /**
     * Generate Textures
     */
    private float[] generateTexture(float[] heightData, int chunkX, int chunkZ) {
        int heightDataSize = CHUNK_SIZE + 1;
        float[] blends = new float[heightDataSize * heightDataSize * 5];
        
        float BEACH_LEVEL = 60.0f;
        float GRASS_LEVEL = 65.0f;
        float MOUNTAIN_LEVEL = 250.0f;
        float BLEND_RANGE = 10.0f;
        
        for(int x = 0; x < heightDataSize; x++) {
            for(int z = 0; z < heightDataSize; z++) {
                int i = x * heightDataSize + z;
                int blendIdx = i * 5;
                
                float heightVal = heightData[i];
                
                for(int t = 0; t < 5; t++) {
                    blends[blendIdx + t] = 0.0f;
                }
                
                if(heightVal < BEACH_LEVEL) {
                    blends[blendIdx + 1] = 1.0f;
                } else if(heightVal < GRASS_LEVEL) {
                    float t = (heightVal - BEACH_LEVEL) / (GRASS_LEVEL - BEACH_LEVEL);
                    blends[blendIdx + 1] = 1.0f - t;
                    blends[blendIdx + 2] = t;
                } else if(heightVal < MOUNTAIN_LEVEL) {

                    float grassEnd = GRASS_LEVEL + BLEND_RANGE;
                    if(heightVal < grassEnd) {
                        float t = (heightVal - GRASS_LEVEL) / BLEND_RANGE;
                        blends[blendIdx + 2] = 1.0f - t;
                        blends[blendIdx + 3] = t;
                    } else {
                        blends[blendIdx + 3] = 1.0f;
                    }
                } else {
                    float t = Math.min((heightVal - MOUNTAIN_LEVEL) / 20.0f, 1.0f);
                    blends[blendIdx + 3] = 1.0f - t;
                    blends[blendIdx + 4] = t;
                }
            }
        }
        
        return blends;
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
        
        for(int i = 0; i < normals.length; i++) {
            normals[i] = 0.0f;
        }
        for(int i = 0; i < indices.length; i += 3) {
            int idx1 = indices[i] * 3;
            int idx2 = indices[i + 1] * 3;
            int idx3 = indices[i + 2] * 3;
            
            float v1x = vertices[idx1];
            float v1y = vertices[idx1 + 1];
            float v1z = vertices[idx1 + 2];
            
            float v2x = vertices[idx2];
            float v2y = vertices[idx2 + 1];
            float v2z = vertices[idx2 + 2];
            
            float v3x = vertices[idx3];
            float v3y = vertices[idx3 + 1];
            float v3z = vertices[idx3 + 2];
            
            float edge1x = v2x - v1x;
            float edge1y = v2y - v1y;
            float edge1z = v2z - v1z;
            
            float edge2x = v3x - v1x;
            float edge2y = v3y - v1y;
            float edge2z = v3z - v1z;

            float nx = edge1y * edge2z - edge1z * edge2y;
            float ny = edge1z * edge2x - edge1x * edge2z;
            float nz = edge1x * edge2y - edge1y * edge2x;
            
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
        meshData = MeshLoader.load(MeshData.MeshType.WORLD, getId(chunkX, chunkZ));

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

        float[] normals = generateNormals(vertices, indices);
        float[] uvs = WorldTexture.generateUVs(chunkX, chunkZ);
        float[] texBlends = generateTexture(heightData, chunkX, chunkZ);
        if(!WorldTexture.hasWorldTextures) {
            float[] colors = generateColors(heightData, chunkX, chunkZ);
            meshData.setColors(colors);
        }

        meshData.setVertices(vertices);
        meshData.setIndices(indices);
        meshData.setNormals(normals);
        meshData.setTexCoords(uvs);
        meshData.setTexBlends(texBlends);

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
     * ***TEST FLAT MAP BELOW
     */
    private void testFlatMap(float[] heightData) {
        Arrays.fill(heightData, 100.0f);
    }

    /**
     * Generate Height Data
     */
    public float[] generateHeightData(int chunkX, int chunkZ) {
        int size = CHUNK_SIZE + 1;
        float[] heightData = new float[size * size];
        
        //testFlatMap(heightData);
        
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

        chunksToUnloadPool.clear();
        chunkLock.readLock().lock();
        try {
            for(ChunkKey key : loadedChunks.keySet()) {
                if(!isInRange(key, playerChunkX, playerChunkZ)) {
                    chunksToUnloadPool.add(key);
                }
            }
        } finally {
            chunkLock.readLock().unlock();
        }

        if(!chunksToUnloadPool.isEmpty()) {
            chunkLock.writeLock().lock();
            try {
                for(ChunkKey key : chunksToUnloadPool) {
                    unload(key);
                }
            } finally {
                chunkLock.writeLock().unlock();
            }
        }

        chunksToLoad.clear();
        for(int x = playerChunkX - Camera.RENDER_DISTANCE; x <= playerChunkX + Camera.RENDER_DISTANCE; x++) {
            for(int z = playerChunkZ - Camera.RENDER_DISTANCE; z <= playerChunkZ + Camera.RENDER_DISTANCE; z++) {
                if(isValid(x, z)) {
                    ChunkKey key = new ChunkKey(x, z);
                    
                    chunkLock.readLock().lock();
                    boolean needsLoad = !loadedChunks.containsKey(key);
                    chunkLock.readLock().unlock();
                    
                    if(needsLoad) {
                        chunksToLoad.add(key);
                    }
                }
            }
        }

        chunksToLoad.sort((key1, key2) -> {
            int dx1 = key1.x - playerChunkX;
            int dz1 = key1.z - playerChunkZ;
            int dx2 = key2.x - playerChunkX;
            int dz2 = key2.z - playerChunkZ;
            
            int dist1Sq = dx1 * dx1 + dz1 * dz1;
            int dist2Sq = dx2 * dx2 + dz2 * dz2;
            
            return Integer.compare(dist1Sq, dist2Sq);
        });

        lastProcessedIndex = 0;
    }

    public void processChunkLoading() {
        chunkLock.writeLock().lock();
        try {
            int chunkLoadedThisFrame = 0;
            
            for(int i = lastProcessedIndex; i < chunksToLoad.size() && chunkLoadedThisFrame < chunksPerFrame; i++) {
                ChunkKey key = chunksToLoad.get(i);

                if(!loadedChunks.containsKey(key) && isValid(key.x, key.z)) {
                    load(key.x, key.z);
                    chunkLoadedThisFrame++;
                }

                lastProcessedIndex++;
            }

            if(lastProcessedIndex >= chunksToLoad.size()) {
                chunksToLoad.clear();
                lastProcessedIndex = 0;
            }
        } finally {
            chunkLock.writeLock().unlock();
        }
    }

    /**
     * Load
     */
    public void load(int chunkX, int chunkZ) {
        ChunkKey key = new ChunkKey(chunkX, chunkZ);
        String chunkId = getId(chunkX, chunkZ);
        String waterId = Water.getId(chunkX, chunkZ);
        
        if(cachedChunks.containsKey(key)) {
            ChunkData cached = cachedChunks.remove(key);
            loadedChunks.put(key, cached);
            
            if(cached.meshData != null) {
                mesh.add(chunkId, cached.meshData);
            }

            MeshData waterMeshData = Water.createMeshData(chunkX, chunkZ);
            mesh.add(waterId, waterMeshData);
            water.loadTex(chunkX, chunkZ);
            
            if(cached.collider != null) {
                collisionManager.addStaticCollider(cached.collider);
            }
            
            render(key);
            return;
        }

        if(spawner != null) {
            spawner.generate(chunkX, chunkZ);
        }

        try {
            float[] chunkHeightData = generateHeightData(chunkX, chunkZ);
            MeshData chunkMeshData = createMeshData(chunkHeightData, chunkX, chunkZ);
            MeshData waterMeshData = Water.createMeshData(chunkX, chunkZ);
            
            mesh.add(chunkId, chunkMeshData);
            mesh.add(waterId, waterMeshData);
            water.loadTex(chunkX, chunkZ);
            
            StaticObject chunkCollider = createCollider(chunkHeightData, chunkX, chunkZ);

            ChunkData chunkData = new ChunkData(chunkMeshData, chunkCollider);
            loadedChunks.put(key, chunkData);
            
            if(chunkCollider != null) collisionManager.addStaticCollider(chunkCollider);

            render(key);
        } catch(Exception err) {
            System.err.println("Failed to load chunk " + chunkId + ": " + err.getMessage());
            err.printStackTrace();
        }
    }

    /**
     * Unload
     */
    public void unload(ChunkKey key) {
        ChunkData chunkData = loadedChunks.remove(key);
        if(chunkData != null) {
            String chunkId = key.toString();
            String waterId = chunkId.replace("chunk_", "water_");
            
            mesh.remove(chunkId);
            mesh.remove(waterId);
            
            if(chunkData.collider != null) {
                collisionManager.removeCollider(chunkData.collider);
            }
            
            if(spawner != null) {
                spawner.unload(key.x, key.z);
            }

            chunkData.isRendered = false;
            cachedChunks.put(key, chunkData);
            //System.out.println("Unloaded chunk: " + chunkId);
        }
    }

    /**
     * Render
     */
    public void render(ChunkKey key) {
        ChunkData chunkData = loadedChunks.get(key);
        if(chunkData != null) {
            chunkData.isRendered = true;
            chunkData.lastAccessTime = System.currentTimeMillis();
            
            String chunkId = key.toString();
            if(mesh.hasMesh(chunkId)) {
                mesh.render(chunkId, 0);
            }
            
            String waterId = chunkId.replace("chunk_", "water_");
            if(mesh.hasMesh(waterId)) {
                mesh.render(waterId, 0);
            }
        }
    }

    /**
     * Clear
     */
    public void clear() {
        chunkLock.writeLock().lock();
        try {
            for(ChunkKey key : new ArrayList<>(loadedChunks.keySet())) {
                unload(key);
            }
            loadedChunks.clear();
            for(Map.Entry<ChunkKey, ChunkData> entry : cachedChunks.entrySet()) {
                ChunkData chunkData = entry.getValue();
                if(chunkData != null) {
                    String chunkId = entry.getKey().toString();
                    String waterId = chunkId.replace("chunk_", "water_");
                    mesh.remove(chunkId);
                    mesh.remove(waterId);
                    if(chunkData.collider != null) {
                        collisionManager.removeCollider(chunkData.collider);
                    }
                }
            }
            cachedChunks.clear();

            chunksToLoad.clear();
            chunksToUnloadPool.clear();
            lastProcessedIndex = 0;
        } finally {
            chunkLock.writeLock().unlock();
        }
    }
}