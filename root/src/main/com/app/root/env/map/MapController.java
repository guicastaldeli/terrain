package main.com.app.root.env.map;
import main.com.app.root.DataController;
import main.com.app.root.DependencyValue;
import main.com.app.root.StateController;
import main.com.app.root.Tick;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.collision.CollisionManager;
import main.com.app.root.env.EnvInstance;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.mesh.MeshRenderer;

public class MapController implements EnvInstance<MapController> {
    @DependencyValue private Tick tick;
    @DependencyValue private ShaderProgram shaderProgram;
    @DependencyValue private Mesh mesh;
    @DependencyValue private MeshRenderer meshRenderer;
    @DependencyValue private DataController dataController;
    @DependencyValue private StateController stateController;
    @DependencyValue private CollisionManager collisionManager;

    private MapGenerator mapGenerator;

    @Override
    public MapController getInstance() {
        if(mapGenerator == null) createGenerator();
        return this;
    }

    private void createGenerator() {
        this.mapGenerator = new MapGenerator(
            tick, 
            mesh, 
            meshRenderer, 
            shaderProgram,
            dataController,
            stateController,
            collisionManager
        );
    }

    /**
     * Get Map Generator
     */
    public MapGenerator getGenerator() {
        //System.out.println("GET GENERATOR");
        return mapGenerator;
    }
}
