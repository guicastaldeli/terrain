package main.com.app.root.mesh;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

public class ModelMap {
    private static final String PATH = "root/src/main/com/app/root/mesh/types/";
    
    private final Map<String, ModelInfo> dataMap;
    private final Map<String, List<String>> categories;

    public ModelMap() {
        this.dataMap = new HashMap<>();
        this.categories = new HashMap<>();
        load();
    }

    private void load() {
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
    
                                dataMap.put(
                                    name.toLowerCase(), 
                                    new ModelInfo(
                                        name, 
                                        path, 
                                        texture, 
                                        size
                                    )
                                );
                                
                                System.out.println("Loaded object: " + name + " from " + path);
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

    public ModelInfo getObjInfo(String name) {
        return dataMap.get(name.toLowerCase());
    }
    
    public String getObjPath(String name) {
        ModelInfo info = getObjInfo(name);
        return info != null ? info.getPath() : null;
    }
    
    public float[] getObjSize(String name) {
        ModelInfo info = getObjInfo(name);
        return info != null ? info.getSize() : new float[]{1.0f, 1.0f, 1.0f};
    }
    
    public boolean hasObj(String name) {
        return dataMap.containsKey(name.toLowerCase());
    }
    
    public List<String> getObjectsInCategory(String category) {
        return categories.getOrDefault(category, new ArrayList<>());
    }
    
    public Set<String> getAllCategories() {
        return categories.keySet();
    }
    
    public Map<String, ModelInfo> getAllObjects() {
        return new HashMap<>(dataMap);
    }
}
