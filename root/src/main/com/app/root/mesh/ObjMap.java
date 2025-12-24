package main.com.app.root.mesh;
import java.util.*;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

public class ObjMap {
    private static final String OBJ_MAP_PATH =  "root/src/main/com/app/root/_resources/";
    private final Map<String, ObjInfo> objMap;
    private final Map<String, List<String>> categories;

    public ObjMap() {
        this.objMap = new HashMap<>();
        this.categories = new HashMap<>();
        load();
    }

    private void load() {
        try {
            Globals globals = JsePlatform.standardGlobals();
            LuaValue chunk = globals.loadfile(OBJ_MAP_PATH);
            LuaValue result = chunk.call();
            if(result.istable()) {
                LuaValue objTable = result.get("objects");
                if(objTable.istable()) {
                    String name = objTable.get("name").checkjstring();
                    String path = objTable.get("path").checkjstring();
                    String texture = objTable.get("texture").checkjstring();
                    LuaValue sizeTable = objTable.get("size");
                    float[] size = new float[]{ 1.0f, 1.0f, 1.0f };
                    if(sizeTable.istable()) {
                        for(int j = 1; j <= 3 && j <= sizeTable.length(); j++) {
                            size[j-1] = (float) sizeTable.get(j).checkdouble();
                        }
                    }

                    objMap.put(
                        name.toLowerCase(), 
                        new ObjInfo(name, path, texture, size)
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load object map!: " + e.getMessage());
        }
    }

    public ObjInfo getObjInfo(String name) {
        return objMap.get(name.toLowerCase());
    }
    
    public String getObjPath(String name) {
        ObjInfo info = getObjInfo(name);
        return info != null ? info.getPath() : null;
    }
    
    public float[] getObjSize(String name) {
        ObjInfo info = getObjInfo(name);
        return info != null ? info.getSize() : new float[]{1.0f, 1.0f, 1.0f};
    }
    
    public boolean hasObj(String name) {
        return objMap.containsKey(name.toLowerCase());
    }
    
    public List<String> getObjectsInCategory(String category) {
        return categories.getOrDefault(category, new ArrayList<>());
    }
    
    public Set<String> getAllCategories() {
        return categories.keySet();
    }
    
    public Map<String, ObjInfo> getAllObjects() {
        return new HashMap<>(objMap);
    }
}
