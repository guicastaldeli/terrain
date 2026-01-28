package main.com.app.root;
import java.util.Map;

import main.com.app.root.mesh.Mesh;

public interface SpawnerHandler {
    SpawnerData getType();
    void setMesh(Mesh mesh);

    default void setActive(boolean active) {};
    default boolean isActive() {
        return true;
    }

    void applyData(Map<String, Object> data);
    void getData(Map<String, Object> data);

    void generate(int chunkX, int chunkZ);
    void unload(int chunkX, int chunkZ);
    void update();
    void render();   
}
