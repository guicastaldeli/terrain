package main.com.app.root.mesh;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.*;

public class MeshLoader {
    private static final String DATA_TYPES_DIR = "root/src/main/com/app/root/mesh/types/";
    private static final ObjMap objMap = new ObjMap(); 

    public static MeshData load(MeshData.MeshType type, String id) {
        String fileName = type.name().toLowerCase() + ".lua";
        return loadFromFile(fileName, id);
    }

    public static MeshData loadFromFile(String file, String meshId) {
        try {
            Globals globals = JsePlatform.standardGlobals();
            LuaValue chunk = globals.loadfile(DATA_TYPES_DIR + file);
            chunk.call();
            return createMesh(globals, meshId);
        } catch(Exception err) {
            throw new RuntimeException("Failed to load mesh !!: " + file, err);
        }
    }

    /**
     * Create Mesh
     */
    private static MeshData createMesh(Globals globals, String meshId) {
        String meshTypeStr = globals.get("meshType").checkjstring();
        MeshData.MeshType meshType = MeshData.MeshType.valueOf(meshTypeStr.toUpperCase());
        MeshData meshData = new MeshData(meshId, meshType);
        overrideData(globals, meshData);
        return meshData;
    }

    /**
     * Override Data
     */
    private static void overrideData(Globals globals, MeshData meshData) {
        /* Vertices */
        if(globals.get("vertices").istable()) {
            LuaValue verticesTable = globals.get("vertices");
            float[] vertices = tableToFloatArray(verticesTable);
            meshData.addData(MeshData.DataType.VERTICES, vertices);
        }
        /* Indices */
        if(globals.get("indices").istable()) {
            LuaValue indicesTable = globals.get("indices");
            int[] indices = tableToIntArray(indicesTable);
            meshData.addData(MeshData.DataType.INDICES, indices);
        }
        /* Colors */
        if(globals.get("colors").istable()) {
            LuaValue colorsTable = globals.get("colors");
            float[] colors = tableToFloatArray(colorsTable);
            meshData.addData(MeshData.DataType.COLORS, colors);
        }
        /* Normals */
        if(globals.get("normals").istable()) {
            LuaValue normalsTable = globals.get("normals");
            float[] normals = tableToFloatArray(normalsTable);
            meshData.addData(MeshData.DataType.NORMALS, normals);
        }
        /* Tex Coords */
        if(globals.get("texCoords").istable()) {
            LuaValue texCoordsTable = globals.get("texCoords");
            float[] texCoords = tableToFloatArray(texCoordsTable);
            meshData.addData(MeshData.DataType.TEX_COORDS, texCoords);
        }
        /* Rotation */
        if(globals.get("rotation").istable()) {
            LuaValue rotationTable = globals.get("rotation");
            loadRotationData(rotationTable, meshData);
        }
        /* Scale */
        if(globals.get("scale").istable()) {
            LuaValue scaleTable = globals.get("scale");
            float[] scale = tableToFloatArray(scaleTable);
            meshData.addData(MeshData.DataType.SCALE, scale);
            System.out.println(scaleTable);
        }
    }

    /**
     * Load Rotation Data
     */
    private static void loadRotationData(LuaValue table, MeshData data) {
        LuaValue axisVal = table.get("axis");
        if(!axisVal.isnil() && axisVal.isstring()) {
            String axis = axisVal.checkjstring();
            data.addData(MeshData.DataType.ROTATION_AXIS, axis);
        }
        LuaValue speedVal = table.get("speed");
        if(!speedVal.isnil() && speedVal.isnumber()) {
            float speed = (float) speedVal.checkdouble();
            data.addData(MeshData.DataType.ROTATION_SPEED, speed);
        }
    }

    private static float[] tableToFloatArray(LuaValue table) {
        int len = table.length();
        float[] arr = new float[len];
        for(int i = 1; i <= len; i++) {
            arr[i-1] = (float) table.get(i).checkdouble();
        }
        return arr;
    }

    private static int[] tableToIntArray(LuaValue table) {
        int len = table.length();
        int[] arr = new int[len];
        for(int i = 1; i <= len; i++) {
            arr[i-1] = table.get(i).checkint();
        }
        return arr;
    }

    /**
     * Load Model
     */
    public static MeshData loadModel(String modelName, String meshId) {
        String filePath = objMap.getObjPath(modelName);
        if(filePath == null) throw new RuntimeException("Model not found in object map: " + modelName);
        
        MeshData meshData = ObjLoader.load(filePath, meshId);
        
        float[] size = objMap.getObjSize(modelName);
        if(size != null) {
            meshData.setScale(size);
        }
        
        return meshData;
    }
    public static MeshData loadModel(String filePath) {
        String meshId = extractMeshIdFromPath(filePath);
        return ObjLoader.load(filePath, meshId);
    }
    
    public static float[] getModelSize(String modelName) {
        return objMap.getObjSize(modelName);
    }
    
    private static String extractMeshIdFromPath(String filePath) {
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        return fileName.replace(".obj", "");
    }
}
