package main.com.app.root.env.axe;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joml.Vector3f;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

public class AxeData {
    private static final String DATA_PATH = "root/src/main/com/app/root/_data/axe_data.lua";
    private static final String OBJ_PATH = "root/src/main/com/app/root/mesh/obj_list.lua";

    public String name;
    public final String indexTo;
    public int level;
    public final float damage;
    public final float speed;
    public float woodMultiplier;
    public String currModel;
    public String texturePath;
    public Vector3f scale;
    public int upgradeCost;

    public final List<AxeController> axes;
    public final Map<Integer, AxeData> configs;
    public int currentAxeId;
    
    public AxeData(
        String name,
        String indexTo,
        int level,
        float damage,
        float speed,
        int woodMultiplier,
        String currModel,
        String texturePath,
        int upgradeCost,
        Vector3f scale
    ) {
        this.name = name;
        this.indexTo = indexTo;
        this.level = level;
        this.damage = damage;
        this.speed = speed;
        this.woodMultiplier = woodMultiplier;
        this.currModel = currModel;
        this.texturePath = texturePath;
        this.scale = scale;

        this.axes = new ArrayList<>();
        this.configs = new HashMap<>();
        this.currentAxeId = 0;
    }
    public AxeData(
        String indexTo,
        int level,
        float damage,
        float speed,
        int woodMultiplier,
        int upgradeCost
    ) {
        this.indexTo = indexTo;
        this.level = level;
        this.damage = damage;
        this.speed = speed;
        this.woodMultiplier = woodMultiplier;
        this.upgradeCost = upgradeCost;

        this.axes = new ArrayList<>();
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
                    AxeData dataInstance = new AxeData(
                        data.get("indexTo").tojstring(),
                        data.get("level").toint(),
                        (float) data.get("damage").toint(),
                        (float) data.get("speed").toint(),
                        data.get("wood_multiplier").toint(),
                        data.get("upgrade_cost").toint()
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
            AxeData data = new AxeData(
                "axe" + l,
                OBJ_PATH + l,
                l,
                damage,
                speed,
                (int) woodMultiplier,
                OBJ_PATH + l + ".obj",
                OBJ_PATH + l + ".png",
                upgradeCost,
                scale
            );
            configs.put(l, data);
        }
    }

    public String getIndexTo() { 
        return indexTo; 
    }

    public String getName() { 
        return name; 
    }

    public int getLevel() { 
        return level; 
    }

    public float getDamage() { 
        return damage; 
    }

    public String getCurrentModel() { 
        return currModel; 
    }

    public String getTexturePath() { 
        return texturePath; 
    }

    public Vector3f getScale() { 
        return scale; 
    }

    @Override
    public String toString() {
        return indexTo + " (Lvl " + level + ", HP: " + damage + ")";
    }
}
