package main.com.app.root.env.clouds;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.joml.Vector3f;

import main.com.app.root.Tick;
import main.com.app.root.env.NoiseGeneratorWrapper;
import main.com.app.root.env.world.WorldGenerator;
import main.com.app.root.mesh.Mesh;

public class CloudGenerator {
    private final Tick tick;
    private final NoiseGeneratorWrapper noiseGeneratorWrapper;
    private final Mesh mesh;
    private final Random random;

    private static final String CLOUD_MESH_ID_PREFIX = "cloud_";
    private static final int COUNT = 100;
    private static final float HEIGHT_MIN = 150.0f;
    private static final float HEIGHT_MAX = 250.0f;
    private static final float SCALE_MIN = 10.0f;
    private static final float SCALE_MAX= 30.0f;
    private static final float DESTINY_THRESHOLD = 0.4f;
    
    private List<String> activeCloudIds;
    private int cloudCounter = 0;
    private long seed;
    private boolean cloudsGenerated = false;

    public CloudGenerator(Tick tick, Mesh mesh) {
        this.tick = tick;
        this.mesh = mesh;
        this.noiseGeneratorWrapper = new NoiseGeneratorWrapper();
        this.random = new Random();

        this.activeCloudIds = new ArrayList<>();
        this.seed = System.currentTimeMillis();
    }

    public void setSeed(long seed) {
        this.seed = seed;
        this.random.setSeed(seed);
        regenerate();
    }

    /**
     * Regenerate
     */
    public void regenerate() {
        clear();
        generate();
        cloudsGenerated = true;
    }

    /**
     * Clear
     */
    private void clear() {
        for(String cloudId : activeCloudIds) {
            if(mesh.hasMesh(cloudId)) {
                mesh.remove(cloudId);
            }
        }
        activeCloudIds.clear();
    }

    /**
     * Spawn Cloud
     */
    private void spawnCloud(
        float worldX, 
        float worldZ, 
        float destiny
    ) {
        String cloudId = 
            CLOUD_MESH_ID_PREFIX + 
            System.currentTimeMillis() + "_" +
            activeCloudIds.size();

        float height = HEIGHT_MIN + (destiny * (HEIGHT_MAX - HEIGHT_MIN)) + 90.0f;
        float scale = SCALE_MIN + destiny * (SCALE_MAX - SCALE_MIN);

        try {
            mesh.addModel(cloudId, "cloud1");
            mesh.setPosition(cloudId, new Vector3f(worldX, height, worldZ));
            mesh.getData(cloudId).setScale(scale);
            mesh.getData(cloudId).setRotation(
                new Vector3f(0, random.nextFloat() * 360.0f, 0)
            );
            activeCloudIds.add(cloudId);
        } catch(Exception err) {
            System.err.println("Failed to spawn cloud at (" + worldX + ", " + worldZ + "): " + err.getMessage());
        }
    }

    /**
     * Generate
     */
    private void generate() {
        float worldHalfSize = WorldGenerator.WORLD_SIZE / 2.0f;
        for(int i = 0; i < COUNT; i++) {
            float x = (random.nextFloat() * WorldGenerator.WORLD_SIZE) - worldHalfSize;
            float z = (random.nextFloat() * WorldGenerator.WORLD_SIZE) - worldHalfSize;

            float destiny = noiseGeneratorWrapper.fractualSimplexNoise(
                x * 0.005f,
                z * 0.005f,
                3,
                0.6f,
                2.0f
            );
            if(destiny > DESTINY_THRESHOLD) {
                spawnCloud(x, z, destiny);
            }
        }
    }

    private void generateAdditionalClouds(int count) {
        float worldHalfSize = WorldGenerator.WORLD_SIZE / 2.0f;
        for (int i = 0; i < count; i++) {
            float x = (random.nextFloat() * WorldGenerator.WORLD_SIZE) - worldHalfSize;
            float z = (random.nextFloat() * WorldGenerator.WORLD_SIZE) - worldHalfSize;
            
            float destiny = noiseGeneratorWrapper.fractualSimplexNoise(
                x * 0.005f,
                z * 0.005f,
                3,
                0.6f,
                2.0f
            );
            if (destiny > DESTINY_THRESHOLD) {
                spawnCloud(x, z, destiny);
            }
        }
    }

    /**
     * Update
     */
    public void update() {
        if(!cloudsGenerated) regenerate();

        float worldHalfSize = WorldGenerator.WORLD_SIZE / 2.0f;
        List<String> cloudsToRemove = new ArrayList<>();

        for(String id : activeCloudIds) {
            if(mesh.hasMesh(id)) {
                Vector3f currentPos = mesh.getPosition(id);
                float speed = 0.1f * tick.getDeltaTime();
                Vector3f newPos = new Vector3f(
                    currentPos.x + speed,
                    currentPos.y,
                    currentPos.z
                );
                
                if(Math.abs(newPos.x) > worldHalfSize * 1.5f) {
                    cloudsToRemove.add(id);
                } else {
                    mesh.setPosition(id, newPos);
                }
            } else {
                cloudsToRemove.add(id);
            }
        }

        for(String id : cloudsToRemove) {
            if(mesh.hasMesh(id)) mesh.remove(id);
            activeCloudIds.remove(id);
        }

        if(activeCloudIds.size() < COUNT * 0.7f) {
            generateAdditionalClouds(COUNT - activeCloudIds.size());
        }
    }

    /**
     * Render
     */
    public void render() {
        if(!cloudsGenerated) regenerate();
    }

    public int getCount() {
        return activeCloudIds.size();
    }

    public boolean areCloudsGenerated() {
        return cloudsGenerated;
    }
}