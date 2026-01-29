package main.com.app.root.env.torch;
import main.com.app.root.Spawner;
import main.com.app.root.SpawnerData;
import main.com.app.root.SpawnerHandler;
import main.com.app.root.Tick;
import main.com.app.root.env.EnvCall;
import main.com.app.root.env.EnvController;
import main.com.app.root.env.EnvData;
import main.com.app.root.env.world.Chunk;
import main.com.app.root.env.world.Water;
import main.com.app.root.env.world.WorldGenerator;
import main.com.app.root.lightning.LightningController;
import main.com.app.root.lightning.LightningData;
import main.com.app.root.lightning.PointLight;
import main.com.app.root.mesh.Mesh;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.joml.Vector3f;

public class TorchSpawner implements SpawnerHandler {
    private final Tick tick;
    private final EnvController envController;
    private final Spawner spawner;
    private final LightningController lightningController;
    private Mesh mesh;

    public List<TorchController> torchData = new ArrayList<>();
    private Map<String, List<TorchController>> chunkTorchMap = new HashMap<>();
    private Map<String, List<PointLight>> chunkLightMap = new HashMap<>();
    private int currentTorchId = 0;

    private static final float CHUNK_LIMIT = 0.3f;
    private static final float TORCH_COVERAGE = 0.0005f;
    public static final int MAX_TORCHES_PER_CHUNK = 0;
    /*
        Math.max(1, (int)(
            Chunk.CHUNK_SIZE * 
            Chunk.CHUNK_SIZE * 
            TORCH_COVERAGE
        ));
*/
    public TorchSpawner(
        Tick tick, 
        EnvController envController,
        Spawner spawner,
        LightningController lightningController
    ) {
        this.tick = tick;
        this.envController = envController;
        this.spawner = spawner;
        this.lightningController = lightningController;

        this.torchData = new ArrayList<>();
        this.currentTorchId = 0;
    }

    @Override
    public SpawnerData getType() {
        return SpawnerData.TORCH;
    }

