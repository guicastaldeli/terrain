package main.com.app.root.env.world;
import main.com.app.root.DataController;
import main.com.app.root.StateController;
import main.com.app.root.Tick;
import main.com.app.root._save.SaveFile;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.collision.CollisionManager;
import main.com.app.root.collision.types.StaticObject;
import main.com.app.root.env.NoiseGeneratorWrapper;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.mesh.MeshData;
import main.com.app.root.mesh.MeshRenderer;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WorldGenerator {
    private final Tick tick;
    private final ShaderProgram shaderProgram;
    public final DataController dataController;
    private final StateController stateController;
    private final CollisionManager collisionManager;
    private final Mesh mesh;
    private final MeshRenderer meshRenderer;
    private MeshData meshData;
    private StaticObject collider;

    public final NoiseGeneratorWrapper noiseGeneratorWrapper;
    private float[] heightMapData;
    private int mapWidth;
    private int mapHeight;

    private boolean isReady = false;
    private Runnable onReadyCallback;

    private final Chunk chunk;

    private static final String MAP_ID = "MAP_ID";
    private static final String TEMP_MAP_ID = "temp_map_";

    public static final int WORLD_SIZE = 20000;
    public static final int RENDER_DISTANCE = 8;
    
    public WorldGenerator(
        Tick tick, 
        Mesh mesh,
        MeshRenderer meshRenderer, 
        ShaderProgram shaderProgram,
        DataController dataController,
        StateController stateController,
        CollisionManager collisionManager
    ) {
        this.tick = tick;
        this.shaderProgram = shaderProgram;
        this.mesh = mesh;
        this.meshRenderer = meshRenderer;
        this.noiseGeneratorWrapper = new NoiseGeneratorWrapper();
        this.dataController = dataController;
        this.stateController = stateController;
        this.collisionManager = collisionManager;
        this.chunk = new Chunk(
            this, 
            collisionManager, 
            mesh, 
            null
        );
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
     * Generate New Map
     */
    private boolean generateNewMap(String saveId) {
        try {
            SaveFile saveFile = new SaveFile(saveId);
            long seed = dataController.getWorldSeed();

            String tempPath = "temp_map_" + System.currentTimeMillis() + ".dat";
            boolean success = noiseGeneratorWrapper.generateMapMetadata(
                tempPath, 
                seed, 
                WORLD_SIZE,
                Chunk.CHUNK_SIZE
            );
            if(success) {
                Path source = Paths.get(tempPath);
                Path target = saveFile.getSavePath().resolve("world").resolve("d.m.0.dat");
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                Files.deleteIfExists(source);

                System.out.println("New map generated for save: " + saveId);
                return true;
            }

            return false;
        } catch (Exception err) {
            System.err.println("Failed to generate new map for save: " + saveId);
            err.printStackTrace();
            return false;
        }
    }

    /**
     * Collision
     */
    private void createCollider(float[] heightData) {
        int width = noiseGeneratorWrapper.getMapWidth();
        int height = noiseGeneratorWrapper.getMapHeight();
        collider = new StaticObject(heightData, width, height, MAP_ID);
    }

    public StaticObject getCollider() {
        return collider;
    }

    public void addMapCollider() {
        try {
            StaticObject coll = (StaticObject) collider;
            if(coll != null) {
                collisionManager.addStaticCollider(coll);
                System.out.println("Map collider added to collision system");
            }
        } catch (Exception err) {
            System.err.println("Failed to add map collider: " + err.getMessage());
        }
    }

    public float getHeightAt(float x, float z) {
        int[] chunkCoords = Chunk.getCoords(x, z);
        String chunkId = Chunk.getId(chunkCoords[0], chunkCoords[1]);
        if(chunk.loadedChunks.containsKey(chunkId)) {
            ChunkData chunkData = chunk.loadedChunks.get(chunkId);
            if(chunkData.meshData != null) {
                int localX = (int)((x + WORLD_SIZE / 2) % Chunk.CHUNK_SIZE);
                int localZ = (int)((z + WORLD_SIZE / 2) % Chunk.CHUNK_SIZE);
                return getHeightFromChunkData(chunkData, localX, localZ);
            }
        }

        float[] chunkHeightData = chunk.generateHeightData(chunkCoords[0], chunkCoords[1]);
        int localX = (int)((x + WORLD_SIZE / 2) % Chunk.CHUNK_SIZE);
        int localZ = (int)((z + WORLD_SIZE / 2) % Chunk.CHUNK_SIZE);

        if(chunkHeightData != null && chunkHeightData.length > 0) {
            int i = localX * Chunk.CHUNK_SIZE + localZ;
            if(i >= 0 && i < chunkHeightData.length) {
                return chunkHeightData[i];
            }
        }

        return 0.0f;
    }

    private float getHeightFromChunkData(
        ChunkData chunkData,
        int localX,
        int localZ
    ) {
        if(chunkData.meshData == null || chunkData.meshData.getVertices() == null) {
            return 0.0f;
        }

        int vertexIndex = (localX + localZ * Chunk.CHUNK_SIZE) * 3;
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

    public void render(float playerX, float playerZ) {
        chunk.updateChunks(playerX, playerZ);
        chunk.processChunkLoading();
        
        for(String chunkId : chunk.loadedChunks.keySet()) {
            mesh.render(chunkId, 0);
        }

        isReady = true;
        if(onReadyCallback != null) onReadyCallback.run();
    }

    public void update(float playerX, float playerZ) {
        chunk.updateChunks(playerX, playerZ);
        chunk.processChunkLoading();
        for(String chunkId : chunk.loadedChunks.keySet()) {
            mesh.render(chunkId, 0);
        }
    }

    public boolean isReady() {
        return isReady;
    }
}