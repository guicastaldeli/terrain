package main.com.app.root.env.map;
import java.io.File;
import java.nio.file.Paths;

import main.com.app.root.Tick;
import main.com.app.root._resources.TextureLoader;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.mesh.MeshData;
import main.com.app.root.mesh.MeshLoader;
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
        this.mapGeneratorWrapper = new MapGeneratorWrapper();
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
     * Generate Map Data
     */
    private boolean generateMapData() {
        String noiseDir = "C:/Users/casta/OneDrive/Desktop/vscode/terrain/root/src/main/com/app/root/_resources/noise";
        File dir = new File(noiseDir);
        if(!dir.exists()) dir.mkdirs();

        String fileName = "map_" + System.currentTimeMillis() + ".bin";
        String path = Paths.get(noiseDir, fileName).toString();
        
        long seed = System.currentTimeMillis();
        boolean success = mapGeneratorWrapper.generateMap(path, seed);
        if(success) {
            heightMapData = mapGeneratorWrapper.getHeightMapData();
            mapWidth = mapGeneratorWrapper.getMapWidth();
            mapHeight = mapGeneratorWrapper.getMapHeight();
            System.out.println("Terrain generated successfully: " + path);
            System.out.println("Terrain dimensions: " + mapWidth + "x" + mapHeight);
            return true;
        } else {
            System.err.println("Failed to generate terrain");
            return false;
        }
    }

    /**
     * Create Mesh
     */
    public void createMesh() {
        if(!generateMapData()) {
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
        mesh.render(MAP_ID);
    }


    /**
     * Render
     */
    public void render() {
        generate();
        System.out.println("Map generated...");
    }
}
