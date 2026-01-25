package main.com.app.root;
import main.com.app.root.env.EnvCall;
import main.com.app.root.env.EnvController;
import main.com.app.root.env.EnvData;
import main.com.app.root.env.tree.TreeController;
import main.com.app.root.env.tree.TreeData;
import main.com.app.root.env.tree.TreeGenerator;
import main.com.app.root.env.world.Chunk;
import main.com.app.root.env.world.Water;
import main.com.app.root.env.world.WorldGenerator;
import main.com.app.root.mesh.Mesh;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.joml.Vector3f;

public class Spawner {
    private enum SpawnType {
        TREE,
        NULL,
        TEST
    }

    private final Tick tick;
    public Mesh mesh;
    private EnvController envController;

    private Vector3f centerPosition;
    private int maxObjs;
    private float spawnRadius;
    private boolean isActive;

    private final Random random;
    private float spawnTimer;
    private float spawnRate = 100.0f;
    private float minSpawnDistance = 80.0f;
    private float maxSpawnDistance = 500.0f;

    private SpawnType currentType = SpawnType.TREE;
    public TreeData treeData;

    private Map<String, List<TreeController>> chunkTreeMap = new HashMap<>();
    private static Map<Integer, Float> LEVEL_DISTRIBUTION;

    private static final float TREE_COVERAGE = 0.0005f;
    public static final int MAX_TREES_PER_CHUNK = (int)(Chunk.CHUNK_SIZE * Chunk.CHUNK_SIZE * TREE_COVERAGE);

    public Spawner(
        Tick tick,
        Mesh mesh,
        Vector3f centerPosition,
        int maxObjs,
        float spawnRadius
    ) {
        this.tick = tick;
        this.mesh = mesh;

        this.centerPosition = centerPosition;
        this.maxObjs = maxObjs;
        this.spawnRadius = spawnRadius;

        this.random = new Random();
        LEVEL_DISTRIBUTION = new HashMap<>();
        setLevelDistribution();

        this.treeData = new TreeData();
        this.treeData.createDefaultConfigs();
        this.isActive = true;
    } 

    public void setEnvController(EnvController envController) {
        this.envController = envController;
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
            Object treeGenerator = EnvCall.callReturn(tree, "getGenerator");
            
            if(treeGenerator == null) {
                System.out.println("  Tree " + checked + ": generator is NULL");
                continue;
            }
            
            boolean isAlive = (Boolean) EnvCall.callReturn(treeGenerator, "isAlive");
            if(!isAlive) {
                System.out.println("  Tree " + checked + ": not alive");
                continue;
            }
            
            aliveCount++;
            Vector3f treePos = (Vector3f) EnvCall.callReturn(treeGenerator, "getPosition");
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
        Object oldGenerator = EnvCall.callReturn(oldTree, "getGenerator");
        EnvCall.call(oldGenerator, "cleanup");

        Vector3f position = (Vector3f) EnvCall.callReturn(oldGenerator, "getPosition");
        TreeData data = treeData.configs.get(newLevel);
        if(data != null) {
            Object treeInstance = envController.getEnv(EnvData.TREE).getInstance();
            Object treeGenerator = EnvCall.callReturn(treeInstance, "getGenerator");
            
            EnvCall.callReturn(treeGenerator, "tree_" + treeData.currentTreeId++, "setId");
            treeData.trees.add((TreeController) treeInstance);

            /*
            System.out.println("Respawned tree from level " + 
                              EnvCall.callReturn(oldGenerator, "getLevel") + 
                              " to level " + newLevel);
                              */
        }
    }

