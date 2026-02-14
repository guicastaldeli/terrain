package main.com.app.root;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

public class MainDataLoader {
    private static final String DATA_PATH = "root/src/main/com/app/root/_data/main_data.lua";

    /**
     * Load (Defaults...)
     */
    public static MainData load() {
        try {
            Globals globals = JsePlatform.standardGlobals();
            LuaValue chunk = globals.loadfile(DATA_PATH);
            chunk.call();

            MainData data = new MainData();

            /* Player */
            LuaValue playerTable = globals.get("player");
            data.setWood(playerTable.get("wood").toint());
            data.setAxeLevel(playerTable.get("axe_level").toint());
            data.setCurrentAxe(playerTable.get("current_axe").tojstring());

            /* Upgrade */
            LuaValue upgradesTable = globals.get("upgrades");
            LuaValue costsTable = upgradesTable.get("costs");
            int[] costs = new int[costsTable.length()];
            for(int i = 1; i <= costsTable.length(); i++) {
                costs[i-1] = costsTable.get(i).toint();
            }
            data.setUpgradeCosts(costs);

            return data;
        } catch(Exception e) {
            System.err.println("Failed to load game data: " + e.getMessage());
            return createDefaultData();
        }
    }

    public static MainData createDefaultData() {
        MainData data = new MainData();
        data.setWood(0);
        data.setAxeLevel(0);
        data.setCurrentAxe("axe0");

        saveData(data);
        return data;
    }

    public static void saveData(MainData data) {
        try {
            String filePath = DATA_PATH;
            File dir = new File(DATA_PATH);
    
            try(FileWriter writer = new FileWriter(filePath)) {
                writer.write(data.toString());
            }
        } catch(IOException err) {
            System.err.println("Failed to save game data: " + err.getMessage());
            err.printStackTrace();
        }
    }
}
