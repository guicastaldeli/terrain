package main.com.app.root._font;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.util.*;

public class FontMap { 
    private static final String LIST_PATH = "root/src/main/com/app/root/_font/list.lua";
    private static Map<String, FontConfig> fontRegistry = new HashMap<>();
    private static boolean init = false;

    public static void init(String path) {
        if(init) return;

        try {
            Globals globals = JsePlatform.standardGlobals();
            LuaValue chunk = globals.loadfile(path);
            if(chunk.isnil()) {
                System.err.println("Failed to load Lua font config from: " + path);
                return;
            }

            LuaValue result = chunk.call();
            if(result.istable()) {
                LuaValue[] keys = result.checktable().keys();
                for(LuaValue key : keys) {
                    LuaValue fontTable = result.get(key);
                    if(fontTable.istable()) {
                        String name = fontTable.get("name").tojstring();
                        int size = fontTable.get("size").toint();
                        String p = fontTable.get("path").tojstring();

                        String fontKey = key.tojstring();
                        fontRegistry.put(
                            fontKey, 
                            new FontConfig(name, size, p)
                        );
                    }
                }

                init = true;
            }
        } catch(Exception err) {
            System.err.println("Font error!" + err.getMessage());
            err.printStackTrace();
        }
    }

    /**
     * Get Font
     */
    public static FontConfig getFont(String key) {
        if(!init) init(LIST_PATH);
        FontConfig font = fontRegistry.get(key.toLowerCase());
        return font;
    }

    /**
     * Has Font
     */
    public static boolean hasFont(String key) {
        if(!init) init(LIST_PATH);
        return fontRegistry.containsKey(key.toLowerCase());
    }

    /**
     * Get All Fonts
     */
    public static Map<String, FontConfig> getAllFonts() {
        if(!init) init(LIST_PATH);
        return new HashMap<>(fontRegistry);
    }
}