    public void respawnTreeAtPos(Vector3f position, int level) {
        TreeData data = treeData.configs.get(level);
        if(data != null) {
            Object treeInstance = envController.getEnv(EnvData.TREE).getInstance();
            Object treeGenerator = EnvCall.callReturn(treeInstance, "getGenerator");
            
            Object[] params = new Object[]{data, position, mesh};
            EnvCall.callWithParams(treeGenerator, params, "createGenerator");
            
            EnvCall.callReturn(treeGenerator, "tree_" + treeData.currentTreeId++, "setId");
            treeData.trees.add((TreeController) treeInstance);

            /*
            System.out.println("Respawned tree at position [" + position.x + 
                              ", " + position.z + "] at level " + level);
                              */
        }
    }

    public void setCenterPos(Vector3f newCenter) {
        this.centerPosition = newCenter;
        /*
        System.out.println("Spawner center moved to [" + 
                          newCenter.x + ", " + newCenter.z + "]");
                          */
    }

    public void setActive(boolean active) {
        this.isActive = active;
        System.out.println("Spawner " + (active ? "activated" : "deactivated"));
    }

    public void setMaxObjs(int max) {
        this.maxObjs = max;
        adjustObjCount();
    }

    public void setSpawnRate(float rate) {
        this.spawnRate = Math.max(0.1f, rate);
    }

