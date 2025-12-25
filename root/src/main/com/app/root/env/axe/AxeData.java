package main.com.app.root.env.axe;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

public class AxeData {
    private static final String DATA_PATH = "root/src/main/com/app/root/_data/axe_data.lua";
    private static final String OBJ_LIST_PATH = "root/src/main/com/app/root/_data/obj_list.lua";
    private static final String OBJ_PATH = "root/src/main/com/app/root/_resources/item/";
    private static final String TEX_PATH = "root/src/main/com/app/root/_resources/texture/item/";

    public String indexTo;
    public int level;
    public float damage;
    public float speed;
    public float woodMultiplier;
    public int upgradeCost;

    public final List<AxeController> axes;
    public final Map<Integer, AxeData> configs;
    private Map<Integer, Integer> upgradeCostsByLevel;
    public int currentAxeId;
    
    public AxeData(
        String indexTo,
        int level,
        float damage,
        float speed,
        float woodMultiplier,
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
        this.upgradeCostsByLevel = new HashMap<>();
        this.currentAxeId = 0;
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
                        (float)data.get("damage").todouble(),
                        (float)data.get("speed").todouble(),
                        (float)data.get("wood_multiplier").todouble(),
                        data.get("upgrade_cost").toint()
                    );
                    configs.put(dataInstance.getLevel(), dataInstance);
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
        //System.out.println("Creating default axe configurations...");
        for(int l = 0; l <= 10; l++) {
            String indexTo = "axe" + l;

            AxeData data = new AxeData(
                indexTo,
                l,
                10.0f + (l * 5),
                1.0f + (l * 0.1f),
                1 + l,
                100 + (l * 50)
            );
            configs.put(l, data);
            upgradeCostsByLevel.put(l, data.upgradeCost);
        }
    }

    public String getIndexTo() { 
        return indexTo; 
    }

    public int getLevel() { 
        return level; 
    }

    public float getDamage() { 
        return damage; 
    }

    public int getUpgradeCostForLevel(int targetLevel) {
        if(configs.containsKey(targetLevel)) {
            return configs.get(targetLevel).upgradeCost;
        }
        
        return 100 + (targetLevel * 50);
    }

    @Override
    public String toString() {
        return indexTo + " (Lvl " + level + ", HP: " + damage + ")";
    }
}
