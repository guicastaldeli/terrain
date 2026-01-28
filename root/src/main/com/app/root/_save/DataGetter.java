package main.com.app.root._save;
import main.com.app.root.DataController;
import main.com.app.root.Scene;
import main.com.app.root.Spawner;
import main.com.app.root.SpawnerHandler;
import main.com.app.root.StateController;
import main.com.app.root.Upgrader;
import main.com.app.root.env.EnvController;
import main.com.app.root.player.PlayerController;
import org.joml.Vector3f;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataGetter {
    public final DataController dataController;
    public final StateController stateController;
    private final Scene scene;
    public EnvController envController;

    public Spawner spawner;
    public PlayerController playerController;
    public Upgrader upgrader;

    public DataGetter(
        DataController dataController,
        StateController stateController,
        Scene scene
    ) {
        this.dataController = dataController;
        this.stateController = stateController;
        this.scene = scene;
    }

    public void setEnvController(EnvController envController) {
        this.envController = envController;
    }

    public void setUpgrader(Upgrader upgrader) {
        this.upgrader = upgrader;
    }

    public void setPlayerController(PlayerController playerController) {
        this.playerController = playerController;
    }

    public void setSpawner(Spawner spawner) {
        this.spawner = spawner;
    }

    /**
     * Get All Data
     */
    public Map<String, Object> getAllData() {
        Map<String, Object> data = new HashMap<>();
        data.put("world", getWorldData());
        data.put("player", getPlayerData());
        data.put("state", getStateData());
        data.put("stats", getStatsData());
        return data;
    }

    /**
     * World Data
     */
    public Map<String, Object> getWorldData() {
        Map<String, Object> data = new HashMap<>();
        data.put("seed", dataController.getWorldSeed());
        data.put("time", dataController.getWorldTime());
        data.put("current_level", stateController.getCurrentLevel());
        
        if(spawner != null) {
            Vector3f spawnerCenter = spawner.getCenterPosition();
            data.put("spawner_center_x", spawnerCenter.x);
            data.put("spawner_center_y", spawnerCenter.y);
            data.put("spawner_center_z", spawnerCenter.z);
            data.put("spawner_radius", spawner.getSpawnRadius());
            for(List<SpawnerHandler> handlers : Spawner.getInstance().spawnerData.values()) {
                for(SpawnerHandler handler : handlers) {
                    data.put("spawner_active", handler.isActive());
                }
            }

            for(List<SpawnerHandler> handlers : Spawner.getInstance().spawnerData.values()) {
                for(SpawnerHandler handler : handlers) {
                    handler.getData(data);
                }
            }
        }
        
        return data;
    }

    /**
     * Player Data
     */
    public Map<String, Object> getPlayerData() {
        Map<String, Object> data = new HashMap<>();
        if(playerController != null) {
            Vector3f pos = playerController.getPosition();
            data.put("position_x", pos.x);
            data.put("position_y", pos.y);
            data.put("position_z", pos.z);

            Vector3f rotation = playerController.getPlayerMesh().getMeshRotation();
            data.put("rotation_x", rotation.x);
            data.put("rotation_y", rotation.y);
            data.put("rotation_z", rotation.z);
        }
        if(upgrader != null) {
            data.put("wood", upgrader.getWood());
            data.put("axe_level", upgrader.getAxeLevel());
        }
        data.put("items", dataController.getItems());
        return data;
    }

    /**
     * State Data
     */
    public Map<String, Object> getStateData() {
        Map<String, Object> data = new HashMap<>();
        //data.put("items", dataController.getItems()); //implement this right later
        return data;
    }

    /**
     * Stats Data
     */
    public Map<String, Object> getStatsData() {
        Map<String, Object> data = new HashMap<>();
        data.put("play_time", dataController.getPlayTimeSecs());
        //data.put("items_collected", dataController.getItems()); //implement this right later
        return data;
    }

    /**
     * 
     * Apply Data
     * 
     */
    public void applyData(Map<String, Object> data) {  
        if(data.containsKey("world")) {
            applyWorldData((Map<String, Object>) data.get("world"));
        }
        if(data.containsKey("player")) {
            applyPlayerData((Map<String, Object>) data.get("player"));
        }
    }

    public void applyWorldData(Map<String, Object> data) {
        if(data.containsKey("seed")) {
            dataController.setWorldSeed(((Number) data.get("seed")).longValue());
        }
        if(data.containsKey("time")) {
            dataController.setWorldTime(((Number) data.get("time")).longValue());
        }
        if(data.containsKey("current_level")) {
            stateController.setCurrentLevel((String) data.get("current_level"));
        }
        
        if(data.containsKey("spawner_center_x") && spawner != null) {
            float x = ((Number) data.get("spawner_center_x")).floatValue();
            float y = ((Number) data.get("spawner_center_y")).floatValue();
            float z = ((Number) data.get("spawner_center_z")).floatValue();
            spawner.setCenterPosition(new Vector3f(x, y, z));
        }
        if(data.containsKey("spawner_radius") && spawner != null) {
            float radius = ((Number) data.get("spawner_radius")).floatValue();
            spawner.setSpawnDistances(spawner.getSpawnRadius(), radius);
        }
        if(data.containsKey("spawner_active") && spawner != null) {
            boolean active = (Boolean) data.get("spawner_active");
            for(List<SpawnerHandler> handlers : Spawner.getInstance().spawnerData.values()) {
                for(SpawnerHandler handler : handlers) {
                    handler.setActive(active);
                }
            }
        }
        
        for(List<SpawnerHandler> handlers : Spawner.getInstance().spawnerData.values()) {
            for(SpawnerHandler handler : handlers) {
                handler.applyData(data);
            }
        }
    }

    public void applyPlayerData(Map<String, Object> data) {
        if(data.containsKey("position_x") && playerController != null) {
            float x = ((Number) data.get("position_x")).floatValue();
            float y = ((Number) data.get("position_y")).floatValue();
            float z = ((Number) data.get("position_z")).floatValue();
            playerController.setPosition(x, y, z);
            
            if(data.containsKey("rotation_x")) {
                float rx = ((Number) data.get("rotation_x")).floatValue();
                float ry = ((Number) data.get("rotation_y")).floatValue();
                float rz = ((Number) data.get("rotation_z")).floatValue();
                playerController.getPlayerMesh().setMeshRotation(rx, ry, rz);
            }
        }
        if(upgrader != null) {
            if(data.containsKey("wood")) {
                int wood = ((Number) data.get("wood")).intValue();
                upgrader.setWood(wood);
            }
            if(data.containsKey("axe_level")) {
                int axeLevel = ((Number) data.get("axe_level")).intValue();
                upgrader.setAxeLevel(axeLevel);
            }
        }
    }
}