    public void setSpawnDistances(float min, float max) {
        this.minSpawnDistance = Math.max(1.0f, min);
        this.maxSpawnDistance = Math.max(minSpawnDistance + 1.0f, max);
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
            Object treeGenerator = EnvCall.callReturn(tree, "getGenerator");
            boolean isAlive = (Boolean) EnvCall.callReturn(treeGenerator, "isAlive");
            if(isAlive) count++;
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

    public boolean isActive() {
        return isActive;
    }
    
    public Vector3f getCenterPosition() {
        return new Vector3f(centerPosition);
    }
    
    public int getMaxObjects() {
        return maxObjs;
    }
    
    public float getSpawnRadius() {
        return spawnRadius;
    }

    private void adjustObjCount() {
        if(treeData.trees.size() > maxObjs) {
            int toRemove = treeData.trees.size() - maxObjs;
            //System.out.println("Removing " + toRemove + " excess trees");
            
            for(int i = 0; i < toRemove && !treeData.trees.isEmpty(); i++) {
                TreeController tree = treeData.trees.remove(0);
                Object treeGenerator = EnvCall.callReturn(tree, "getGenerator");
                EnvCall.call(treeGenerator, "cleanup");
            }
        }
    }

    public void printSpawnerStatus() {
        /*
        System.out.println("=== SPAWNER STATUS ===");
        System.out.println("Active: " + isActive);
        System.out.println("Center: [" + centerPosition.x + ", " + centerPosition.z + "]");
        System.out.println("Trees: " + getActiveTreeCount() + " alive / " + 
                          treeData.trees.size() + " total (max: " + maxObjs + ")");
        System.out.println("Radius: " + spawnRadius);
        System.out.println("======================");
        */
    }

    /**
     * Spawn Tree At Level
     */
    private void spawnTreeAtLevel(Vector3f position, int level) {
        if(!isActive) return;
        if(treeData.trees.size() >= maxObjs) return;

        TreeData data = treeData.configs.get(level);
        if(data == null) {
            data = treeData.configs.get(0);
        }

        TreeController treeController = new TreeController();
        treeController.createGenerator(data, position, mesh, this);
        
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
    public void handleTreeBreak(Vector3f position, int currLevel) {
        cleanupTreeAtPos(position);
        
        int nextLevel = currLevel + 1;
        if(!treeData.configs.containsKey(nextLevel)) nextLevel = 0;
        
        spawnTreeAtLevel(position, nextLevel);
    }

    /**
     * Cleanup Tree At Pos
     */
    private void cleanupTreeAtPos(Vector3f position) {
        float cleanupRadius = 1.0f;
        for(Iterator<TreeController> iterator = treeData.trees.iterator(); iterator.hasNext();) {
            TreeController tree = iterator.next();
            Object treeGenerator = EnvCall.callReturn(tree, "getGenerator");
            if(treeGenerator == null) continue;
            
            Vector3f treePos = (Vector3f) EnvCall.callReturn(treeGenerator, "getPosition");
            float distance = treePos.distance(position);
            
            if(distance <= cleanupRadius) {
                EnvCall.call(treeGenerator, "cleanup");
                iterator.remove();
                System.out.println("Cleaned up tree at position [" + position.x + ", " + position.z + "]");
                break;
            }
        }
    }

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

    public static Random Deterministic(int chunkX, int chunkZ) {
        return new Random(chunkX * 7919L + chunkZ * 131071L);
    }

    private float getHeightAt(float worldX, float worldZ) {
        Object mapInstance = envController.getEnv(EnvData.MAP).getInstance();
        Object worldGenerator = EnvCall.callReturn(mapInstance, "getGenerator");
        
        Object[] heightParams = new Object[]{worldX, worldZ};
        Float height = (Float) EnvCall.callReturnWithParams(worldGenerator, heightParams, "getHeightAt");
        
        return height != null ? height : Water.LEVEL;
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
            this
        );

        TreeGenerator treeGenerator = treeController.getGenerator();
        if(treeGenerator != null) {
            treeGenerator.mesh = this.mesh;
            String treeId = "tree_" + chunkX + "_" + chunkZ + "_" + treeData.trees.size();
            treeGenerator.setId(treeId);
            treeGenerator.createMesh();
            treeData.trees.add(treeController);
        } 
    }

    /**
     * Clear Trees
     */
    public void clearChunkTrees() {
        for(TreeController tree : treeData.trees) {
            Object treeGenerator = EnvCall.callReturn(tree, "getGenerator");
            if(treeGenerator != null) {
                EnvCall.call(treeGenerator, "cleanup");
            }
        }
        treeData.trees.clear();
    }

    /**
     * Update
     */
    public void update() {
        if(!isActive) return;

        Iterator<TreeController> iterator = treeData.trees.iterator();
        while(iterator.hasNext()) {
            TreeController tree = iterator.next();
            Object treeGenerator = EnvCall.callReturn(tree, "getGenerator");

            boolean isAlive = (Boolean) EnvCall.callReturn(treeGenerator, "isAlive");
            if(!isAlive) {
                iterator.remove();
                continue;
            }
            
            Object[] updateParams = new Object[]{tick.getDeltaTime()};
            EnvCall.callWithParams(treeGenerator, updateParams, "update");
        }
    }

    /**
     * Generate
     */
    public void generate(int chunkX, int chunkZ) {
        Object mapInstance = envController.getEnv(EnvData.MAP).getInstance();
        Object worldGenerator = EnvCall.callReturn(mapInstance, "getGenerator");

        float worldStartX = chunkX * Chunk.CHUNK_SIZE - WorldGenerator.WORLD_SIZE / 2.0f;
        float worldStartZ = chunkZ * Chunk.CHUNK_SIZE - WorldGenerator.WORLD_SIZE / 2.0f;
        float worldEndX = worldStartX + Chunk.CHUNK_SIZE;
        float worldEndZ = worldStartZ + Chunk.CHUNK_SIZE;

        Random random = Deterministic(chunkX, chunkZ);

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
     * Render
     */
    public void render() {
        if(!isActive) return;
        for(TreeController tree : treeData.trees) {
            Object treeGenerator = EnvCall.callReturn(tree, "getGenerator");
            boolean isAlive = (Boolean) EnvCall.callReturn(treeGenerator, "isAlive");
            if(isAlive) {
                EnvCall.call(treeGenerator, "render");
            }
        }
    }

    public void clearTrees() {
        for(TreeController tree : treeData.trees) {
            Object treeGenerator = EnvCall.callReturn(tree, "getGenerator");
            if(treeGenerator != null) {
                EnvCall.call(treeGenerator, "cleanup");
            }
        }
        treeData.trees.clear();
        treeData.currentTreeId = 0;
        System.out.println("Cleared all trees for save loading");
    }
}
