package main.com.app.root.env.skybox;

import org.lwjgl.opengl.GL11;

import main.com.app.root.Tick;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.mesh.MeshData;
import main.com.app.root.mesh.MeshLoader;

public class SkyboxMesh {
    private static final String SKYBOX_ID = "skybox";
    private Mesh mesh;

    public SkyboxMesh(Tick tick, ShaderProgram shaderProgram) {
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
