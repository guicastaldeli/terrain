package main.com.app.root._save;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class SaveFile {
    private static final String SAVE_BASE_PATH = "root/src/main/com/app/root/_resources/saves";
    private static final String FILE_EXTENSION = ".dat";
    private static final String SAVE_INFO = "save.info";
    private static final String INFO_COMMENT = "SAVE INFORMATION";

    private String saveId;
    private Path savePath;
    private Properties saveInfo;

    public SaveFile(String saveId) {
        this.saveId = saveId;
        this.savePath = Paths.get(SAVE_BASE_PATH, saveId);
        this.saveInfo = new Properties();
        loadSaveInfo();
    } 

    /**
     * List All Saves
     */
    public static List<String> listAllSaves() {
        List<String> saves = new ArrayList<>();
        File baseDir = new File(SAVE_BASE_PATH);
        if(baseDir.exists() || !baseDir.isDirectory()) {
            System.out.println("Save directory does not exist: " + SAVE_BASE_PATH);
            return saves;
        }

        File[] saveDirs = baseDir.listFiles(File::isDirectory);
        if(saveDirs != null) {
            for(File dir : saveDirs) {
                if(new File(dir, SAVE_INFO).exists()) {
                    saves.add(dir.getName());
                }
            }
        }

        System.out.println("Found " + saves.size() + " saves");
        return saves;
    }

    public static List<Path> getAllSaveInfoFiles() {
        try {
            return Files.walk(Paths.get(SAVE_BASE_PATH))
                .filter(path -> path.endsWith(SAVE_INFO))
                .collect(Collectors.toList());
        } catch(Exception err) {
            System.err.println("Error walking save directory: " + err.getMessage());
            return new ArrayList<>();
        }
    }

    public boolean exists() {
        return Files.exists(savePath);
    }

    /**
     * Create Save Directory
     */
    public void createSaveDir() throws IOException {
        if(!Files.exists(savePath)) {
            Files.createDirectories(savePath);
            Files.createDirectories(savePath.resolve("data"));
            Files.createDirectories(savePath.resolve("world"));
            Files.createDirectories(savePath.resolve("player"));
            Files.createDirectories(savePath.resolve("stats"));
        }
    }

    /**
     * 
     * Data
     * 
     */
    public void saveData(
        String category,
        String fileName,
        byte[] data
    ) throws IOException {
        Path filePath = savePath.resolve(category).resolve(fileName + FILE_EXTENSION);
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, data);
    }

    public byte[] loadData(String category, String fileName) throws IOException {
        Path filePath = savePath.resolve(category).resolve(fileName + FILE_EXTENSION);
        return Files.readAllBytes(filePath);
    }

    public boolean hasData(String category, String fileName) {
        Path filePath = savePath.resolve(category).resolve(fileName + FILE_EXTENSION);
        return Files.exists(filePath);
    }

    /**
     * Save Object
     */
    public void saveObject(
        String category,
        String fileName,
        Serializable object
    ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try(ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(object);
        }
        saveData(
            category, 
            fileName, 
            baos.toByteArray()
        );
    }

    /**
     * Load Object
     */
    public Object loadObject(String category, String fileName) throws IOException, ClassNotFoundException {
        byte[] data = loadData(category, fileName);
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        try(ObjectInputStream ois = new ObjectInputStream(bais)) {
            return ois.readObject();
        }
    }

    /**
     * 
     * Save Info
     * 
     */
    public void saveSaveInfo() throws IOException {
        Path infoPath = savePath.resolve(SAVE_INFO);
        try(FileWriter writer = new FileWriter(infoPath.toFile())) {
            saveInfo.store(writer, INFO_COMMENT);
        }
    }

    private void loadSaveInfo() {
        Path infoPath = savePath.resolve(SAVE_INFO);
        if(Files.exists(infoPath)) {
            try(FileReader reader = new FileReader(infoPath.toFile())) {
                saveInfo.load(reader);
            } catch(IOException e) {
                saveInfo = new Properties();
            }
        }
    }

    public void setSaveInfo(String key, String val) {
        saveInfo.setProperty(key, val);
    }

    public String getSaveInfo(String key) {
        return saveInfo.getProperty(key);
    }

    public void deleteSave() throws IOException {
        if(exists()) {
            Files.walk(savePath)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        }
    }

    public String getSaveId() {
        return saveId;
    }

    public Path getSavePath() {
        return savePath;
    }

    /**
     * Last Modified
     */
    public long getLastModified() {
        Path infoPath = savePath.resolve(SAVE_INFO);
        if(Files.exists(infoPath)) {
            try {
                return Files.getLastModifiedTime(infoPath).toMillis();
            } catch(IOException e) {
                return 0;
            }
        }
        return 0;
    }

    public String getFormattedLastModified() {
        long time = getLastModified();
        if(time > 0) {
            return new Date(time).toString();
        }
        return "Unknown";
    }
}
