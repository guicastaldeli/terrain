package main.com.app.root.env.world;
import main.com.app.root.DataController;
import main.com.app.root.DependencyValue;
import main.com.app.root.Spawner;
import main.com.app.root.StateController;
import main.com.app.root.Tick;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.collision.CollisionManager;
import main.com.app.root.env.EnvInstance;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.mesh.MeshRenderer;

public class WorldController implements EnvInstance<WorldController> {
    @DependencyValue private Tick tick;
    @DependencyValue private ShaderProgram shaderProgram;
    @DependencyValue private Mesh mesh;
    @DependencyValue private MeshRenderer meshRenderer;
    @DependencyValue private DataController dataController;
    @DependencyValue private StateController stateController;
    @DependencyValue private CollisionManager collisionManager;
    @DependencyValue private Spawner spawner;

    private WorldGenerator worldGenerator;

    @Override
    public WorldController getInstance() {
        if(worldGenerator == null) createGenerator();
        return this;
    }

    private void createGenerator() {
        this.worldGenerator = new WorldGenerator(
            tick, 
            mesh, 
            meshRenderer, 
            shaderProgram,
            dataController,
            stateController,
            collisionManager,
            spawner
        );
    }

    /**
     * Get Map Generator
     */
    public WorldGenerator getGenerator() {
        return worldGenerator;
    }

    public void initNoiseWithSeed(long seed) {
        if(worldGenerator == null) createGenerator();
        worldGenerator.resetSeed(seed);
        worldGenerator.initNoise();
    }
}
