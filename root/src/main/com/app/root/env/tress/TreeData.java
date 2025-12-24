package main.com.app.root.env.tress;

import org.joml.Vector3f;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

public class TreeData {
    private static final String DATA_PATH = "root/src/main/com/app/root/_data/tree_data.lua";

    private final String name;
    private final String indexTo;
    private final int level;
    private final float health;
    private final int woodMin;
    private final int woodMax;
    private final float respawnTime;
    private final String modelPath;
    private final String texturePath;
    private final Vector3f scale;
    
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
                    if(data.istable()) {
                        data.get("indexTo").tojstring();
                        data.get("level").toint();
                        data.get("health").todouble();
                        data.get("wood_min").toint();
                        data.get("wood_max").toint();
                        data.get("respawn_time").todouble();
                    }
                } 
            }
        } catch(Exception err) {
            System.err.println("Failed to load tree configs: " + err.getMessage());
        }
    }

    public String getIndexTo() { return indexTo; }
    public int getLevel() { return level; }
    public float getHealth() { return health; }
    public int getWoodMin() { return woodMin; }
    public int getWoodMax() { return woodMax; }
    public float getRespawnTime() { return respawnTime; }

    @Override
    public String toString() {
        return indexTo + " (Lvl " + level + ", HP: " + health + ")";
    }
}
