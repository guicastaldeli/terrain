package main.com.app.root.env.clouds;
import main.com.app.root.DependencyValue;
import main.com.app.root.Tick;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.env.EnvInstance;
import main.com.app.root.mesh.Mesh;

public class CloudController implements EnvInstance<CloudController> {
    @DependencyValue private Tick tick;
    @DependencyValue private ShaderProgram shaderProgram;
    @DependencyValue private Mesh mesh;

    private CloudGenerator cloudGenerator;

    @Override
    public CloudController getInstance() {
        if (this.cloudGenerator == null) {
            this.cloudGenerator = new CloudGenerator(tick, mesh);
        }
        return this;
    }

    public CloudGenerator getGenerator() {
        return cloudGenerator;
    }

    public void setSeed(long seed) {
        if(cloudGenerator != null) {
            cloudGenerator.setSeed(seed);
        }
    }

    public void render() {
        if(cloudGenerator != null) {
            cloudGenerator.render();
        }
    }
}