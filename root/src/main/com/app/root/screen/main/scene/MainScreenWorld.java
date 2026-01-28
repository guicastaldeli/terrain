package main.com.app.root.screen.main.scene;
import main.com.app.root.Tick;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.collision.types.StaticObject;
import main.com.app.root.env.NoiseGeneratorWrapper;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.mesh.MeshData;
import main.com.app.root.mesh.MeshRenderer;

public class MainScreenWorld {
    private final Tick tick;
    private final ShaderProgram shaderProgram;
    private final Mesh mesh;
    private final MeshRenderer meshRenderer;
    private final MainScreenChunk chunk;
    private MeshData meshData;
    private StaticObject collider;

    public final NoiseGeneratorWrapper noiseGeneratorWrapper;
    private float[] heightMapData;
    private int mapWidth;
    private int mapHeight;

    private boolean isReady = false;
    private Runnable onReadyCallback;

    private boolean noiseInitialized = false;
    private long currentSeed = -1;

    private static final String MAP_ID = "MAP_ID";

    public static final int WORLD_SIZE = 10000;
    public static final int DISPLAY_SIZE = 1000;
    
    public MainScreenWorld(
        Tick tick, 
        Mesh mesh,
        MeshRenderer meshRenderer, 
        ShaderProgram shaderProgram
    ) {
        this.tick = tick;
        this.mesh = mesh;
        this.meshRenderer = meshRenderer;
        this.shaderProgram = shaderProgram;
        this.noiseGeneratorWrapper = new NoiseGeneratorWrapper();
        this.chunk = new MainScreenChunk(this, mesh, meshData);
        
        long seed = System.currentTimeMillis();
        this.noiseGeneratorWrapper.initNoise(seed, WORLD_SIZE);
    }

    private float[] createVertices(float[] heightData) {
        float[] vertices = new float[mapWidth * mapHeight * 3];
        for(int x = 0; x < mapWidth; x++) {
            for(int z = 0; z < mapHeight; z++) {
                int heightIndex = x * mapHeight + z;
                int vertexIndex = heightIndex * 3;

                vertices[vertexIndex] = (x - mapWidth / 2.0f);
                vertices[vertexIndex+1] = heightData[heightIndex];
                vertices[vertexIndex+2] = (z - mapHeight / 2.0f);
            }
        }
        return vertices;
    }

    /**
     * Height Data
     */
    public float getHeightAt(float x, float z) {
        if(!noiseInitialized) {
            initNoise();
            if(!noiseInitialized) {
                System.err.println("Noise not initialized, returning default height");
                return 50.0f;
            }
        }
        
        int[] chunkCoords = MainScreenChunk.getCoords(x, z);
        String chunkId = MainScreenChunk.getId(chunkCoords[0], chunkCoords[1]);

        if(chunk.loadedChunks.containsKey(chunkId)) {
            MainScreenChunkData chunkData = chunk.loadedChunks.get(chunkId);
            if(chunkData.meshData != null) {
                int localX = (int)((x + WORLD_SIZE / 2) % MainScreenChunk.CHUNK_SIZE);
                int localZ = (int)((z + WORLD_SIZE / 2) % MainScreenChunk.CHUNK_SIZE);
                float height = getHeightFromChunkData(chunkData, localX, localZ);
                if(height > 0.0f) return height;
            }
        }

        float[] chunkHeightData = chunk.generateHeightData(chunkCoords[0], chunkCoords[1]);
        int localX = (int)((x + WORLD_SIZE / 2) % MainScreenChunk.CHUNK_SIZE);
        int localZ = (int)((z + WORLD_SIZE / 2) % MainScreenChunk.CHUNK_SIZE);

        if(chunkHeightData != null && chunkHeightData.length > 0) {
            int i = localX * MainScreenChunk.CHUNK_SIZE + localZ;
            if(i >= 0 && i < chunkHeightData.length) {
                return chunkHeightData[i];
            }
        }

        System.err.println("Failed to get height at (" + x + ", " + z + "), returning safe default");
        return 50.0f;
    }

    private float getHeightFromChunkData(
        MainScreenChunkData chunkData,
        int localX,
        int localZ
    ) {
        if(chunkData.meshData == null || chunkData.meshData.getVertices() == null) {
            return 0.0f;
        }

        int vertexIndex = (localX + localZ * MainScreenChunk.CHUNK_SIZE) * 3;
        float[] vertices = chunkData.meshData.getVertices();
        if(vertexIndex >= 0 && vertexIndex + 1 < vertices.length) {
            return vertices[vertexIndex + 1];
        }

        return 0.0f;
    }

    /**
     * Render
     */
    public void setOnReadyCallback(Runnable callback) {
        this.onReadyCallback = callback;
    }

    public void render(float cameraX, float cameraZ) {
        chunk.updateChunks(cameraX, cameraZ);
        chunk.processChunkLoading();
        
        for(String chunkId : chunk.loadedChunks.keySet()) {
            mesh.render(chunkId, 0);
        }

        isReady = true;
        if(onReadyCallback != null) onReadyCallback.run();
    }

    /**
     * Update
     */
    public void update(float cameraX, float cameraZ) {        
        chunk.updateChunks(cameraX, cameraZ);
        chunk.processChunkLoading();
        for(String chunkId : chunk.loadedChunks.keySet()) {
            mesh.render(chunkId, 0);
        }
    }

    /**
     * Reset Seed
     */
    public void resetSeed(long newSeed) {
        currentSeed = newSeed;
        
        if(chunk != null) {
            chunk.clear();
        }

        isReady = false;
        
        if(noiseGeneratorWrapper != null) {
            try {
                noiseGeneratorWrapper.reset();
                noiseInitialized = false;
                if(!noiseGeneratorWrapper.initNoise(currentSeed, WORLD_SIZE)) {
                    System.err.println("Failed to reinitialize noise system after reset");
                } else {
                    noiseInitialized = true;
                    System.out.println("WorldGenerator reinitialized with seed: " + currentSeed);
                }
            } catch(Exception err) {
                err.printStackTrace();
            }
        }
    }

    public boolean isReady() {
        return isReady;
    }

    public MainScreenChunk getChunk() {
        return chunk;
    }

    public NoiseGeneratorWrapper getNoiseGeneratorWrapper() {
        return noiseGeneratorWrapper;
    }

    /**
     * Init Noise
     */
    public void initNoise() {
        if(!noiseInitialized && noiseGeneratorWrapper != null) {
            if(!noiseGeneratorWrapper.initNoise(currentSeed, WORLD_SIZE)) {
                System.err.println("Failed to initialize noise system");
            } else {
                noiseInitialized = true;
                System.out.println("Noise system initialized with seed " + currentSeed);
            }
        }
    }

    public void waitUntilReady() {
        int maxWaitAttempts = 100;
        int attempts = 0;
        while(attempts < maxWaitAttempts && !isReady) {
            try {
                Thread.sleep(50);
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            attempts++;
        }
    }
}