package main.com.app.root.env.map;
import main.com.app.root.DataController;
import main.com.app.root.StateController;
import main.com.app.root.Tick;
import main.com.app.root._resources.TextureLoader;
import main.com.app.root._save.SaveFile;
import main.com.app.root._shaders.ShaderProgram;
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

public class MapGenerator {
    private final Tick tick;
    private final ShaderProgram shaderProgram;

    private static final String MAP_ID = "MAP_ID";
    private static final String TEMP_MAP_ID = "temp_map_";
    private final Mesh mesh;
    private final MeshRenderer meshRenderer;
    private MeshData meshData;

    private final MapGeneratorWrapper mapGeneratorWrapper;
    private float[] heightMapData;
    private int mapWidth;
    private int mapHeight;

    private final DataController dataController;
    private final StateController stateController;

    public MapGenerator(
        Tick tick, 
        Mesh mesh,
        MeshRenderer meshRenderer, 
        ShaderProgram shaderProgram,
        DataController dataController,
        StateController stateController
    ) {
        this.tick = tick;
        this.shaderProgram = shaderProgram;
        this.mesh = mesh;
        this.meshRenderer = meshRenderer;
        this.mapGeneratorWrapper = new MapGeneratorWrapper();
        this.dataController = dataController;
        this.stateController = stateController;
    }

    private void loadTex() {
        String path = "C:/Users/casta/OneDrive/Desktop/vscode/terrain/root/src/main/com/app/root/_resources/texture/joe.png";
        int id = TextureLoader.load(path);
        if(id <= 0) {
            System.err.println("FAILED to load texture!");
            return;
        }
        
        mesh.setTex(MAP_ID, id);
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
            boolean success = mapGeneratorWrapper.generateMap(tempPath, seed);
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
                byte[] mapData = saveFile.loadData("world", "d.m.0");
                
                System.out.println("Map loaded from save: " + saveId);
                return true;
            } else {
                System.out.println("No map data found in save, generating new map");
                return generateNewMap(saveId);
            }
        } catch (Exception err) {
            System.err.println("Failed to load map from save: " + saveId);
            err.printStackTrace();
            return false;
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
            String noiseDir = "root/src/main/com/app/root/env/map/noise/data";
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
            boolean success = mapGeneratorWrapper.generateMap(path, seed);
            if(success) {
                heightMapData = mapGeneratorWrapper.getHeightMapData();
                mapWidth = mapGeneratorWrapper.getMapWidth();
                mapHeight = mapGeneratorWrapper.getMapHeight();
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

        float[] heightData = mapGeneratorWrapper.getHeightMapData();
        float[] vertices = createVertices(heightData);
        int[] indices = mapGeneratorWrapper.getIndicesData();
        float[] normals = mapGeneratorWrapper.getNormalsData();
        float[] colors = mapGeneratorWrapper.getColorsData();

        int vertexCount = mapGeneratorWrapper.getVertexCount();
        int indexCount = mapGeneratorWrapper.getIndexCount();

        if(vertices != null && vertices.length > 0) meshData.setVertices(vertices);
        if(indices != null && indices.length > 0) meshData.setIndices(indices);
        if(normals != null && normals.length > 0) meshData.setNormals(normals);
        if(colors != null && colors.length > 0) meshData.setColors(colors);

        mesh.add(MAP_ID, meshData);
        //loadTex();
    }

    /**
     * Generate
     */
    private void generate() {
        createMesh();
        mesh.render(MAP_ID, 0);
    }


    /**
     * Render
     */
    public void render() {
        generate();
        System.out.println("Map generated...");
    }
}
