package main.com.app.root;
import main.com.app.root.env.EnvCall;
import main.com.app.root.env.EnvController;
import main.com.app.root.env.EnvData;
import main.com.app.root.env.torch.TorchSpawner;
import main.com.app.root.env.tree.TreeSpawner;
import main.com.app.root.env.world.Water;
import main.com.app.root.lightning.LightningController;
import main.com.app.root.mesh.Mesh;
import org.joml.Vector3f;

import java.util.*;

/**
 * 
 * 
 * ------ General Spawner
 * 
 * 
 */
public class Spawner {
    private Tick tick;
    public Mesh mesh;
    private EnvController envController;

    private Vector3f centerPosition;
    private float spawnRadius;

    private Random random;
    private float spawnTimer;
    private float spawnRate = 100.0f;
    private float minSpawnDistance = 80.0f;
    private float maxSpawnDistance = 500.0f;

    public Map<SpawnerData, List<SpawnerHandler>> spawnerData;

    public Spawner(
        Tick tick,
        Mesh mesh,
        Vector3f centerPosition,
        float spawnRadius
    ) {
        this.tick = tick;
        this.mesh = mesh;

        this.centerPosition = centerPosition;
        this.spawnRadius = spawnRadius;

        this.random = new Random();
        this.spawnerData = new EnumMap<>(SpawnerData.class);
        for(SpawnerData type : SpawnerData.values()) {
            spawnerData.put(type, new ArrayList<>());
        }
    } 
    
    public void setEnvController(EnvController envController) {
        this.envController = envController;
    }

    /**
     * 
     * Register
     * 
     */
    public void registerHandler(SpawnerHandler spawnerHandler) {
        SpawnerData type = spawnerHandler.getType();
        if(spawnerData.containsKey(type)) {
            spawnerData.get(type).add(spawnerHandler);
        }
    }

    public void registerHandlers(EnvController envController, LightningController lightningController) {
        TreeSpawner treeSpawner = new TreeSpawner(tick, mesh, envController, this);
        registerHandler(treeSpawner);
        
        TorchSpawner torchSpawner = new TorchSpawner(tick, envController, this, lightningController);
        registerHandler(torchSpawner);
    }

    /**
     * 
     * Center Position
     * 
     */
    public void setCenterPosition(Vector3f newCenter) {
        this.centerPosition = newCenter;
        /*
        System.out.println("Spawner center moved to [" + 
                          newCenter.x + ", " + newCenter.z + "]");
                          */
    }

    public Vector3f getCenterPosition() {
        return new Vector3f(centerPosition);
    }

    /**
     * 
     * Spawn
     * 
     */
    public float getSpawnRadius() {
        return spawnRadius;
    }

    public void setSpawnRate(float rate) {
        this.spawnRate = Math.max(0.1f, rate);
    }

    public void setSpawnDistances(float min, float max) {
        this.minSpawnDistance = Math.max(1.0f, min);
        this.maxSpawnDistance = Math.max(minSpawnDistance + 1.0f, max);
    }

    /**
     * Get Height At
     */
    private float getHeightAt(float worldX, float worldZ) {
        Object mapInstance = envController.getEnv(EnvData.MAP).getInstance();
        Object worldGenerator = EnvCall.callReturn(mapInstance, "getGenerator");
        
        Object[] heightParams = new Object[]{worldX, worldZ};
        Float height = (Float) EnvCall.callReturnWithParams(worldGenerator, heightParams, "getHeightAt");
        
        return height != null ? height : Water.LEVEL;
    }

    public static Random Deterministic(int chunkX, int chunkZ) {
        return new Random(chunkX * 7919L + chunkZ * 131071L);
    }

    /**
     * Set Mesh
     */
    public void setMesh(Mesh mesh) {
        for(List<SpawnerHandler> handlers : spawnerData.values()) {
            for(SpawnerHandler handler : handlers) {
                handler.setMesh(mesh);
            }
        }
    }

    /**
     * Set Active
     */
    public void setActive(boolean active) {
        for(List<SpawnerHandler> handlers : spawnerData.values()) {
            for(SpawnerHandler handler : handlers) {
                handler.setActive(active);
            }
        }
    }

    /**
     * 
     * Unload
     * 
     */
    public void unload(int chunkX, int chunkZ) {
        for(List<SpawnerHandler> handlers : spawnerData.values()) {
            for(SpawnerHandler handler : handlers) {
                handler.unload(chunkX, chunkZ);
            }
        }
    }

    /**
     * 
     * Update
     * 
     */
    public void update() {
        for(List<SpawnerHandler> handlers : spawnerData.values()) {
            for(SpawnerHandler handler : handlers) {
                handler.update();
            }
        }
    }

    /**
     *
     * Generate
     * 
     */
    public void generate(int chunkX, int chunkZ) {
        for(List<SpawnerHandler> handlers : spawnerData.values()) {
            for(SpawnerHandler handler : handlers) {
                handler.generate(chunkX, chunkZ);
            }
        }
    }

    /**
     * 
     * Render
     * 
     */
    public void render() {
        for(List<SpawnerHandler> handlers : spawnerData.values()) {
            for(SpawnerHandler handler : handlers) {
                handler.render();
            }
        }
    }
}
