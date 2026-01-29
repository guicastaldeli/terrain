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

public class CloudSpawner implements SpawnerHandler {
    private final Tick tick;
    private Mesh mesh;
    private final Spawner spawner;

    private final NoiseGeneratorWrapper noiseGeneratorWrapper;
    private final Random random;

    private static final String[] CLOUD_MODELS = { "cloud1", "cloud2" };
    private static final int COUNT = 1;
    private static final int MIN_CLOUD_CHUNKS = 10;
    private static final int MAX_CLOUD_CHUNKS = 15;

    private static final float HEIGHT_MIN = 500.0f;
    private static final float HEIGHT_MAX = 2000.0f;
    private static final float SCALE_MIN = 10.0f;
    private static final float SCALE_MAX = 80.0f;
    private static final float SPEED_MIN = 10.0f;
    private static final float SPEED_MAX = 50.0f;
    private static final float DENSITY_THRESHOLD = -0.3f;
    
    private Map<String, List<Vector3f>> chunkCloudMap;
    private Map<String, List<Vector3f>> chunkCloudRotations;
    private Map<String, List<Float>> chunkCloudScales;
    private Map<String, List<Float>> chunkCloudSpeeds;
    private Map<String, List<Integer>> chunkCloudModelIndices;
    private Set<String> selectedChunks;

    private int currentCloudId = 0;
    private long seed;
    private boolean cloudsGenerated = false;

    public CloudSpawner(
        Tick tick, 
        Mesh mesh,
        Spawner spawner
    ) {
        this.tick = tick;
        this.mesh = mesh;
        this.spawner = spawner;
        this.noiseGeneratorWrapper = new NoiseGeneratorWrapper();
        this.random = new Random();

        this.chunkCloudMap = new HashMap<>();
        this.chunkCloudRotations = new HashMap<>();
        this.chunkCloudScales = new HashMap<>();
        this.chunkCloudSpeeds = new HashMap<>();
        this.chunkCloudModelIndices = new HashMap<>();
        this.selectedChunks = new HashSet<>();

        this.seed = System.currentTimeMillis();
        this.currentCloudId = 0;

        initMesh();
    }

    @Override
    public SpawnerData getType() {
        return SpawnerData.CLOUD;
    }

