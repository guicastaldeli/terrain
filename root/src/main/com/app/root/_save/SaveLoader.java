package main.com.app.root._save;
import main.com.app.root.DataController;
import main.com.app.root.StateController;
import java.io.IOException;
import java.text.DateFormat;
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

            stateController.setCurrentSaveId(saveId);
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

            Date currentDate = new Date();
            String lastPlayed = DateFormat.getDateTimeInstance(
                DateFormat.DEFAULT,
                DateFormat.DEFAULT,
                Locale.getDefault()
            ).format(currentDate);
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

        SimpleDateFormat[] formats = {
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
            new SimpleDateFormat("MM/dd/yyyy HH:mm:ss"),
            new SimpleDateFormat("yyyy-MM-dd"),
            new SimpleDateFormat("MM/dd/yyyy"),
            new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"),
            new SimpleDateFormat("dd/MM/yyyy")
        };
        saves.sort((a, b) -> {
            try {
                Date dA = parseDate(a.lastPlayed, formats);
                Date dB = parseDate(b.lastPlayed, formats);
            
                if(dA == null && dB == null) return 0;
                if(dA == null) return 1;
                if(dB == null) return -1;
                
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

    private Date parseDate(String dateString, SimpleDateFormat[] formats) {
        if(dateString == null || dateString.trim().isEmpty() || dateString.equals("Unknown")) {
            return null;
        }
        
        for(SimpleDateFormat format : formats) {
            try {
                format.setLenient(false);
                return format.parse(dateString);
            } catch (Exception e) {
                continue;
            }
        }
        
        try {
            DateFormat defaultFormatter = DateFormat.getDateTimeInstance(
                DateFormat.DEFAULT,
                DateFormat.DEFAULT,
                Locale.getDefault()
            );
            return defaultFormatter.parse(dateString);
        } catch (Exception e) {
            return null;
        }
    }
}
