package main.com.app.root._save;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;

import main.com.app.root.DataController;
import main.com.app.root.StateController;
import main.com.app.root.env.EnvController;
import main.com.app.root.player_controller.PlayerController;

public class DataGetter {
    public final DataController dataController;
    public final StateController stateController;
    public final EnvController envController;
    public final PlayerController playerController;

    public DataGetter(
        DataController dataController,
        StateController stateController,
        EnvController envController,
        PlayerController playerController
    ) {
        this.dataController = dataController;
        this.stateController = stateController;
        this.envController = envController;
        this.playerController = playerController;
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
        if(data.containsKey("player")) {
            applyPlayerData((Map<String, Object>) data.get("player"));
        }
        if(data.containsKey("world")) {
            applyWorldData((Map<String, Object>) data.get("world"));
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
    }

    public void applyPlayerData(Map<String, Object> data) {
        if(data.containsKey("position_x") && playerController != null) {
            float x = ((Number) data.get("position_x")).floatValue();
            float y = ((Number) data.get("position_y")).floatValue();
            float z = ((Number) data.get("position_z")).floatValue();
            playerController.setPosition(x, y, z);

            if(data.containsKey("items")) {
                List<String> items = (List<String>) data.get("items");
            }
        }
    }
}