    @Override 
    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
        initMesh();
    }

    private void initMesh() {
        for(String modelName : CLOUD_MODELS) {
            mesh.addModel(modelName, modelName);
            
            MeshData cloudData = mesh.getData(modelName);
            cloudData.getMeshInstance().setInstanced(true);
            
            cloudData.setTransparentColor(0.8f, 0.2f, 0.2f, 0.8f);

            MeshRenderer renderer = mesh.getMeshRenderer(modelName);
            renderer.setData(cloudData);
        }
    }

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
            chunkCloudSpeeds.put(chunkId, new ArrayList<>());
            chunkCloudModelIndices.put(chunkId, new ArrayList<>());
            return;
        }

        float worldStartX = chunkX * Chunk.CHUNK_SIZE - WorldGenerator.WORLD_SIZE / 2.0f;
        float worldStartZ = chunkZ * Chunk.CHUNK_SIZE - WorldGenerator.WORLD_SIZE / 2.0f;

        List<Vector3f> positions = new ArrayList<>();
        List<Vector3f> rotations = new ArrayList<>();
        List<Float> scales = new ArrayList<>();
        List<Float> speeds = new ArrayList<>();
        List<Integer> modelIndices = new ArrayList<>();

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
            if(density > DENSITY_THRESHOLD) {
                float height = HEIGHT_MIN + (density * (HEIGHT_MAX - HEIGHT_MIN));
                float scale = SCALE_MIN + density * (SCALE_MAX - SCALE_MIN);
                float speed = SPEED_MIN + random.nextFloat() * (SPEED_MAX - SPEED_MIN);
                float randomRotation = random.nextFloat() * 360.0f;
                
                int modelIndex = random.nextInt(CLOUD_MODELS.length);

                Vector3f position = new Vector3f(x, height, z);
                Vector3f rotation = new Vector3f(0, randomRotation, 0);

                positions.add(position);
                rotations.add(rotation);
                scales.add(scale);
                speeds.add(speed);
                modelIndices.add(modelIndex);
            }
        }

        chunkCloudMap.put(chunkId, positions);
        chunkCloudRotations.put(chunkId, rotations);
        chunkCloudScales.put(chunkId, scales);
        chunkCloudSpeeds.put(chunkId, speeds);
        chunkCloudModelIndices.put(chunkId, modelIndices);
        
        addChunkCloudsToInstances(chunkId);
    }

    private void addChunkCloudsToInstances(String chunkId) {
        List<Vector3f> positions = chunkCloudMap.get(chunkId);
        List<Vector3f> rotations = chunkCloudRotations.get(chunkId);
        List<Float> scales = chunkCloudScales.get(chunkId);
        List<Integer> modelIndices = chunkCloudModelIndices.get(chunkId);
        if(positions == null || 
            rotations == null || 
            scales == null || 
            modelIndices == null
        ) {
            return;
        }

        for(int i = 0; i < positions.size(); i++) {
            int modelIndex = modelIndices.get(i);
            String modelName = CLOUD_MODELS[modelIndex];
            
            MeshData cloudData = mesh.getData(modelName);
            cloudData.getMeshInstance().addInstance(
                positions.get(i), 
                rotations.get(i), 
                scales.get(i)
            );
            mesh.getMeshRenderer(modelName).markInstanceBuffer();
        }
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
        float threshold = 0.6f;
        return noiseValue > threshold;
    }

    @Override
    public void update() {
        if(!cloudsGenerated) return;

        float worldHalfSize = WorldGenerator.WORLD_SIZE / 2.0f;
        
        for(String modelName : CLOUD_MODELS) {
            MeshData cloudData = mesh.getData(modelName);
            cloudData.getMeshInstance().clearInstances();
        }

        for(String chunkId : chunkCloudMap.keySet()) {
            List<Vector3f> positions = chunkCloudMap.get(chunkId);
            List<Vector3f> rotations = chunkCloudRotations.get(chunkId);
            List<Float> scales = chunkCloudScales.get(chunkId);
            List<Float> speeds = chunkCloudSpeeds.get(chunkId);
            List<Integer> modelIndices = chunkCloudModelIndices.get(chunkId);
            
            if(positions == null || rotations == null || scales == null || speeds == null || modelIndices == null) continue;
            
            for(int i = 0; i < positions.size(); i++) {
                Vector3f currentPos = positions.get(i);
                float speed = speeds.get(i) * tick.getDeltaTime();
                currentPos.x += speed;
                if(currentPos.x > worldHalfSize * 1.5f) {
                    currentPos.x = -worldHalfSize * 1.5f;
                } else if(currentPos.x < -worldHalfSize * 1.5f) {
                    currentPos.x = worldHalfSize * 1.5f;
                }
                
                int modelIndex = modelIndices.get(i);
                String modelName = CLOUD_MODELS[modelIndex];
                
                MeshData cloudData = mesh.getData(modelName);
                cloudData.getMeshInstance().addInstance(
                    currentPos,
                    rotations.get(i),
                    scales.get(i)
                );
            }
        }
        
        for(String modelName : CLOUD_MODELS) {
            mesh.getMeshRenderer(modelName).markInstanceBuffer();
        }
    }

    @Override
    public void render() {
        if(!cloudsGenerated) {
            cloudsGenerated = true;
            return;
        }
        for(String modelName : CLOUD_MODELS) {
            int instanceCount = mesh.getData(modelName).getMeshInstance().getInstanceCount();
            if(instanceCount > 0) {
                mesh.render(modelName, 0);
            }
        }
    }

    @Override
    public void unload(int chunkX, int chunkZ) {
        String chunkId = Chunk.getId(chunkX, chunkZ);
        
        List<Vector3f> positions = chunkCloudMap.get(chunkId);
        List<Integer> modelIndices = chunkCloudModelIndices.get(chunkId);
        
        if(positions == null || modelIndices == null) return;

        for(int i = positions.size() - 1; i >= 0; i--) {
            int modelIndex = modelIndices.get(i);
            String modelName = CLOUD_MODELS[modelIndex];
            
            MeshData cloudData = mesh.getData(modelName);
            cloudData.getMeshInstance().removeInstance(
                cloudData.getMeshInstance().getInstanceCount() - 1
            );
            mesh.getMeshRenderer(modelName).markInstanceBuffer();
        }

        chunkCloudMap.remove(chunkId);
        chunkCloudRotations.remove(chunkId);
        chunkCloudScales.remove(chunkId);
        chunkCloudSpeeds.remove(chunkId);
        chunkCloudModelIndices.remove(chunkId);
    }

    @Override
    public void applyData(Map<String, Object> data) {
        if(data.containsKey("cloud") && spawner != null) {
            List<Map<String, Object>> cloudsData = (List<Map<String, Object>>) data.get("cloud");
            
            int maxCloudId = 0;
            
            for(Map<String, Object> cloudData : cloudsData) {
                try {
                    float x = ((Number) cloudData.get("position_x")).floatValue();
                    float y = ((Number) cloudData.get("position_y")).floatValue();
                    float z = ((Number) cloudData.get("position_z")).floatValue();
                    float scale = ((Number) cloudData.get("scale")).floatValue();
                    float rotationY = ((Number) cloudData.get("rotation_y")).floatValue();
                    float speed = cloudData.containsKey("speed") 
                        ? ((Number) cloudData.get("speed")).floatValue() 
                        : SPEED_MIN + (float)Math.random() * (SPEED_MAX - SPEED_MIN);
                    int modelIndex = cloudData.containsKey("model_index") 
                        ? ((Number) cloudData.get("model_index")).intValue() 
                        : 0;
                    
                    Vector3f position = new Vector3f(x, y, z);
                    Vector3f rotation = new Vector3f(0, rotationY, 0);
                    
                    String modelName = CLOUD_MODELS[modelIndex];
                    MeshData cloudMeshData = mesh.getData(modelName);
                    cloudMeshData.getMeshInstance().addInstance(position, rotation, scale);
                    
                    String cloudId = "cloud_" + currentCloudId++;
                    maxCloudId = Math.max(maxCloudId, currentCloudId);
                    
                    String chunkId = Chunk.getId(
                        Chunk.getCoords(position.x, position.z)[0],
                        Chunk.getCoords(position.x, position.z)[1]
                    );
                    
                    chunkCloudMap.computeIfAbsent(chunkId, k -> new ArrayList<>()).add(position);
                    chunkCloudRotations.computeIfAbsent(chunkId, k -> new ArrayList<>()).add(rotation);
                    chunkCloudScales.computeIfAbsent(chunkId, k -> new ArrayList<>()).add(scale);
                    chunkCloudSpeeds.computeIfAbsent(chunkId, k -> new ArrayList<>()).add(speed);
                    chunkCloudModelIndices.computeIfAbsent(chunkId, k -> new ArrayList<>()).add(modelIndex);
                    
                } catch(Exception err) {
                    err.printStackTrace();
                }
            }
            currentCloudId = maxCloudId;
            
            for(String modelName : CLOUD_MODELS) {
                mesh.getMeshRenderer(modelName).markInstanceBuffer();
            }
        }
    }

    @Override
    public void getData(Map<String, Object> data) {
        List<Map<String, Object>> cloudsData = new ArrayList<>();
        
        for(String chunkId : chunkCloudMap.keySet()) {
            List<Vector3f> positions = chunkCloudMap.get(chunkId);
            List<Vector3f> rotations = chunkCloudRotations.get(chunkId);
            List<Float> scales = chunkCloudScales.get(chunkId);
            List<Float> speeds = chunkCloudSpeeds.get(chunkId);
            List<Integer> modelIndices = chunkCloudModelIndices.get(chunkId);
            
            if(positions != null && rotations != null && scales != null && speeds != null && modelIndices != null) {
                for(int i = 0; i < positions.size(); i++) {
                    Map<String, Object> cloudEntry = new HashMap<>();
                    
                    cloudEntry.put("position_x", positions.get(i).x);
                    cloudEntry.put("position_y", positions.get(i).y);
                    cloudEntry.put("position_z", positions.get(i).z);
                    cloudEntry.put("scale", scales.get(i));
                    cloudEntry.put("rotation_y", rotations.get(i).y);
                    cloudEntry.put("speed", speeds.get(i));
                    cloudEntry.put("model_index", modelIndices.get(i));
                    
                    cloudsData.add(cloudEntry);
                }
            }
        }
        data.put("cloud", cloudsData);
    }
}