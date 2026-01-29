package main.com.app.root.env.clouds;
import main.com.app.root.Spawner;
import main.com.app.root.SpawnerData;
import main.com.app.root.SpawnerHandler;
import main.com.app.root.Tick;
import main.com.app.root.env.NoiseGeneratorWrapper;
import main.com.app.root.env.world.Chunk;
import main.com.app.root.env.world.WorldGenerator;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.mesh.MeshData;
import main.com.app.root.mesh.MeshRenderer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.joml.Vector3f;

public class CloudGenerator implements SpawnerHandler {
    private final Tick tick;
    private final NoiseGeneratorWrapper noiseGeneratorWrapper;
    private final Random random;
    private Mesh mesh;

    private static final String CLOUD_MESH_ID = "CLOUD_MESH";
    private static final int COUNT = 1;
    private static final int MIN_CLOUD_CHUNKS = 10;
    private static final int MAX_CLOUD_CHUNKS = 15;

    private static final float HEIGHT_MIN = 150.0f;
    private static final float HEIGHT_MAX = 250.0f;
    private static final float SCALE_MIN = 10.0f;
    private static final float SCALE_MAX = 30.0f;
    private static final float DENSITY_THRESHOLD = -0.3f;
    
    private Map<String, List<Vector3f>> chunkCloudMap;
    private Map<String, List<Vector3f>> chunkCloudRotations;
    private Map<String, List<Float>> chunkCloudScales;
    private Set<String> selectedChunks;

    private int cloudCounter = 0;
    private long seed;
    private boolean cloudsGenerated = false;

    public CloudGenerator(Tick tick, Mesh mesh) {
        this.tick = tick;
        this.mesh = mesh;
        this.noiseGeneratorWrapper = new NoiseGeneratorWrapper();
        this.random = new Random();

        this.chunkCloudMap = new HashMap<>();
        this.chunkCloudRotations = new HashMap<>();
        this.chunkCloudScales = new HashMap<>();
        this.selectedChunks = new HashSet<>();

        this.seed = System.currentTimeMillis();

        setMesh();
    }

    @Override
    public SpawnerData getType() {
        return SpawnerData.CLOUD;
    }

