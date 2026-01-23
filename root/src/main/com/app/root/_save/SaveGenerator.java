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
        SaveNameResult result = generateSaveId(saveName);
        String saveId = result.saveId;
        String finalSaveName = result.saveName;

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
        saveFile.setSaveInfo("save_name", finalSaveName);
        saveFile.setSaveInfo("creation_date", creationDate);
        saveFile.setSaveInfo("version", "beta_1.1.0"); //Change this later;;;;;
        saveFile.setSaveInfo("last_played", saveFile.getSaveInfo("creation_date"));
        saveFile.setSaveInfo("play_time", "00:00:00");

        saveInitData(saveFile);
        saveFile.saveSaveInfo();

        System.out.println("New save created" + saveId);
        return saveId;
    }

    private boolean isSaveNameTaken(
        String saveId, 
        String saveName,
        List<String> existingSaves
    ) {
        if(existingSaves.contains(saveId)) return true;

        if(!saveId.contains("_")) {
            for(String existingSaveId : existingSaves) {
                try {
                    SaveFile existingSave = new SaveFile(existingSaveId);
                    String existingSaveName = existingSave.getSaveInfo("save_name");
                    if(saveName.equals(existingSaveName)) return true;
                } catch(Exception e) {
                    continue;
                }
            }
        }

        return false;
    }

    public SaveNameResult generateSaveId(String saveName) {
        if(saveName == null || saveName.trim().isEmpty()) {
            saveName = "New World";
        }

        String baseId = saveName
            .replaceAll("[<>:\"/\\\\|?*]", "")
            .replaceAll("^\\s+|\\s+$", "")
            .replaceAll("\\.+$", ""); 
        
        String finalSaveId = baseId;
        String finalSaveName = saveName;
        int counter = 1;
        List<String> existingSaves = SaveFile.listAllSaves();

        while(isSaveNameTaken(finalSaveId, finalSaveName, existingSaves)) {
            finalSaveId = baseId + "_" + counter;
            finalSaveName = saveName + "_" + counter;
            counter++;
        }

        return new SaveNameResult(finalSaveId, finalSaveName);
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
        saveFile.saveObject( //Change this later.....!
            "stats",
            "st.data",
           (Serializable) worldData
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
            if(mainData != null) {
                saveFile.saveObject(
                    "main",
                    "m.data",
                    (Serializable) mainData
                );
                System.out.println("MainData saved: wood=" + mainData.getWood() + ", axeLevel=" + mainData.getAxeLevel());
            } else {
                System.out.println("Warning: MainData is null, cannot save");
            }
        } else {
            System.out.println("Warning: Upgrader is null, cannot save MainData");
        }
        
        /* World Data */
        Map<String, Object> worldData = dataGetter.getWorldData();
        saveFile.saveObject(
            "world",
            "w.data",
            (Serializable) worldData
        );
        System.out.println("World data saved with " + 
            ((worldData.containsKey("trees")) ? 
                ((List<?>) worldData.get("trees")).size() : 0) + " trees");
        
        /* Player Data */
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
