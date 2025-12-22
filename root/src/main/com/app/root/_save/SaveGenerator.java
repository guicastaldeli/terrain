package main.com.app.root._save;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.text.SimpleDateFormat;
import main.com.app.root.DataController;
import main.com.app.root.StateController;

public class SaveGenerator {
    private final DataController dataController;
    private final StateController stateController;
    private final DataGetter dataGetter;

    public SaveGenerator(
        DataController dataController,
        StateController stateController,
        DataGetter dataGetter
    ) {
        this.dataController = dataController;
        this.stateController = stateController;
        this.dataGetter = dataGetter;
    }

    /**
     * Generate New Save
     */
    public String generateNewSave(String saveName) throws IOException {
        String saveId = generateSaveId(saveName);
        SaveFile saveFile = new SaveFile(saveId);
        saveFile.createSaveDir();

        /*
        Object instance = envController.getEnv(EnvData.MAP).getInstance();
        EnvCall.call(instance, "getGenerator", "generateNewMap");
        */

        stateController.setCurrentSaveId(saveId);
        stateController.setLoadInProgress(true);

        String creationDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        saveFile.setSaveInfo("save_name", saveName);
        saveFile.setSaveInfo("creation_date", saveId);
        saveFile.setSaveInfo("version", "beta_1.0.0"); //Change this
        saveFile.setSaveInfo("last_played", saveFile.getSaveInfo("creation_date"));
        saveFile.setSaveInfo("play_time", "00:00:00");

        saveInitData(saveFile);
        saveFile.saveSaveInfo();

        System.out.println("New save created" + saveId);
        return saveId;
    }

    public String generateSaveId(String saveName) {
        String baseId = saveName.toLowerCase()
            .replaceAll("[^a-z0-9]", "_")
            .replaceAll("_+", "_")
            .replaceAll("^_|_$", "");

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return baseId + "_" + timestamp;
    }

    /**
     * Save Init Data
     */
    private void saveInitData(SaveFile saveFile) throws IOException {
        /* Data */
        saveFile.saveObject(
            "data", 
            "s.data", 
            dataController
        );
        /* World */
        Map<String, Object> worldData = dataGetter.getWorldData();
        saveFile.saveObject(
            "world",
            "w.data",
            (Serializable) worldData
        );
        /* Player */
        Map<String, Object> playerData = dataGetter.getPlayerData();
        saveFile.saveObject(
            "player",
            "p.data",
            (Serializable) playerData
        );
        /* Stats */
        saveFile.saveData(
            "stats",
            "st.data",
            new byte[0]
        );
    }

    /**
     * Save
     */
    public void save(String saveId) throws IOException {
        SaveFile saveFile = new SaveFile(saveId);
        if(!saveFile.exists()) {
            throw new IOException("Save file does not exist: " + saveId);
        }

        String lastPlayed = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        saveFile.setSaveInfo("last_played", lastPlayed);
        saveFile.setSaveInfo("play_time", dataController.getFormattedPlayTime());

        /* Save Data */
        saveFile.saveObject(
            "data",
            "s.data",
            dataController
        );
        /* Save Player Data */
        Map<String, Object> playerData = dataGetter.getPlayerData();
        saveFile.saveObject(
            "player",
            "p.data",
            (Serializable) playerData
        );

        saveFile.saveSaveInfo();
        System.out.println("Saved: " + saveId);
    }
}