    @Override 
    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
    }

    public void setSeed(long seed) {
        this.seed = seed;
        this.random.setSeed(seed);
    }

    /**
     * Set Mesh
     */
    private void setMesh() {
        mesh.addModel(CLOUD_MESH_ID, "cloud1");
        
        MeshData cloudData = mesh.getData(CLOUD_MESH_ID);
        cloudData.getMeshInstance().setInstanced(true);
        
        MeshRenderer renderer = mesh.getMeshRenderer(CLOUD_MESH_ID);
        renderer.setData(cloudData);
    }

    /**
     * 
     * Generate
     *
     */
    @Override
    public void generate(int chunkX, int chunkZ) {
        String chunkId = Chunk.getId(chunkX, chunkZ);

        if(chunkCloudMap.containsKey(chunkId)) {
            addChunkCloudsToInstances(chunkId);
            return;
        }
        if(!shouldChunkHaveClouds(chunkX, chunkZ)) {
            chunkCloudMap.put(chunkId, new ArrayList<>());
            chunkCloudRotations.put(chunkId, new ArrayList<>());
            chunkCloudScales.put(chunkId, new ArrayList<>());
            return;
        }

        float worldStartX = chunkX * Chunk.CHUNK_SIZE - WorldGenerator.WORLD_SIZE / 2.0f;
        float worldStartZ = chunkZ * Chunk.CHUNK_SIZE - WorldGenerator.WORLD_SIZE / 2.0f;

        List<Vector3f> positions = new ArrayList<>();
        List<Vector3f> rotations = new ArrayList<>();
        List<Float> scales = new ArrayList<>();

        long offset = 81647L;
        Random random = Spawner.Deterministic(
            chunkX, 
            chunkZ, 
            offset,
            SpawnerData.CLOUD
        );

        for(int i = 0; i < COUNT; i++) {
            float x = worldStartX + random.nextFloat() * Chunk.CHUNK_SIZE;
            float z = worldStartZ + random.nextFloat() * Chunk.CHUNK_SIZE;

            float density = noiseGeneratorWrapper.fractualSimplexNoise(
                x * 0.005f,
                z * 0.005f,
                3,
                0.6f,
                2.0f
            );
            density = (density + 1.0f) / 2.0f;
            
            if(density > DENSITY_THRESHOLD) {
                float height = HEIGHT_MIN + (density * (HEIGHT_MAX - HEIGHT_MIN)) + 90.0f;
                float scale = SCALE_MIN + density * (SCALE_MAX - SCALE_MIN);
                float randomRotation = random.nextFloat() * 360.0f;

                Vector3f position = new Vector3f(x, height, z);
                Vector3f rotation = new Vector3f(0, randomRotation, 0);

                positions.add(position);
                rotations.add(rotation);
                scales.add(scale);
            }
        }

        chunkCloudMap.put(chunkId, positions);
        chunkCloudRotations.put(chunkId, rotations);
        chunkCloudScales.put(chunkId, scales);
        
        addChunkCloudsToInstances(chunkId);
    }

    private void addChunkCloudsToInstances(String chunkId) {
        List<Vector3f> positions = chunkCloudMap.get(chunkId);
        List<Vector3f> rotations = chunkCloudRotations.get(chunkId);
        List<Float> scales = chunkCloudScales.get(chunkId);

        if(positions == null || rotations == null || scales == null) return;

        MeshData cloudData = mesh.getData(CLOUD_MESH_ID);
        for(int i = 0; i < positions.size(); i++) {
            cloudData.getMeshInstance().addInstance(
                positions.get(i), 
                rotations.get(i), 
                scales.get(i)
            );
        }
        mesh.getMeshRenderer(CLOUD_MESH_ID).markInstanceBuffer();
    }

    private boolean shouldChunkHaveClouds(int chunkX, int chunkZ) {
        float noiseValue = noiseGeneratorWrapper.fractualSimplexNoise(
            chunkX * 0.3f,
            chunkZ * 0.3f,
            2,
            0.5f,
            2.0f
        );
        noiseValue = (noiseValue + 1.0f) / 2.0f;
        float threshold = 0.7f;
        return noiseValue > threshold;
    }

    /**
     * Update
     */
    @Override
    public void update() {
        if(!cloudsGenerated) return;

        MeshData cloudData = mesh.getData(CLOUD_MESH_ID);
        List<Vector3f> allPositions = new ArrayList<>();
        
        for(List<Vector3f> chunkPositions : chunkCloudMap.values()) {
            allPositions.addAll(chunkPositions);
        }

        float worldHalfSize = WorldGenerator.WORLD_SIZE / 2.0f;
        
        for(int i = 0; i < allPositions.size(); i++) {
            Vector3f currentPos = allPositions.get(i);
            float speed = 0.1f * tick.getDeltaTime();
            currentPos.x += speed;

            if(Math.abs(currentPos.x) > worldHalfSize * 1.5f) {
                currentPos.x = -worldHalfSize * 1.5f;
            }
        }

        mesh.getMeshRenderer(CLOUD_MESH_ID).markInstanceBuffer();
    }

    /**
     * Render
     */
    @Override
    public void render() {
        if(!cloudsGenerated) {
            cloudsGenerated = true;
            return;
        }
        
        int instanceCount = mesh.getData(CLOUD_MESH_ID).getMeshInstance().getInstanceCount();
        if(instanceCount > 0) {
            mesh.render(CLOUD_MESH_ID, 0);
        }
    }

    /**
     * Unload
     */
    @Override
    public void unload(int chunkX, int chunkZ) {
        String chunkId = Chunk.getId(chunkX, chunkZ);
        
        List<Vector3f> positions = chunkCloudMap.get(chunkId);
        if(positions == null) return;

        MeshData cloudData = mesh.getData(CLOUD_MESH_ID);
        int instancesToRemove = positions.size();
        
        for(int i = instancesToRemove - 1; i >= 0; i--) {
            cloudData.getMeshInstance().removeInstance(
                cloudData.getMeshInstance().getInstanceCount() - 1
            );
        }

        chunkCloudMap.remove(chunkId);
        chunkCloudRotations.remove(chunkId);
        chunkCloudScales.remove(chunkId);
        
        mesh.getMeshRenderer(CLOUD_MESH_ID).markInstanceBuffer();
    }

    /**
     * 
     * Data
     * 
     */
    @Override
    public void applyData(Map<String, Object> data) {
        
    }

    @Override
    public void getData(Map<String, Object> data) {
        
    }
}