package main.com.app.root.env.world;
import main.com.app.root.collision.types.StaticObject;
import main.com.app.root.mesh.MeshData;

public class ChunkData {
    public MeshData meshData;
    public StaticObject collider;
    public boolean isRendered;
    public long lastAccessTime;

    public ChunkData(MeshData meshData, StaticObject collider) {
        this.meshData = meshData;
        this.collider = collider;
        this.isRendered = false;
        this.lastAccessTime = System.currentTimeMillis();
    }
}
