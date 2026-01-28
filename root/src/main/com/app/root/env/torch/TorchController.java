package main.com.app.root.env.torch;
import main.com.app.root.DependencyValue;
import main.com.app.root.Spawner;
import main.com.app.root.Tick;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.env.EnvInstance;
import main.com.app.root.lightning.LightningController;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.mesh.MeshRenderer;
import org.joml.Vector3f;

public class TorchController implements EnvInstance<TorchController> {
    @DependencyValue private Tick tick;
    @DependencyValue private ShaderProgram shaderProgram;
    @DependencyValue private Mesh mesh;
    @DependencyValue private MeshRenderer meshRenderer;
    @DependencyValue private LightningController lightningController;
    @DependencyValue private Spawner spawner;

    private TorchGenerator torchGenerator;

    @Override
    public TorchController getInstance() {
        return this;
    }

    public void createGenerator(
        Vector3f position, 
        Mesh mesh, 
        Spawner spawner
    ) {
        this.torchGenerator = new TorchGenerator(
            position,
            mesh, 
            spawner, 
            lightningController
        );
    }

    public TorchGenerator getGenerator() {
        return torchGenerator;
    }
}
