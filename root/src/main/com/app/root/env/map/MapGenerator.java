package main.com.app.root.env.map;
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
