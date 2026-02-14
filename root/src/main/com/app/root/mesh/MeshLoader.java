package main.com.app.root.mesh;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.*;

import main.com.app.root._resources.TextureLoader;

public class MeshLoader {
    private static final String DATA_TYPES_DIR = "root/src/main/com/app/root/mesh/types/";
    private static ModelMap modelMap = null;
    
    public static ModelMap getModelMap() {
        if(modelMap == null) modelMap = new ModelMap();
        return modelMap;
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
            getRotationData(rotationTable, meshData);
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
     * Get Rotation Data
     */
    private static void getRotationData(LuaValue table, MeshData data) {
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
     * 
     * Load
     * 
     */
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

    public static MeshData loadModel(String modelName, String meshId) {
        ModelMap map = getModelMap();
        if(!map.hasModel(modelName)) {
            throw new RuntimeException("Model not found in object map: " + modelName);
        }
        
        ModelInfo info = map.getModelInfo(modelName);
        String filePath = info.getPath();
        ModelMap.ModelFormat format = info.getFormat();
        
        MeshData meshData;
        switch(format) {
            case OBJ:
                meshData = ObjLoader.load(filePath, meshId);
                break;
            case GLTF:
            case GLB:
                meshData = GltfLoader.load(filePath, meshId);
                break;
            default:
                throw new RuntimeException("Unsupported model format: " + format + " for " + modelName);
        }
        
        float[] size = info.getSize();
        if(size != null) {
            meshData.setScale(size);
        }

        String texPath = info.getTexture();
        if(texPath != null && !texPath.isEmpty()) {
            TextureLoader.load(texPath);
        }
        
        return meshData;
    }

    public static MeshData loadModel(String filePath) {
        String meshId = extractMeshIdFromPath(filePath);
        ModelMap.ModelFormat format = getModelMap().detectFormat(filePath);
        
        switch(format) {
            case OBJ:
                return ObjLoader.load(filePath, meshId);
            case GLTF:
            case GLB:
                return GltfLoader.load(filePath, meshId);
            default:
                throw new RuntimeException("Unsupported file format: " + filePath);
        }
    }
    
    public static MeshData loadMesh(String modelName, int meshIndex, String meshId) {
        ModelMap map = getModelMap();
        ModelInfo info = map.getModelInfo(modelName);
        
        if(info == null) {
            throw new RuntimeException("Model not found: " + modelName);
        }
        if(!info.isGltf()) {
            throw new RuntimeException("Model is not a glTF file: " + modelName);
        }
        
        MeshData meshData = GltfLoader.loadMesh(info.getPath(), meshIndex, meshId);
        
        float[] size = info.getSize();
        if(size != null) {
            meshData.setScale(size);
        }
        
        return meshData;
    }

    public static MeshData[] loadAllMeshes(String modelName, String meshIdPrefix) {
        int meshCount = getMeshCount(modelName);
        MeshData[] meshes = new MeshData[meshCount];
        
        for(int i = 0; i < meshCount; i++) {
            meshes[i] = loadMesh(modelName, i, meshIdPrefix + "_" + i);
        }
        
        return meshes;
    }
    
    public static MeshData loadMesh(String filePath, int meshIndex) {
        String meshId = extractMeshIdFromPath(filePath) + "_" + meshIndex;
        return GltfLoader.loadMesh(filePath, meshIndex, meshId);
    }

    public static AnimatedModel loadAnimatedModel(String modelName, String meshId) {
        ModelMap modelMap = getModelMap();
        ModelInfo modelInfo = modelMap.getModelInfo(modelName);
        if(modelInfo == null) throw new RuntimeException("Model not found: " + modelName);
        if(!modelInfo.isGltf()) throw new RuntimeException("Only glTF/GLB models support animations: " + modelName);
        
        AnimatedModel animatedModel = AnimationLoader.loadAnimatedModel(modelInfo.getPath(), meshId);

        float[] size = modelInfo.getSize();
        if(size != null) animatedModel.getMeshData().setScale(size);

        return animatedModel;
    }
    
    /**
     * Get Mesh Count
     */
    public static int getMeshCount(String modelName) {
        ModelMap map = getModelMap();
        ModelInfo info = map.getModelInfo(modelName);
        
        if(info == null) {
            throw new RuntimeException("Model not found: " + modelName);
        }
        
        if(!info.isGltf()) {
            throw new RuntimeException("Model is not a glTF file: " + modelName);
        }
        
        return GltfLoader.getMeshCount(info.getPath());
    }
    
    public static float[] getModelSize(String modelName) {
        return getModelMap().getModelSize(modelName);
    }
    
    /**
     * Format
     */
    public static ModelMap.ModelFormat getModelFormat(String modelName) {
        return getModelMap().getModelFormat(modelName);
    }
    
    public static boolean isGltfModel(String modelName) {
        ModelInfo info = getModelMap().getModelInfo(modelName);
        return info != null && info.isGltf();
    }
    
    public static boolean isObjModel(String modelName) {
        ModelInfo info = getModelMap().getModelInfo(modelName);
        return info != null && info.isObj();
    }

    /**
     * Extract Mesh Id
     */
    private static String extractMeshIdFromPath(String filePath) {
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        fileName = fileName.replaceAll("\\.(obj|gltf|glb)$", "");
        return fileName;
    }
}