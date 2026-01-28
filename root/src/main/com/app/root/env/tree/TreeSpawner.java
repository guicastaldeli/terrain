package main.com.app.root.env.tree;
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
import main.com.app.root.mesh.Mesh;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.joml.Vector3f;

public class TreeSpawner implements SpawnerHandler {
    private final Tick tick;
    private final EnvController envController;
    private final Spawner spawner;
    private Mesh mesh;

    public TreeData treeData;
    private boolean isActive;

    private Map<String, List<TreeController>> chunkTreeMap = new HashMap<>();
    private static Map<Integer, Float> LEVEL_DISTRIBUTION;

    private static final float TREE_COVERAGE = 0.0000f;
    public static final int MAX_TREES_PER_CHUNK = (int)(Chunk.CHUNK_SIZE * Chunk.CHUNK_SIZE * TREE_COVERAGE);

    public TreeSpawner(
        Tick tick, 
        Mesh mesh,
        EnvController envController,
        Spawner spawner
    ) {
        this.tick = tick;
        this.mesh = mesh;
        this.envController = envController;
        this.spawner = spawner;

        LEVEL_DISTRIBUTION = new HashMap<>();
        setLevelDistribution();

        this.treeData = new TreeData();
        this.treeData.createDefaultConfigs();
        this.isActive = true;
    }

    @Override
    public SpawnerData getType() {
        return SpawnerData.TREE;
    }