    @Override 
    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
    }

    public void addTorch(TorchController torch) {
        torchData.add(torch);
    }

    public List<TorchController> getTorches() {
        return torchData;
    }

    /**
     * Get chunk ID for a position
     */
    private String getChunkIdForPosition(Vector3f position) {
        int[] coords = Chunk.getCoords(position.x, position.z);
        return Chunk.getId(coords[0], coords[1]);
    }

    /**
     * Spawn Torch
     */
    private void spawnTorch(
        Vector3f position,
        int chunkX,
        int chunkZ
    ) {
        TorchController torchController = new TorchController();
        torchController.createGenerator(position, spawner.mesh, spawner);

        TorchGenerator torchGenerator = torchController.getGenerator();
        if(torchGenerator == null) return;

        String torchId = "torch_" + currentTorchId++;
        torchGenerator.setId(torchId);

        PointLight pointLight = new PointLight(
            "#ffffff",
            2.0f,
            new Vector3f(position.x, position.y, position.z),
            50.0f
        );
        torchGenerator.setLight(pointLight);

        lightningController.add(LightningData.POINT, pointLight);

        String chunkId = Chunk.getId(chunkX, chunkZ);
        chunkTorchMap.computeIfAbsent(chunkId, k -> new ArrayList<>()).add(torchController);
        chunkLightMap.computeIfAbsent(chunkId, k -> new ArrayList<>()).add(pointLight);

        torchGenerator.createMesh();
        torchData.add(torchController);
    }

    /**
     * Generate
     */
    @Override
    public void generate(int chunkX, int chunkZ) {
        String chunkId = Chunk.getId(chunkX, chunkZ);
        if(chunkTorchMap.containsKey(chunkId)) {
            List<PointLight> lights = chunkLightMap.get(chunkId);
            if(lights != null) {
                for(PointLight light : lights) {
                    if(!lightningController.getLights(LightningData.POINT).contains(light)) {
                        lightningController.add(LightningData.POINT, light);
                    }
                }
            }
            return;
        }

        Object mapInstance = envController.getEnv(EnvData.MAP).getInstance();
        Object worldGenerator = EnvCall.callReturn(mapInstance, "getGenerator");

        float worldStartX = chunkX * Chunk.CHUNK_SIZE - WorldGenerator.WORLD_SIZE / 2.0f;
        float worldStartZ = chunkZ * Chunk.CHUNK_SIZE - WorldGenerator.WORLD_SIZE / 2.0f;

        long offset = 19562L;
        Random random = Spawner.Deterministic(
            chunkX, 
            chunkZ, 
            offset,
            SpawnerData.TORCH
        );

        if(random.nextFloat() > CHUNK_LIMIT) return;
        for(int i = 0; i < MAX_TORCHES_PER_CHUNK; i++) {
            float torchX = worldStartX + random.nextFloat() * Chunk.CHUNK_SIZE;
            float torchZ = worldStartZ + random.nextFloat() * Chunk.CHUNK_SIZE;

            Object[] heightParams = new Object[]{torchX, torchZ};
            Float torchY = (Float) EnvCall.callReturnWithParams(worldGenerator, heightParams, "getHeightAt");
            
            if(torchY != null && torchY >= Water.LEVEL + 1.0f) {
                Vector3f torchPos = new Vector3f(torchX, torchY + 2.0f, torchZ);
                if(!spawner.isPositionOccupied(torchPos, chunkX, chunkZ)) {
                    spawner.registerPosition(torchPos, chunkX, chunkZ);
                    spawnTorch(torchPos, chunkX, chunkZ);
                }
            }
        }
    }

    /**
     * Unload
     */
    @Override
    public void unload(int chunkX, int chunkZ) {
        String chunkId = Chunk.getId(chunkX, chunkZ);
        
        List<TorchController> torches = chunkTorchMap.get(chunkId);
        if(torches != null) {
            for(TorchController torch : torches) {
                TorchGenerator torchGenerator = torch.getGenerator();
                if(torchGenerator != null) torchGenerator.cleanup();
                torchData.remove(torch);
            }
        }

        List<PointLight> lights = chunkLightMap.get(chunkId);
        if(lights != null) {
            for(PointLight light : lights) {
                lightningController.remove(LightningData.POINT, light);
            }
        }
    }

    /**
     * Update
     */
    @Override
    public void update() {
        for(TorchController torch : torchData) {
            Object torchGenerator = EnvCall.callReturn(torch, "getGenerator");
            if(torchGenerator == null) continue;
            
            Object[] updateParams = new Object[]{tick.getDeltaTime()};
            EnvCall.callWithParams(torchGenerator, updateParams, "update");
        }
    }

    /**
     * Render
     */
    @Override
    public void render() {
        for(TorchController torch : torchData) {
            TorchGenerator torchGenerator = torch.getGenerator();
            if(torchGenerator == null) continue;
            torchGenerator.render();
        }
    }

    /**
     * 
     * Data
     * 
     */
    @Override
    public void applyData(Map<String, Object> data) {
        if(data.containsKey("torch") && spawner != null) {
            List<Map<String, Object>> torchesData = (List<Map<String, Object>>) data.get("torch");
            
            int maxTorchId = 0;
            
            for(Map<String, Object> torchData : torchesData) {
                try {
                    float x = ((Number) torchData.get("position_x")).floatValue();
                    float y = ((Number) torchData.get("position_y")).floatValue();
                    float z = ((Number) torchData.get("position_z")).floatValue();
                    int level = ((Number) torchData.get("level")).intValue();
                    boolean alive = (Boolean) torchData.get("alive");
                    float respawnTimer = 
                        torchData.containsKey("respawn_timer") ? 
                        ((Number) torchData.get("respawn_timer")).floatValue() : 
                        0f;
                    
                    Vector3f position = new Vector3f(x, y, z);
                    
                    TorchController torchController = new TorchController();
                    torchController.createGenerator(position, spawner.mesh, spawner);
                        
                    TorchGenerator torchGenerator = torchController.getGenerator();
                    if(torchController != null) {
                        torchGenerator.mesh = spawner.mesh;
                            
                        String torchId = "torch" + currentTorchId++;
                        torchGenerator.setId(torchId);
                            
                        maxTorchId = Math.max(maxTorchId, currentTorchId);
                            
                        addTorch(torchController);
                    }
                } catch(Exception err) {
                    err.printStackTrace();
                }
            }
            currentTorchId = maxTorchId;
        }
    }

    @Override
    public void getData(Map<String, Object> data) {
        List<Map<String, Object>> torchesData = new ArrayList<>();
        for(TorchController torch : getTorches()) {
            Map<String, Object> torchEntry = new HashMap<>();
                
            try {
                TorchGenerator torchGenerator = torch.getGenerator();
                
                if(torchGenerator != null) {
                    Vector3f torchPos = torchGenerator.getPosition();
                    
                    torchEntry.put("position_x", torchPos.x);
                    torchEntry.put("position_y", torchPos.y);
                    torchEntry.put("position_z", torchPos.z);
                    torchesData.add(torchEntry);
                }
            } catch(Exception err) {
                err.printStackTrace();
            }
        }
        data.put("torch", torchesData);
    }
}
