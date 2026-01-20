package main.com.app.root._save;
import main.com.app.root.DataController;
import main.com.app.root.MainData;
import main.com.app.root.Scene;
import main.com.app.root.StateController;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.text.DateFormat;

public class SaveGenerator {
    private final DataController dataController;
    private final StateController stateController;
    private final DataGetter dataGetter;

    private Scene scene;

    public SaveGenerator(
        DataController dataController,
        StateController stateController,
        DataGetter dataGetter
    ) {
        this.dataController = dataController;
        this.stateController = stateController;
        this.dataGetter = dataGetter;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    /**
     * Generate New Save
     */
    public String generateNewSave(String saveName) throws IOException {
        String saveId = generateSaveId(saveName);
        SaveFile saveFile = new SaveFile(saveId);
        saveFile.createSaveDir();

        stateController.setCurrentSaveId(saveId);
        stateController.setLoadInProgress(true);

        scene.reset();
        dataController.reset();

        Date currentDate = new Date();
        String creationDate = DateFormat.getDateTimeInstance(
            DateFormat.DEFAULT,
            DateFormat.DEFAULT,
            Locale.getDefault()
        ).format(currentDate);
        saveFile.setSaveInfo("save_name", saveName);
        saveFile.setSaveInfo("creation_date", creationDate);
        saveFile.setSaveInfo("version", "beta_1.0.0"); //Change this later;;;;;
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

        String finalSaveId = baseId;
        int counter = 1;
        List<String> existingSaves = SaveFile.listAllSaves();
        while(existingSaves.contains(finalSaveId)) {
            finalSaveId = baseId + counter;
            counter++;
        }

        return finalSaveId;
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
        /* Main Data */
        if(dataGetter.upgrader != null) {
            MainData mainData = dataGetter.upgrader.getData();
            saveFile.saveObject(
                "main",
                "m.data",
                (Serializable) mainData
            );
        }
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

        Date currentDate = new Date();
        String lastPlayed = DateFormat.getDateTimeInstance(
            DateFormat.DEFAULT,
            DateFormat.DEFAULT,
            Locale.getDefault()
        ).format(currentDate);
        saveFile.setSaveInfo("last_played", lastPlayed);
        saveFile.setSaveInfo("play_time", dataController.getFormattedPlayTime());

        /* Save Data */
        saveFile.saveObject(
            "data",
            "s.data",
            dataController
        );
        /* Main Data */
        if(dataGetter.upgrader != null) {
            MainData mainData = dataGetter.upgrader.getData();
            saveFile.saveObject(
                "main",
                "m.data",
                (Serializable) mainData
            );
        }
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
