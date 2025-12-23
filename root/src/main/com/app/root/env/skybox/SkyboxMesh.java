package main.com.app.root.env.skybox;
import main.com.app.root.Tick;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.mesh.MeshData;
import main.com.app.root.mesh.MeshLoader;
import org.lwjgl.opengl.GL11;

public class SkyboxMesh {
    private static final String SKYBOX_ID = "skybox";
    private final Tick tick;
    private final Mesh mesh;

    public SkyboxMesh(Tick tick, Mesh mesh) {
        this.tick = tick;
        this.mesh = mesh;
        setMesh();
    }

    private void setMesh() {
        MeshData data = MeshLoader.load(MeshData.MeshType.SKYBOX, SKYBOX_ID);
        if(data == null) {
            System.err.println("Failed to load terrain mesh template");
            return;
        }
        mesh.add(SKYBOX_ID, data);
    }

    public void update() {

    }

    public void render() {
        try {
            GL11.glDepthMask(false);
            mesh.render(SKYBOX_ID);
            GL11.glDepthMask(true);
        } catch(Exception err) {
            System.err.println("Skybox error!" + err.getMessage());
        }
    }
}
