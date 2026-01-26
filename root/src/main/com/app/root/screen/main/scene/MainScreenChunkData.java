package main.com.app.root.screen.main.scene;
import main.com.app.root.mesh.MeshData;

public class MainScreenChunkData {
    public MeshData meshData;
    public boolean isRendered;
    public long lastAccessTime;

    public MainScreenChunkData(MeshData meshData) {
        this.meshData = meshData;
        this.isRendered = false;
        this.lastAccessTime = System.currentTimeMillis();
    }
}
