package main.com.app.root._save;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.text.SimpleDateFormat;
import main.com.app.root.DataController;
import main.com.app.root.StateController;
import main.com.app.root.env.map.MapGeneratorWrapper;

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

        generateWorldMap(saveFile);
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
     * Generate World Map
     */
    private void generateWorldMap(SaveFile saveFile) throws IOException {
        String noiseDir = "C:/Users/casta/OneDrive/Desktop/vscode/terrain/root/src/main/com/app/root/env/map/noise/data";
        File dir = new File(noiseDir);
        if(!dir.exists()) dir.mkdirs();

        Random r = new Random();
        int r1 = r.nextInt(9);
        int r2 = r.nextInt(9);
        String fileName = String.format(
            "m.%03d.%03d.%d.dat",
            r1,
            r2,
            System.currentTimeMillis()
        );
        String path = Paths.get(noiseDir, fileName).toString();

        long seed = dataController.getWorldSeed();
        MapGeneratorWrapper mapGeneratorWrapper = new MapGeneratorWrapper();

        boolean success = mapGeneratorWrapper.generateMap(path, seed);
        if(success) {
            Path source = Paths.get(path);
            Path target = saveFile.getSavePath().resolve("world").resolve("d.m.0.dat");
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("World map generated and saved to: " + target);
        } else {
            throw new IOException("Failed to generate world map");
        }
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
