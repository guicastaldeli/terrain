package main.com.app.root.env.torch;
import main.com.app.root.Spawner;
import main.com.app.root.SpawnerData;
import main.com.app.root.SpawnerHandler;
import main.com.app.root.Tick;
import main.com.app.root.env.EnvCall;
import main.com.app.root.env.EnvController;
import main.com.app.root.env.EnvData;
import main.com.app.root.env.tree.TreeController;
import main.com.app.root.env.world.Chunk;
import main.com.app.root.env.world.Water;
import main.com.app.root.env.world.WorldGenerator;
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
    private final Mesh mesh;
    private final EnvController envController;
    private final Spawner spawner;

    public List<TorchController> torchData = new ArrayList<>();
    private Map<String, List<TreeController>> chunkTorchMap = new HashMap<>();

    private static final float TORCH_COVERAGE = 0.0003f;
    public static final int MAX_TORCHES_PER_CHUNK = (int)(Chunk.CHUNK_SIZE * Chunk.CHUNK_SIZE * TORCH_COVERAGE);

    public TorchSpawner(
        Tick tick, 
        Mesh mesh,
        EnvController envController,
        Spawner spawner
    ) {
        this.tick = tick;
        this.mesh = mesh;
        this.envController = envController;
        this.spawner = spawner;

        this.torchData = new ArrayList<>();
    }

    @Override
    public SpawnerData getType() {
        return SpawnerData.TORCH;
    }

    /**
     * Spawn Torch
     */
    private void spawnTorch(Vector3f position) {
        TorchController torchController = new TorchController();
        torchController.createGenerator(position, mesh, this);

        TorchGenerator torchGenerator = torchController.getGenerator();
        if(torchGenerator == null) return;

        torchGenerator.mesh = this.mesh;
        String torchId = "torch_" + torchData.size();
        torchGenerator.setId(torchId);

        PointLight pointLight = new PointLight(
            "#ff6600",
            2.0f,
            position,
            15.0f
        );
        torchGenerator.setLight(pointLight);

        torchGenerator.createMesh();
        torchData.add(torchController);
    }

    /**
     * Generate
     */
    @Override
    public void generate(int chunkX, int chunkZ) {
        Object mapInstance = envController.getEnv(EnvData.MAP).getInstance();
        Object worldGenerator = EnvCall.callReturn(mapInstance, "getGenerator");

        float worldStartX = chunkX * Chunk.CHUNK_SIZE - WorldGenerator.WORLD_SIZE / 2.0f;
        float worldStartZ = chunkZ * Chunk.CHUNK_SIZE - WorldGenerator.WORLD_SIZE / 2.0f;

        Random random = Spawner.Deterministic(chunkX, chunkZ);
        
        for(int i = 0; i < MAX_TORCHES_PER_CHUNK; i++) {
            float torchX = worldStartX + random.nextFloat() * Chunk.CHUNK_SIZE;
            float torchZ = worldStartZ + random.nextFloat() * Chunk.CHUNK_SIZE;

            Object[] heightParams = new Object[]{torchX, torchZ};
            Float torchY = (Float) EnvCall.callReturnWithParams(worldGenerator, heightParams, "getHeightAt");
            
            if(torchY != null && torchY >= Water.LEVEL + 1.0f) {
                Vector3f torchPos = new Vector3f(torchX, torchY + 2.0f, torchZ);
                spawnTorch(torchPos);
            }
        }
    }

    /**
     * Update Torches
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
     * Render torches
     */
    @Override
    public void render() {
        for(TorchController torch : torchData) {
            Object torchGenerator = EnvCall.callReturn(torch, "getGenerator");
            if(torchGenerator == null) continue;
            
            EnvCall.call(torchGenerator, "render");
            
            
        }
    }
}
