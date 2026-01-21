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
import main.com.app.root.mesh.MeshLoader;
import main.com.app.root.mesh.MeshRenderer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Random;

public class WorldGenerator {
    private final Tick tick;
    private final ShaderProgram shaderProgram;
    private final DataController dataController;
    private final StateController stateController;
    private final CollisionManager collisionManager;
    private final Mesh mesh;
    private final MeshRenderer meshRenderer;
    private MeshData meshData;
    private StaticObject collider;

    private final NoiseGeneratorWrapper noiseGeneratorWrapper;
    private float[] heightMapData;
    private int mapWidth;
    private int mapHeight;

    private boolean isReady = false;
    private Runnable onReadyCallback;

    private final Chunk chunk;

    private static final String MAP_ID = "MAP_ID";
    private static final String TEMP_MAP_ID = "temp_map_";

    public static final int WORLD_SIZE = 10000;
    public static final int RENDER_DISTANCE = 3;
    
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
            boolean success = noiseGeneratorWrapper.generateMap(
                tempPath, 
                seed, 
                WORLD_SIZE
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
     * Load Map
     */
    public boolean loadMapData(String saveId) {
        try {
            SaveFile saveFile = new SaveFile(saveId);
            if(saveFile.hasData("world", "d.m.0")) {
                Path mapFilePath = saveFile.getSavePath()
                    .resolve("world")
                    .resolve("d.m.0.dat");
                
                boolean success = noiseGeneratorWrapper.loadMapData(mapFilePath.toString());
                
                if(success) {
                    heightMapData = noiseGeneratorWrapper.getHeightMapData();
                    mapWidth = noiseGeneratorWrapper.getMapWidth();
                    mapHeight = noiseGeneratorWrapper.getMapHeight();
                    
                    System.out.println("Map loaded from save: " + saveId);
                    System.out.println("Map dimensions: " + mapWidth + "x" + mapHeight);
                    return true;
                } else {
                    System.err.println("Native loader failed, regenerating map");
                    return generateNewMap(saveId);
                }
            } else {
                System.out.println("No map data found in save, generating new map");
                return generateNewMap(saveId);
            }
        } catch (Exception err) {
            System.err.println("Failed to load map from save: " + saveId);
            err.printStackTrace();
            return generateNewMap(saveId);
        }
    }

    /**
     * 
     * Map Data
     * 
     */
    public boolean generateData() {
        try {
            String currentSaveId = stateController.getCurrentSaveId();
            SaveFile saveFile;

            if(currentSaveId != null && !currentSaveId.isEmpty()) {
                saveFile = new SaveFile(currentSaveId);
            } else {
                System.out.println(currentSaveId);
                currentSaveId = "New World" + "_" + System.currentTimeMillis();
                saveFile = new SaveFile(currentSaveId);
            }
            if(!saveFile.exists()) {
                saveFile.createSaveDir();
            }

            return setData(saveFile);
        } catch (IOException e) {
            System.err.println("Failed to generate map data: " + e.getMessage());
            return false;
        }
    }

    private boolean setData(SaveFile saveFile) throws IOException {
        String currentSaveId = stateController.getCurrentSaveId();
        if(currentSaveId != null && stateController.isLoadInProgress()) {
            return loadMapData(currentSaveId);
        } else {
            String noiseDir = "root/src/main/com/app/root/env/_noise/data";
            File dir = new File(noiseDir);
            if(!dir.exists()) dir.mkdirs();
    
            Random r = new Random();
            int r1 = r.nextInt(9);
            int r2 = r.nextInt(9);
            String fileName = String.format(
                "m.%03d.%03d.%d.dat",
                r1,
                r2,
                System.currentTimeMillis()
            );
            String path = Paths.get(noiseDir, fileName).toString();
    
            long seed = dataController.getWorldSeed();
            boolean success = noiseGeneratorWrapper.generateMap(
                path, 
                seed,
                WORLD_SIZE
            );
            if(success) {
                heightMapData = noiseGeneratorWrapper.getHeightMapData();
                mapWidth = noiseGeneratorWrapper.getMapWidth();
                mapHeight = noiseGeneratorWrapper.getMapHeight();
                System.out.println("Map generated successfully: " + path);
                System.out.println("Map dimensions: " + mapWidth + "x" + mapHeight);
                
                Path source = Paths.get(path);
                Path target = saveFile.getSavePath().resolve("world").resolve("d.m.0.dat");
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("World map generated and saved to: " + target);
                return true;
            } else {
                throw new IOException("Failed to generate world map");
            }
        }
    }

    /**
     * Create Mesh
     */
    public void createMesh() {
        if(!generateData()) {
            System.err.println("Failed to generate terrain data");
            return;
        }
        
        meshData = MeshLoader.load(MeshData.MeshType.MAP, MAP_ID);
        if(meshData == null) {
            System.err.println("Failed to load terrain mesh template");
            return;
        }

        heightMapData = noiseGeneratorWrapper.getHeightMapData();
        mapWidth = noiseGeneratorWrapper.getMapWidth();
        mapHeight = noiseGeneratorWrapper.getMapHeight();
        
        if(heightMapData == null || heightMapData.length == 0) {
            System.err.println("heightMapData is null after generation!");
            System.err.println("Map width: " + mapWidth + ", height: " + mapHeight);
            return;
        }
    
        float[] vertices = createVertices(heightMapData);
        int[] indices = noiseGeneratorWrapper.getIndicesData();
        float[] normals = noiseGeneratorWrapper.getNormalsData();
        float[] colors = noiseGeneratorWrapper.getColorsData();

        int vertexCount = noiseGeneratorWrapper.getVertexCount();
        int indexCount = noiseGeneratorWrapper.getIndexCount();

        if(vertices != null && vertices.length > 0) meshData.setVertices(vertices);
        if(indices != null && indices.length > 0) meshData.setIndices(indices);
        if(normals != null && normals.length > 0) meshData.setNormals(normals);
        if(colors != null && colors.length > 0) meshData.setColors(colors);

        mesh.add(MAP_ID, meshData);
        createCollider(heightMapData);
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
            int localX = (int)((x + WORLD_SIZE / 2) % Chunk.CHUNK_SIZE);
            int localY = (int)((z + WORLD_SIZE / 2) % Chunk.CHUNK_SIZE);
        }

        if(heightMapData == null) {
            heightMapData = noiseGeneratorWrapper.getHeightMapData();
            if(heightMapData != null) {
                mapWidth = noiseGeneratorWrapper.getMapWidth();
                mapHeight = noiseGeneratorWrapper.getMapHeight();
                System.out.println("Retrieved from wrapper: " + heightMapData.length + " floats");
            } else {
                System.out.println("Still null even from wrapper!");
                return 0.0f;
            }
        }
        
        int mapX = (int)(x + mapWidth / 2.0f);
        int mapZ = (int)(z + mapHeight / 2.0f);
        
        mapX = Math.max(0, Math.min(mapWidth - 1, mapX));
        mapZ = Math.max(0, Math.min(mapHeight - 1, mapZ));
        
        int index = mapX * mapHeight + mapZ;
        if(index >= 0 && index < heightMapData.length) {
            return heightMapData[index];
        }
        
        System.out.println("Index out of bounds! Index =" + index + ", array length=" + heightMapData.length);
        return 0.0f;
    }

    /**
     * Render
     */
    public void setOnReadyCallback(Runnable callback) {
        this.onReadyCallback = callback;
    }

    public void render() {
        /*
        generate();
        System.out.println("Map generated...");
        addMapCollider();
        */

        int[] spawnChunk = Chunk.getCoords(0, 0);
        System.out.println("Chunk spanwned!");
        chunk.updateChunks(0, 0);
        System.out.println("Chunk updated!");
        
        isReady = true;
        if(onReadyCallback != null) onReadyCallback.run();
    }

    public void update(float playerX, float playerZ) {
        chunk.updateChunks(playerX, playerZ);
        for(String chunkId : chunk.loadedChunks.keySet()) {
            mesh.render(chunkId, 0);
        }
    }

    public boolean isReady() {
        return isReady;
    }
}
