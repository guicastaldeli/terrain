package main.com.app.root.screen.main.scene;
import main.com.app.root.mesh.MeshData;

public class ChunkData {
    public MeshData meshData;
    public boolean isRendered;
    public long lastAccessTime;

    public ChunkData(MeshData meshData) {
        this.meshData = meshData;
        this.isRendered = false;
        this.lastAccessTime = System.currentTimeMillis();
    }
}
