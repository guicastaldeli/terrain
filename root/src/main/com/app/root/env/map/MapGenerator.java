package main.com.app.root.env.map;
import main.com.app.root.Tick;
import main.com.app.root._resources.TextureLoader;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.mesh.MeshData;
import main.com.app.root.mesh.MeshRenderer;

public class MapGenerator {
    private final Tick tick;
    private final ShaderProgram shaderProgram;

    private static final String MAP_ID = "MAP_ID";
    private final Mesh mesh;
    private final MeshRenderer meshRenderer;
    private MeshData meshData;

    private final MapGeneratorWrapper mapGeneratorWrapper;
    private float[] heightMapData;
    private int mapWitdh;
    private int mapHeight;

    public MapGenerator(
        Tick tick, 
        Mesh mesh,
        MeshRenderer meshRenderer, 
        ShaderProgram shaderProgram
    ) {
        this.tick = tick;
        this.shaderProgram = shaderProgram;
        this.mesh = mesh;
        this.meshRenderer = meshRenderer;
        this.mapGeneratorWrapper = new MapGeneratorWrapper();
    }

    /**
     * Load Texture
     */
    private void loadTex() {
        String path = "C:/Users/casta/OneDrive/Desktop/vscode/terrain/root/src/main/com/app/root/_resources/texture/joe.png";
        int id = TextureLoader.load(path);
        if(id <= 0) {
            System.err.println("FAILED to load texture!");
            return;
        }
        
        mesh.setTex(MAP_ID, id);
    }

    public void createMesh() {
        float[] vertices = new float[mapWitdh * mapHeight * 3];
        for(int x = 0; x < mapWitdh; x++) {
            for(int z = 0; z < mapHeight; z++) {
                int i = (x * mapHeight + z) * 3;
                vertices[i] = x - mapWitdh / 2.0f;
                vertices[i+1] = heightMapData[x * mapHeight + z] + 0.1f;
                vertices[i+2] = z - mapHeight / 2.0f;
            }
        }
    }

    private void generate() {
        createMesh();
        mesh.render(MAP_ID);
    }


    public void render() {
        generate();
        System.out.println("Map generated...");
    }
}

/*
package main.com.app.root.env.map;
import main.com.app.root.Tick;
import main.com.app.root._resources.TextureLoader;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.mesh.MeshData;
import main.com.app.root.mesh.MeshLoader;
import main.com.app.root.mesh.MeshRenderer;
import main.com.app.root.env.map.noise.TerrainGeneratorJNI;

import java.io.File;
import java.nio.file.Paths;

public class MapGenerator {
    private final Tick tick;
    private final ShaderProgram shaderProgram;

    private static final String MAP_ID = "TERRAIN_MAP";
    private final Mesh mesh;
    private final MeshRenderer meshRenderer;
    private MeshData meshData;

    private final TerrainGeneratorJNI terrainGenerator;
    private float[] heightMapData;
    private int mapWidth;
    private int mapHeight;

    public MapGenerator(
        Tick tick, 
        Mesh mesh,
        MeshRenderer meshRenderer, 
        ShaderProgram shaderProgram
    ) {
        this.tick = tick;
        this.shaderProgram = shaderProgram;
        this.mesh = mesh;
        this.meshRenderer = meshRenderer;
        this.terrainGenerator = new TerrainGeneratorJNI();
        this.heightMapData = null;
        this.mapWidth = 0;
        this.mapHeight = 0;
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

   
    private boolean generateTerrainData() {
        String noiseDir = "C:/Users/casta/OneDrive/Desktop/vscode/terrain/root/src/main/com/app/root/_resources/noise";
        File dir = new File(noiseDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        String fileName = "terrain_" + System.currentTimeMillis() + ".bin";
        String fullPath = Paths.get(noiseDir, fileName).toString();
        
        long seed = System.currentTimeMillis();
        boolean success = terrainGenerator.generateTerrain(fullPath, seed);
        
        if (success) {
            heightMapData = terrainGenerator.getHeightMapData();
            mapWidth = terrainGenerator.getTerrainWidth();
            mapHeight = terrainGenerator.getTerrainHeight();
            System.out.println("Terrain generated successfully: " + fullPath);
            System.out.println("Terrain dimensions: " + mapWidth + "x" + mapHeight);
            return true;
        } else {
            System.err.println("Failed to generate terrain");
            return false;
        }
    }

   
    public void createMesh() {
        if (!generateTerrainData()) {
            System.err.println("Failed to generate terrain data");
            return;
        }
        
        // Load base terrain mesh from Lua
        meshData = MeshLoader.load(MeshData.MeshType.TERRAIN, MAP_ID);
        if (meshData == null) {
            System.err.println("Failed to load terrain mesh template");
            return;
        }
        
        // Get generated mesh data from JNI
        float[] vertices = terrainGenerator.getHeightMapData(); // This contains position data
        int[] indices = terrainGenerator.getIndicesData();
        float[] normals = terrainGenerator.getNormalsData();
        float[] colors = terrainGenerator.getColorsData();
        
        int vertexCount = terrainGenerator.getVertexCount();
        int indexCount = terrainGenerator.getIndexCount();
        
        // Override the mesh data with generated terrain data
        if (vertices != null && vertices.length > 0) {
            meshData.setVertices(createCompleteVertices(vertices));
        }
        
        if (indices != null && indices.length > 0) {
            meshData.setIndices(indices);
        }
        
        if (normals != null && normals.length > 0) {
            meshData.setNormals(normals);
        }
        
        if (colors != null && colors.length > 0) {
            meshData.setColors(colors);
        }
        
        // Add the terrain mesh to the mesh system
        mesh.add(MAP_ID, meshData);
        loadTex();
        
        System.out.println("Terrain mesh created with:");
        System.out.println("  Vertices: " + (vertices != null ? vertices.length/3 : 0));
        System.out.println("  Indices: " + (indices != null ? indices.length : 0));
        System.out.println("  Normals: " + (normals != null ? normals.length/3 : 0));
        System.out.println("  Colors: " + (colors != null ? colors.length/4 : 0));
    }

    
    private float[] createCompleteVertices(float[] heightData) {
        if (heightData == null || mapWidth <= 0 || mapHeight <= 0) {
            return new float[0];
        }
        
        float[] vertices = new float[mapWidth * mapHeight * 3];
        
        for (int x = 0; x < mapWidth; x++) {
            for (int z = 0; z < mapHeight; z++) {
                int heightIndex = x * mapHeight + z;
                int vertexIndex = heightIndex * 3;
                
                // Position vertices in world space with proper scaling
                vertices[vertexIndex] = (x - mapWidth / 2.0f) * 0.1f;     // X position
                vertices[vertexIndex + 1] = heightData[heightIndex] * 0.05f; // Y position (height)
                vertices[vertexIndex + 2] = (z - mapHeight / 2.0f) * 0.1f;  // Z position
            }
        }
        
        return vertices;
    }

    
    private void generate() {
        createMesh();
        mesh.render(MAP_ID);
    }

    
    public void render() {
        generate();
        System.out.println("Map generated and rendered...");
    }
    
    
    public int getMapWidth() {
        return mapWidth;
    }
    
    public int getMapHeight() {
        return mapHeight;
    }
    
    
    public float getHeightAt(int x, int z) {
        if (heightMapData == null || x < 0 || x >= mapWidth || z < 0 || z >= mapHeight) {
            return 0.0f;
        }
        return heightMapData[x * mapHeight + z];
    }
}
 */