    @Override 
    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
    }

    public static void setLevelDistribution() {
        LEVEL_DISTRIBUTION.put(0, 0.7f);
        LEVEL_DISTRIBUTION.put(1, 0.15f);
        LEVEL_DISTRIBUTION.put(2, 0.07f);
        LEVEL_DISTRIBUTION.put(3, 0.04f);
        LEVEL_DISTRIBUTION.put(4, 0.02f);
        LEVEL_DISTRIBUTION.put(5, 0.01f);
        LEVEL_DISTRIBUTION.put(6, 0.005f);
        LEVEL_DISTRIBUTION.put(7, 0.002f);
        LEVEL_DISTRIBUTION.put(8, 0.001f);
        LEVEL_DISTRIBUTION.put(9, 0.0005f);
        LEVEL_DISTRIBUTION.put(10, 0.0003f);
    }   

    private boolean isValidTreePos(
        float x,
        float z,
        Object worldGenerator
    ) {
        float offset = 5.0f;

        Object[] heightParams = new Object[]{x, z};
        Float height = (Float) EnvCall.callReturnWithParams(worldGenerator, heightParams, "getHeightAt");
        if(height == null || height < Water.LEVEL + offset) return false;
        
        return true;
    }

    /**
     * Nearest Tree
     */
    public TreeController getNearestTree(Vector3f position, float maxDistance) {
        /*
        System.out.println("DEBUG [getNearestTree]: Looking near [" + position.x + ", " + position.z + 
                        "] within " + maxDistance + " units");
                        */
        
        TreeController nearest = null;
        float nearestDistance = Float.MAX_VALUE;
        int checked = 0;
        int aliveCount = 0;
        
        for(TreeController tree : treeData.trees) {
            checked++;
            TreeGenerator treeGenerator = tree.getGenerator();
            
            if(treeGenerator == null) {
                System.out.println("  Tree " + checked + ": generator is NULL");
                continue;
            }
            
            boolean isAlive = treeGenerator.isAlive();
            if(!isAlive) {
                System.out.println("  Tree " + checked + ": not alive");
                continue;
            }
            
            aliveCount++;
            Vector3f treePos = treeGenerator.getPosition();
            float distance = treePos.distance(position);
            
            /*
            System.out.println("  Tree " + checked + ": at [" + treePos.x + ", " + treePos.z + 
                            "] distance: " + distance);
                            */
            
            if(distance <= maxDistance && distance < nearestDistance) {
                nearestDistance = distance;
                nearest = tree;
                //System.out.println("    -> New nearest!");
            }
        }
        
        /*
        System.out.println("DEBUG [getNearestTree]: Checked " + checked + " trees, " + 
                        aliveCount + " alive, found: " + (nearest != null ? "YES" : "NO"));
                        */
        return nearest;
    }

    /**
     * Respawn Tree
     */
    public void respawnTree(TreeController oldTree, int newLevel) {
        if(!treeData.trees.contains(oldTree)) return;

        treeData.trees.remove(oldTree);
        TreeGenerator oldGenerator = oldTree.getGenerator();
        oldGenerator.cleanup();

        Vector3f position = oldGenerator.getPosition();
        TreeData data = treeData.configs.get(newLevel);
        if(data != null) {
            TreeController treeController = new TreeController();
            treeController.createGenerator(data, position, mesh, spawner);
            
            TreeGenerator treeGenerator = treeController.getGenerator();
            treeGenerator.setId("tree_" + treeData.currentTreeId++);
            treeData.trees.add(treeController);

            /*
            System.out.println("Respawned tree from level " + 
                              oldGenerator.getLevel() + 
                              " to level " + newLevel);
                              */
        }
    }

    public void respawnTreeAtPos(Vector3f position, int level) {
        TreeData data = treeData.configs.get(level);
        if(data != null) {
            TreeController treeController = new TreeController();
            treeController.createGenerator(data, position, mesh, spawner);
            
            TreeGenerator treeGenerator = treeController.getGenerator();
            treeGenerator.setId("tree_" + treeData.currentTreeId++);
            treeData.trees.add(treeController);

            /*
            System.out.println("Respawned tree at position [" + position.x + 
                              ", " + position.z + "] at level " + level);
                              */
        }
    }

    @Override
    public void setActive(boolean active) {
        this.isActive = active;
        System.out.println("Spawner " + (active ? "activated" : "deactivated"));
    }

    public List<TreeController> getTrees() {
        return treeData.trees;
    }

    public void addTree(TreeController tree) {
        treeData.trees.add(tree);
    }

    public int getActiveTreeCount() {
        int count = 0;
        for(TreeController tree : treeData.trees) {
            TreeGenerator treeGenerator = tree.getGenerator();
            if(treeGenerator != null && treeGenerator.isAlive()) {
                count++;
            }
        }
        return count;
    }

    public int getTotalTreeCount() {
        return treeData.trees.size();
    }

    public Map<Integer, TreeData> getTreeData() {
        return Collections.unmodifiableMap(treeData.configs);
    }

    public TreeData getConfigForLevel(int level) {
        return treeData.configs.get(level);
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    /**
     * Spawn Tree At Level
     */
    private void spawnTreeAtLevel(Vector3f position, int level) {
        if(!isActive) return;

        TreeData data = treeData.configs.get(level);
        if(data == null) {
            data = treeData.configs.get(0);
        }

        TreeController treeController = new TreeController();
        treeController.createGenerator(
            data, 
            position, 
            mesh, 
            spawner
        );
        
        TreeGenerator treeGenerator = treeController.getGenerator();
        if(treeGenerator == null) return;
        
        treeGenerator.mesh = this.mesh;
    
        String treeId = "tree" + treeData.currentTreeId++;
        treeGenerator.setId(treeId);
        
        treeGenerator.isAlive = true;
        treeGenerator.currHealth = data.getHealth();
        treeGenerator.createMesh();
        
        treeData.trees.add(treeController);
        
        /*
        System.out.println("DEBUG: Spawned Level " + level + " tree at [" + 
                        position.x + ", " + position.z + "] - Total trees now: " + treeData.trees.size());
                        */
    }

    /**
     * Handle Tree Break
     */
    public void handleTreeBreak(
        TreeController deadTree, 
        Vector3f position, 
        int currLevel
    ) {
        if(treeData.trees.contains(deadTree)) {
            treeData.trees.remove(deadTree);
            System.out.println("Removed dead tree from list");
        }
        
        TreeGenerator treeGenerator = deadTree.getGenerator();
        if(treeGenerator != null) {
            treeGenerator.cleanup();
        }
        
        int nextLevel = currLevel + 1;
        if(!treeData.configs.containsKey(nextLevel)) nextLevel = 0;
        
        spawnTreeAtLevel(position, nextLevel);
    }

    /**
     * Cleanup Tree At Pos
     */
    private void cleanupTreeAtPos(Vector3f position) {
        float cleanupRadius = 5.0f;
        for(Iterator<TreeController> iterator = treeData.trees.iterator(); iterator.hasNext();) {
            TreeController tree = iterator.next();
            TreeGenerator treeGenerator = tree.getGenerator();
            if(treeGenerator == null) continue;
            
            Vector3f treePos = treeGenerator.getPosition();
            float distance = treePos.distance(position);
            
            if(distance <= cleanupRadius) {
                treeGenerator.cleanup();
                iterator.remove();
                System.out.println("Cleaned up tree at position [" + position.x + ", " + position.z + "]");
                break;
            }
        }
    }

    /**
     * Get Random Tree Data
     */
    private TreeData getRandomTreeData(Random random) {
        float totalWeight = 0;
        for(float weight : LEVEL_DISTRIBUTION.values()) {
            totalWeight += weight;
        }

        float randomVal = random.nextFloat() * totalWeight;
        float cumulativeWeight = 0;

        for(Map.Entry<Integer, Float> level : LEVEL_DISTRIBUTION.entrySet()) {
            cumulativeWeight += level.getValue();
            if(randomVal <= cumulativeWeight) {
                TreeData data = getConfigForLevel(level.getKey());
                if(data != null) {
                    return data;
                }
            }
        }

        return getConfigForLevel(0);
    }

    /**
     * Spawn Tree in Chunk
     */
    private void spawnTreeInChunk(
        Vector3f position,
        int chunkX,
        int chunkZ,
        Random random
    ) {
        TreeData data = getRandomTreeData(random);
        if(data == null) {
            System.err.println("Failed to get tree data for chunk " + chunkX + ", " + chunkZ);
            return;
        }

        TreeController treeController = new TreeController();
        treeController.createGenerator(
            data, 
            position,
            mesh,
            spawner
        );

        TreeGenerator treeGenerator = treeController.getGenerator();
        if(treeGenerator != null) {
            treeGenerator.mesh = this.mesh;
            String treeId = "tree_" + chunkX + "_" + chunkZ + "_" + treeData.trees.size();
            treeGenerator.setId(treeId);
            treeGenerator.createMesh();
            treeData.trees.add(treeController);

            String chunkKey = Chunk.getId(chunkX, chunkZ);
            chunkTreeMap.computeIfAbsent(chunkKey, k -> new ArrayList<>()).add(treeController);
        } 
    }

    /**
     * Clear Trees
     */
    public void clearChunkTrees() {
        for(TreeController tree : treeData.trees) {
            TreeGenerator treeGenerator = tree.getGenerator();
            if(treeGenerator != null) {
                treeGenerator.cleanup();
            }
        }
        treeData.trees.clear();
        treeData.currentTreeId = 0;
        chunkTreeMap.clear();
    }

    /**
     * Generate
     */
    @Override
    public void generate(int chunkX, int chunkZ) {
        if(!isActive) return;
        
        Object mapInstance = envController.getEnv(EnvData.MAP).getInstance();
        Object worldGenerator = EnvCall.callReturn(mapInstance, "getGenerator");

        float worldStartX = chunkX * Chunk.CHUNK_SIZE - WorldGenerator.WORLD_SIZE / 2.0f;
        float worldStartZ = chunkZ * Chunk.CHUNK_SIZE - WorldGenerator.WORLD_SIZE / 2.0f;
        float worldEndX = worldStartX + Chunk.CHUNK_SIZE;
        float worldEndZ = worldStartZ + Chunk.CHUNK_SIZE;

        Random random = Spawner.Deterministic(chunkX, chunkZ);

        for(int i = 0; i < MAX_TREES_PER_CHUNK; i++) {
            float treeX = worldStartX + random.nextFloat() * Chunk.CHUNK_SIZE;
            float treeZ = worldStartZ + random.nextFloat() * Chunk.CHUNK_SIZE;
            
            Object[] heightParams = new Object[]{treeX, treeZ};
            Float treeY = (Float) EnvCall.callReturnWithParams(worldGenerator, heightParams, "getHeightAt");

            if(treeY != null && treeY >= Water.LEVEL + 2.0f) {
                Vector3f treePos = new Vector3f(treeX, treeY, treeZ);
                spawnTreeInChunk(
                    treePos, 
                    chunkX, 
                    chunkZ,
                    random
                );
            }
        }
    }

    /**
     * Unload
     */
    @Override
    public void unload(int chunkX, int chunkZ) {
        String chunkKey = Chunk.getId(chunkX, chunkZ);
        List<TreeController> trees = chunkTreeMap.get(chunkKey);

        if(trees != null) {
            for(TreeController tree : trees) {
                TreeGenerator treeGenerator = tree.getGenerator();
                if(treeGenerator != null) treeGenerator.cleanup();
                treeData.trees.remove(tree);
            }
            chunkTreeMap.remove(chunkKey);
            System.out.println("Unloaded " + trees.size() + " trees from chunk " + chunkKey);
        }
    }

    /**
     * Update
     */
    @Override
    public void update() {
        if(!isActive) return;

        List<TreeController> treesToUpdate = new ArrayList<>(treeData.trees);
        
        for(TreeController tree : treesToUpdate) {
            TreeGenerator treeGenerator = tree.getGenerator();
            if(treeGenerator == null || !treeGenerator.isAlive()) continue;
            treeGenerator.update(tick.getDeltaTime());
        }
    }

    /**
     * Render
     */
    @Override
    public void render() {
        if(!isActive) return;
        for(TreeController tree : treeData.trees) {
            TreeGenerator treeGenerator = tree.getGenerator();
            if(treeGenerator != null && treeGenerator.isAlive()) {
                treeGenerator.render();
            }
        }
    }

    /**
     * 
     * Data
     * 
     */
    @Override
    public void applyData(Map<String, Object> data) {
        if(data.containsKey("trees")) {
            List<Map<String, Object>> treesData = (List<Map<String, Object>>) data.get("trees");
            
            clearChunkTrees();
            
            for(Map<String, Object> treeData : treesData) {
                try {
                    float x = ((Number) treeData.get("position_x")).floatValue();
                    float y = ((Number) treeData.get("position_y")).floatValue();
                    float z = ((Number) treeData.get("position_z")).floatValue();
                    int level = ((Number) treeData.get("level")).intValue();
                    boolean alive = (Boolean) treeData.get("alive");
                    float respawnTimer = 
                        treeData.containsKey("respawn_timer") ? 
                        ((Number) treeData.get("respawn_timer")).floatValue() : 
                        0f;
                    
                    Vector3f position = new Vector3f(x, y, z);
                    int[] coords = Chunk.getCoords(position.x, position.z);
                    String chunkKey = Chunk.getId(coords[0], coords[1]);
                    
                    TreeData treeConfig = getConfigForLevel(level);
                    if(treeConfig != null) {
                        TreeController treeController = new TreeController();
                        treeController.createGenerator(treeConfig, position, spawner.mesh, spawner);
                        
                        TreeGenerator treeGenerator = treeController.getGenerator();
                        if(treeGenerator != null) {
                            treeGenerator.mesh = spawner.mesh;
                            
                            String treeId = "tree" + this.treeData.currentTreeId++;
                            treeGenerator.setId(treeId);
                            
                            if(!alive) {
                                treeGenerator.isAlive = false;
                                treeGenerator.currHealth = 0;
                                treeGenerator.respawnTimer = respawnTimer;
                                treeGenerator.destroyMesh();
                            } else {
                                treeGenerator.isAlive = true;
                                treeGenerator.currHealth = treeConfig.getHealth();
                                treeGenerator.createMesh();
                            }
                            
                            addTree(treeController);
                            chunkTreeMap.computeIfAbsent(chunkKey, k -> new ArrayList<>()).add(treeController);
                        }
                    }
                } catch(Exception err) {
                    err.printStackTrace();
                }
            }
            System.out.println("Loaded " + treesData.size() + " tree entries");
        }
    }

    @Override
    public void getData(Map<String, Object> data) {
        List<Map<String, Object>> treesData = new ArrayList<>();
        for(TreeController tree : getTrees()) {
            Map<String, Object> treeData = new HashMap<>();
                
            try {
                TreeGenerator treeGenerator = tree.getGenerator();
                
                if(treeGenerator != null) {
                    Vector3f treePos = treeGenerator.getPosition();
                    int treeLevel = treeGenerator.getLevel();
                    boolean isAlive = treeGenerator.isAlive();
                    float respawnTimer = treeGenerator.getRespawnTimer();
                        
                    treeData.put("position_x", treePos.x);
                    treeData.put("position_y", treePos.y);
                    treeData.put("position_z", treePos.z);
                    treeData.put("level", treeLevel);
                    treeData.put("alive", isAlive);
                    treeData.put("respawn_timer", respawnTimer);
                    treesData.add(treeData);
                }
            } catch(Exception err) {
                err.printStackTrace();
            }
        }
        data.put("trees", treesData);
        System.out.println("Saved " + treesData.size() + " tree entries");
    }
}