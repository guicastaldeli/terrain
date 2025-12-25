package main.com.app.root;
import main.com.app.root.env.EnvCall;
import main.com.app.root.env.EnvController;
import main.com.app.root.env.EnvData;
import main.com.app.root.env.tree.TreeController;
import main.com.app.root.env.tree.TreeData;
import main.com.app.root.env.tree.TreeGenerator;
import main.com.app.root.mesh.Mesh;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.joml.Vector3f;

public class Spawner {
    private final Tick tick;
    private final Mesh mesh;
    private EnvController envController;

    private Vector3f centerPosition;
    private int maxObjs;
    private float spawnRadius;
    private boolean isActive;

    private final Random random;
    private final Map<Integer, Float> levelDistribution;

    private float spawnTimer;
    private float spawnRate = 2.0f;
    private float minSpawnDistance = 80.0f;
    private float maxSpawnDistance = 500.0f;

    private enum SpawnType {
        TREE
    }
    private SpawnType currentType = SpawnType.TREE;
    private TreeData treeData;

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
        this.levelDistribution = new HashMap<>();
        initLevelDistribution();

        this.treeData = new TreeData();
        this.treeData.createDefaultConfigs();
        this.isActive = true;
    }

    private void initLevelDistribution() {
        levelDistribution.put(0, 0.7f);
        levelDistribution.put(1, 0.15f);
        levelDistribution.put(2, 0.07f);
        levelDistribution.put(3, 0.04f);
        levelDistribution.put(4, 0.02f);
        levelDistribution.put(5, 0.01f);
        levelDistribution.put(6, 0.005f);
        levelDistribution.put(7, 0.002f);
        levelDistribution.put(8, 0.001f);
        levelDistribution.put(9, 0.0005f);
        levelDistribution.put(10, 0.0003f);
    }    
    
    /**
     * Initial Spawn
     */
    public void initialSpawn() {
        int treesToSpawn = Math.min(maxObjs, 50);
        //System.out.println("Initial spawn: Creating " + treesToSpawn + " trees...");
        for(int i = 0; i < treesToSpawn; i++) spawnSingleTree();
        //System.out.println("Spawner initialized with " + treeData.trees.size() + " trees");
    }

    /**
     * Spawn Single Tree
     */
    private void spawnSingleTree() {
        if(!isActive || treeData.trees.size() >= maxObjs) return;

        Vector3f position = genRandomPos();
        int level = genRandomLevel();

        TreeData data = treeData.configs.get(level);
        if(data == null) {
            data = treeData.configs.get(0);
            System.err.println("No config for level " + level + ", using level 0");
        }

        /**
         * 
         * 
         * TEMPORARY SOLUTION... SWITCH LATER
         * 
         * 
         */
        TreeController treeController = new TreeController();
        treeController.createGenerator(data, position, mesh, this);
        
        TreeGenerator treeGenerator = treeController.getGenerator();
        if(treeGenerator == null) {
            System.err.println("Failed to create tree generator for " + data.getIndexTo());
            return;
        }
        treeGenerator.mesh = this.mesh;
        
        String treeId = "tree" + treeData.currentTreeId++;
        treeGenerator.setId(treeId);

        treeData.trees.add(treeController);
        
        /*
        System.out.println("Successfully spawned " + data.getIndexTo() + 
                        " (Level " + level + ") at [" + 
                        position.x + ", " + position.z + "]");
                        */
    }

    /**
     * Generate Random Position
     */
    private Vector3f genRandomPos() {
        float angle = random.nextFloat() * (float) Math.PI * 2;
        float distance = minSpawnDistance + random.nextFloat() * (maxSpawnDistance - minSpawnDistance);
        
        float x = centerPosition.x + (float) Math.cos(angle) * distance;
        float z = centerPosition.z + (float) Math.sin(angle) * distance;
        
        Object mapInstance = envController.getEnv(EnvData.MAP).getInstance();
        Object mapGenerator = EnvCall.callReturn(mapInstance, "getGenerator");
        
        Object[] heightParams = new Object[]{x, z};
        Float height = (Float) EnvCall.callReturnWithParams(mapGenerator, heightParams, "getHeightAt");
        
        return new Vector3f(x, height, z);
    }

    /**
     * Get Random Level
     */
    private int genRandomLevel() {
        float totalWeight = 0;
        for(float weight : levelDistribution.values()) {
            totalWeight += weight;
        }

        float randomValue = random.nextFloat() * totalWeight;
        float cumulativeWeight = 0;

        for(Map.Entry<Integer, Float> entry : levelDistribution.entrySet()) {
            cumulativeWeight += entry.getValue();
            if(randomValue <= cumulativeWeight) {
                return entry.getKey();
            }
        }

        return 0;
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
        return new ArrayList<>(treeData.trees);
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
        if(!isActive || treeData.trees.size() >= maxObjs) return;

        TreeData data = treeData.configs.get(level);
        if(data == null) {
            data = treeData.configs.get(0);
            System.err.println("No config for level " + level + ", using level 0");
        }

        TreeController treeController = new TreeController();
        treeController.createGenerator(data, position, mesh, this);
        
        TreeGenerator treeGenerator = treeController.getGenerator();
        if(treeGenerator == null) {
            System.err.println("Failed to create tree generator for level " + level);
            return;
        }
        treeGenerator.mesh = this.mesh;
        
        String treeId = "tree" + treeData.currentTreeId++;
        treeGenerator.setId(treeId);
        
        treeData.trees.add(treeController);
        /*
        System.out.println("Spawned Level " + level + " tree at [" + 
                          position.x + ", " + position.z + "]");
                          */
    }

    /**
     * Handle Tree Break
     */
    public void handleTreeBreak(Vector3f position, int currLevel) {
        cleanupTreeAtPos(position);
        
        int nextLevel = currLevel + 1;
        if(!treeData.configs.containsKey(nextLevel)) {
            System.out.println("No config for level " + nextLevel + ", spawning base level instead");
            nextLevel = 0;
        }
        
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

    public void setEnvController(EnvController envController) {
        this.envController = envController;
    }

    /**
     * Update
     */
    public void update() {
        if(!isActive) return;

        spawnTimer -= tick.getDeltaTime();
        if(spawnTimer <= 0 && treeData.trees.size() < maxObjs) {
            spawnSingleTree();
            spawnTimer = spawnRate;
        }

        Iterator<TreeController> iterator = treeData.trees.iterator();
        while(iterator.hasNext()) {
            TreeController tree = iterator.next();
            Object treeGenerator = EnvCall.callReturn(tree, "getGenerator");

            boolean isAlive = (Boolean) EnvCall.callReturn(treeGenerator, "isAlive");
            if(!isAlive) {
                iterator.remove();
                continue;
            }

            Vector3f treePos = (Vector3f) EnvCall.callReturn(treeGenerator, "getPosition");
            float distance = treePos.distance(centerPosition);
            if(distance > spawnRadius * 1.5f) {
                EnvCall.call(treeGenerator, "cleanup");
                iterator.remove();
                //System.out.println("Removed distant tree at distance: " + distance);
                continue;
            }
            
            Object[] updateParams = new Object[]{tick.getDeltaTime()};
            EnvCall.callWithParams(treeGenerator, updateParams, "update");
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
}
