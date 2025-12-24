package main.com.app.root.env.tree;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joml.Vector3f;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

public class TreeData {
    private static final String DATA_PATH = "root/src/main/com/app/root/_data/tree_data.lua";
    private static final String OBJ_LIST_PATH = "root/src/main/com/app/root/_data/obj_list";
    private static final String OBJ_PATH = "root/src/main/com/app/root/_resources/obj/tree/tree";
    private static final String TEX_PATH = "root/src/main/com/app/root/_resources/texture/tree/tree";

    private String name;
    private String indexTo;
    private int level;
    private float health;
    private int woodMin;
    private int woodMax;
    private float respawnTime;
    private String modelPath;
    private String texturePath;
    private Vector3f scale;

    public List<TreeController> trees;
    public Map<Integer, TreeData> configs;
    public int currentTreeId;
    
    public TreeData() {}
    public TreeData(
        String name,
        String indexTo,
        int level,
        float health,
        int woodMin,
        int woodMax,
        float respawnTime,
        String modelPath,
        String texturePath,
        Vector3f scale
    ) {
        this.name = name;
        this.indexTo = indexTo;
        this.level = level;
        this.health = health;
        this.woodMin = woodMin;
        this.woodMax = woodMax;
        this.respawnTime = respawnTime;
        this.modelPath = modelPath;
        this.texturePath = texturePath;
        this.scale = scale;

        this.trees = new ArrayList<>();
        this.configs = new HashMap<>();
        this.currentTreeId = 0;
    }
    public TreeData(
        String indexTo,
        int level,
        float health,
        int woodMin,
        int woodMax,
        float respawnTime
    ) {
        this.indexTo = indexTo;
        this.level = level;
        this.health = health;
        this.woodMin = woodMin;
        this.woodMax = woodMax;
        this.respawnTime = respawnTime;

        this.trees = new ArrayList<>();
        this.configs = new HashMap<>();
    }

    /**
     * Load Config Data
     */
    public void loadConfigData() {
        try {
            Globals globals = JsePlatform.standardGlobals();
            LuaValue chunk = globals.loadfile(DATA_PATH);
            LuaValue result = chunk.call();

            if(result.istable()) {
                for(int i = 1; i <= result.length(); i++) {
                    LuaValue data = result.get(i);
                    TreeData dataInstance = new TreeData(
                        data.get("indexTo").tojstring(),
                        data.get("level").toint(),
                        (float)data.get("health").todouble(),
                        data.get("wood_min").toint(),
                        data.get("wood_max").toint(),
                        (float)data.get("respawn_time").todouble()
                    );
                    configs.put(getLevel(), dataInstance);
                } 
            }
        } catch(Exception err) {
            System.err.println("Failed to load tree configs: " + err.getMessage());
        }
    }

    /**
     * Default Configs
     */
    public void createDefaultConfigs() {
        System.out.println("Creating default tree configurations...");
        for(int l = 0; l <= 10; l++) {
            TreeData data = new TreeData(
                "tree" + l,
                OBJ_LIST_PATH + l,
                l,
                100 + (l * 50),
                10 + (l * 2),
                20 + (l * 2),
                30.0f + (l * 10),
                OBJ_PATH + l + ".obj",
                TEX_PATH + l + ".png",
                new Vector3f(1.0f + (l * 0.1f), 1.0f + (l * 0.1f), 1.0f + (l * 0.1f))
            );
            configs.put(l, data);
        }
    }

    public String getName() { 
        return name; 
    }
    
    public String getIndexTo() { 
        return indexTo; 
    }

    public int getLevel() { 
        return level; 
    }

    public float getHealth() { 
        return health; 
    }

    public int getWoodMin() { 
        return woodMin; 
    }
    public int getWoodMax() { 
        return woodMax; 
    }

    public float getRespawnTime() { 
        return respawnTime; 
    }

    public String getModelPath() { 
        return modelPath; 
    }

    public String getTexturePath() { 
        return texturePath; 
    }

    public Vector3f getScale() { 
        return scale; 
    }

    @Override
    public String toString() {
        return indexTo + " (Lvl " + level + ", HP: " + health + ")";
    }
}
