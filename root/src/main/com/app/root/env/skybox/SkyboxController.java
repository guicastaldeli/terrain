package main.com.app.root.env.skybox;
import main.com.app.root.DependencyValue;
import main.com.app.root.Tick;
import main.com.app.root.env.EnvInstance;
import main.com.app.root.mesh.Mesh;

public class SkyboxController implements EnvInstance<SkyboxController> {
    @DependencyValue private Tick tick;
    @DependencyValue private Mesh mesh;

    private SkyboxMesh skyboxMesh;

    @Override
    public SkyboxController getInstance() {
        this.skyboxMesh = new SkyboxMesh(tick, mesh);
        return this;
    }

    public SkyboxMesh getMesh() {
        return skyboxMesh;
    }
}
