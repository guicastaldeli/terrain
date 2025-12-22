package main.com.app.root._save;
import main.com.app.root.DataController;
import main.com.app.root.StateController;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class SaveLoader {
    private final DataController dataController;
    private final StateController stateController;
    private final DataGetter dataGetter;

    public SaveLoader(
        DataController dataController,
        StateController stateController,
        DataGetter dataGetter
    ) {
        this.dataController = dataController;
        this.stateController = stateController;
        this.dataGetter = dataGetter;
    }

    /**
     * Load Save
     */
    public boolean loadSave(String saveId) {
        try {
            SaveFile saveFile = new SaveFile(saveId);
            if(!saveFile.exists()) {
                System.err.println("Save file not found: " + saveId);
                return false;
            }

            stateController.setCurrentLevel(saveId);
            stateController.setLoadInProgress(true);

            /* Load Data */
            DataController loadedData = (DataController) saveFile.loadObject(
                "data",
                "s.data"
            );
            if(loadedData != null) {
                copyDataController(loadedData, dataController);
            }
            /* Load World Data */
            if(saveFile.hasData("world", "w.data")) {
                Map<String, Object> worldData = 
                    (Map<String, Object>) 
                    saveFile.loadObject(
                        "world",
                        "w.data"
                    );
                dataGetter.applyWorldData(worldData);
            }
            /* Load Player Data */
            if(saveFile.hasData("player", "p.data")) {
                Map<String, Object> playerData = 
                    (Map<String, Object>) 
                    saveFile.loadObject(
                        "player",
                        "p.data"
                    );
                dataGetter.applyPlayerData(playerData);
            }

            String lastPlayed = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            saveFile.setSaveInfo("last_played", lastPlayed);

            stateController.setLoadInProgress(false);
            System.out.println("Save loaded successfully: " + saveId);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to load save: " + saveId);
            e.printStackTrace();
            stateController.setLoadInProgress(false);
            return false;
        }
    }

    private void copyDataController(DataController source, DataController target) {
        /* World */
        target.setWorldSeed(source.getWorldSeed());
        target.setWorldTime(source.getWorldTime());

        /* Player */
        target.setPlayerPos(source.getPlayerPos());
        target.setPlayerRotation(source.getPlayerRotation());
    }

    /**
     * List Available Saves
     */
    public List<SaveInfo> listAvailableSaves() {
        List<SaveInfo> saves = new ArrayList<>();
        for(String saveId : SaveFile.listAllSaves()) {
            SaveFile saveFile = new SaveFile(saveId);
            SaveInfo info = new SaveInfo();
            info.saveId = saveId;
            info.saveName = saveFile.getSaveInfo("save_name");
            info.creationDate = saveFile.getSaveInfo("creation_date");
            info.lastPlayed = saveFile.getSaveInfo("last_played");
            info.playTime = saveFile.getSaveInfo("play_time");
            info.version = saveFile.getSaveInfo("version");
            info.lastModified = saveFile.getFormattedLastModified();
            saves.add(info);
        }

        saves.sort((a, b) -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date dA = sdf.parse(a.lastPlayed);
                Date dB = sdf.parse(b.lastPlayed);
                return dB.compareTo(dA);
            } catch(Exception e) {
                return 0;
            }
        });
        return saves;
    }

    /**
     * Delete Save
     */
    public boolean deleteSave(String saveId) {
        try {
            SaveFile saveFile = new SaveFile(saveId);
            saveFile.deleteSave();
            System.out.println("Save deleted: " + saveId);
            return true;
        } catch(IOException e) {
            System.err.println("Failed to delete save: " + saveId);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Save Exists
     */
    public boolean saveExists(String saveId) {
        SaveFile saveFile = new SaveFile(saveId);
        return saveFile.exists();
    }

    /**
     * Get Save Info
     */
    public SaveInfo getSaveInfo(String saveId) {
        SaveFile saveFile = new SaveFile(saveId);
        if(!saveFile.exists()) return null;

        SaveInfo info = new SaveInfo();
        info.saveId = saveId;
        info.saveName = saveFile.getSaveInfo("save_name");
        info.creationDate = saveFile.getSaveInfo("creation_date");
        info.lastPlayed = saveFile.getSaveInfo("last_played");
        info.playTime = saveFile.getSaveInfo("play_time");
        info.version = saveFile.getSaveInfo("version");
        info.lastModified = saveFile.getFormattedLastModified();
        return info;
    }
}
