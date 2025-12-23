package main.com.app.root.env.clouds;
import main.com.app.root.Tick;
import main.com.app.root.env.NoiseGeneratorWrapper;
import main.com.app.root.mesh.Mesh;

public class CloudGenerator {
    private final Tick tick;
    private final NoiseGeneratorWrapper noiseGeneratorWrapper;
    private final Mesh mesh;

    private static final String CLOUD_MESH_ID = "CLOUD_MESH";

    public CloudGenerator(Tick tick, Mesh mesh) {
        this.tick = tick;
        this.mesh = mesh;
        this.noiseGeneratorWrapper = new NoiseGeneratorWrapper();
    }

}