package main.com.app.root.mesh;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

public class ModelMap {
    public enum ModelFormat {
        OBJ,
        GLTF,
        GLB,
        UNKNOWN
    }

    private static final String PATH = "root/src/main/com/app/root/mesh/types/";
    
    private final Map<String, ModelInfo> dataMap;
    private final Map<String, List<String>> categories;

    public ModelMap() {
        this.dataMap = new HashMap<>();
        this.categories = new HashMap<>();
        loadData();
    }

    /**
     * 
     * Load
     * 
     */
    public MeshData load(String name, String meshId) {
        ModelInfo info = getModelInfo(name);
        if(info == null) throw new RuntimeException("Model not found: " + name);

        String path = info.getPath();
        ModelFormat format = info.getFormat();
        switch(format) {
            case OBJ:
                return ObjLoader.load(path, meshId);
            case GLTF:
            case GLB:
                return GltfLoader.load(path, meshId);
            default:
                throw new RuntimeException("Unsupported model format: " + format + " for " + name);
        }
    }

    private void loadData() {
        try {
            File typesDir = new File(PATH);
            if(!typesDir.exists() || !typesDir.isDirectory()) {
                System.err.println("Types dir not found: " + PATH);
            }

            File[] dataFiles = typesDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".lua"));
            if(dataFiles == null) {
                System.err.println("No data files found in dir: " + PATH);
                return;
            }

            Globals globals = JsePlatform.standardGlobals();
            for(File dataFile : dataFiles) {
                String fLine = Files.readAllLines(Paths.get(dataFile.getAbsolutePath())).get(0).trim();
                if(!fLine.startsWith("return")) {
                    System.out.println("Skipping " + dataFile.getName() + " - does not start with 'return'");
                    continue;
                }

                LuaValue chunk = globals.loadfile(dataFile.getAbsolutePath());
                LuaValue result = chunk.call();
                
                if(result.istable()) {
                    LuaValue dataTable = result.get("data");
                    if(dataTable.istable()) {
                        for(int i = 1; i <= dataTable.length(); i++) {
                            LuaValue val = dataTable.get(i);
                            if(val.istable()) {
                                String name = val.get("name").checkjstring();
                                String path = val.get("path").checkjstring();
                                
                                String texture = "";
                                LuaValue textureVal = val.get("texture");
                                if(!textureVal.isnil() && textureVal.isstring()) {
                                    texture = textureVal.checkjstring();
                                }
                                
                                LuaValue sizeTable = val.get("scale");
                                float[] size = new float[]{ 1.0f, 1.0f, 1.0f };
                                if(sizeTable.istable()) {
                                    for(int j = 1; j <= 3 && j <= sizeTable.length(); j++) {
                                        size[j-1] = (float) sizeTable.get(j).checkdouble();
                                    }
                                }

                                ModelFormat format = detectFormat(path);
    
                                dataMap.put(
                                    name.toLowerCase(), 
                                    new ModelInfo(
                                        name, 
                                        path, 
                                        texture, 
                                        size,
                                        format
                                    )
                                );
                                
                                System.out.println("Loaded object: " + name + " (" + format + ") from " + path);
                            }
                        }
                    }
                }
            } 
        } catch(Exception e) {
            System.err.println("Failed to load object map!: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public ModelInfo getModelInfo(String name) {
        return dataMap.get(name.toLowerCase());
    }
    
    public String getModelPath(String name) {
        ModelInfo info = getModelInfo(name);
        return info != null ? info.getPath() : null;
    }
    
    public float[] getModelSize(String name) {
        ModelInfo info = getModelInfo(name);
        return info != null ? info.getSize() : new float[]{1.0f, 1.0f, 1.0f};
    }
    
    public boolean hasModel(String name) {
        return dataMap.containsKey(name.toLowerCase());
    }
    
    public List<String> getDataInCategory(String category) {
        return categories.getOrDefault(category, new ArrayList<>());
    }
    
    public Set<String> getAllCategories() {
        return categories.keySet();
    }
    
    public Map<String, ModelInfo> getAllData() {
        return new HashMap<>(dataMap);
    }

    public ModelFormat getModelFormat(String name) {
        ModelInfo info = getModelInfo(name);
        return info != null ? info.getFormat() : ModelFormat.UNKNOWN;
    }

    /**
     * 
     * Detect Format
     * 
     */
    public ModelFormat detectFormat(String path) {
        String lowerPath = path.toLowerCase();
        if(lowerPath.endsWith(".obj")) {
            return ModelFormat.OBJ;
        } else if(lowerPath.endsWith(".gltf")) {
            return ModelFormat.GLTF;
        } else if(lowerPath.endsWith(".glb")) {
            return ModelFormat.GLB;
        }
        return ModelFormat.UNKNOWN;
    }
}