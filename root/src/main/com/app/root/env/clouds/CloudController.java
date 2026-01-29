package main.com.app.root.env.clouds;
import main.com.app.root.DependencyValue;
import main.com.app.root.Spawner;
import main.com.app.root.Tick;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.env.EnvInstance;
import main.com.app.root.mesh.Mesh;

public class CloudController implements EnvInstance<CloudController> {
    @DependencyValue private Tick tick;
    @DependencyValue private Mesh mesh;
    @DependencyValue private Spawner spawner;
    @DependencyValue private ShaderProgram shaderProgram;

    private CloudSpawner cloudGenerator;

    @Override
    public CloudController getInstance() {
        if (this.cloudGenerator == null) {
            this.cloudGenerator = new CloudSpawner(
                tick, 
                mesh, 
                spawner
            );
        }
        return this;
    }

    public CloudSpawner getGenerator() {
        return cloudGenerator;
    }

    public void render() {
        if(cloudGenerator != null) {
            cloudGenerator.render();
        }
    }
}