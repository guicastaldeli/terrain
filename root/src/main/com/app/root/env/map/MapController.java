package main.com.app.root.env.map;
import main.com.app.root.DependencyValue;
import main.com.app.root.Tick;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.env.EnvInstance;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.mesh.MeshRenderer;

public class MapController implements EnvInstance<MapController> {
    @DependencyValue private Tick tick;
    @DependencyValue private ShaderProgram shaderProgram;
    @DependencyValue private Mesh mesh;
    @DependencyValue private MeshRenderer meshRenderer;

    private MapGenerator mapGenerator;

    @Override
    public MapController getInstance() {
        this.mapGenerator = new MapGenerator(
            tick, 
            mesh, 
            meshRenderer, 
            shaderProgram
        );
        return this;
    }

    /**
     * Get Map Generator
     */
    public MapGenerator getGenerator() {
        //System.out.println("GET GENERATOR");
        return mapGenerator;
    }
}
