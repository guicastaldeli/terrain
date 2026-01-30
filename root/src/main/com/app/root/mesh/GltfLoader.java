package main.com.app.root.mesh;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class GltfLoader {
    private static final int COMPONENT_TYPE_FLOAT = 5126;
    private static final int COMPONENT_TYPE_UNSIGNED_INT = 5125;
    private static final int COMPONENT_TYPE_UNSIGNED_SHORT = 5123;
    
    /**
     * 
     * Load
     * 
     */
    public static MeshData load(String filePath, String meshId) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            JSONObject gltf = new JSONObject(content);
            
            return loadMesh(gltf, 0, meshId, filePath);
        } catch(Exception e) {
            throw new RuntimeException("Failed to load glTF file: " + filePath, e);
        }
    }
    
    public static MeshData loadMesh(String filePath, int meshIndex, String meshId) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            JSONObject gltf = new JSONObject(content);
            
            return loadMesh(gltf, meshIndex, meshId, filePath);
        } catch(Exception e) {
            throw new RuntimeException("Failed to load glTF file: " + filePath, e);
        }
    }
    
    private static MeshData loadMesh(JSONObject gltf, int meshIndex, String meshId, String filePath) {
        try {
            List<ByteBuffer> buffers = parseBuffers(gltf, filePath);
            JSONArray bufferViews = gltf.getJSONArray("bufferViews");
            JSONArray accessors = gltf.getJSONArray("accessors");
            JSONArray meshes = gltf.getJSONArray("meshes");
            if(meshIndex >= meshes.length()) {
                throw new RuntimeException("Mesh index " + meshIndex + " out of bounds");
            }
            
            JSONObject mesh = meshes.getJSONObject(meshIndex);
            JSONArray primitives = mesh.getJSONArray("primitives");
            JSONObject primitive = primitives.getJSONObject(0);
            JSONObject attributes = primitive.getJSONObject("attributes");
            
            float[] vertices = null;
            float[] normals = null;
            float[] texCoords = null;
            int[] indices = null;
            
            if(attributes.has("POSITION")) {
                int posAccessor = attributes.getInt("POSITION");
                vertices = readFloatAccessor(gltf, posAccessor, buffers, bufferViews, accessors);
            }
            if(attributes.has("NORMAL")) {
                int normalAccessor = attributes.getInt("NORMAL");
                normals = readFloatAccessor(gltf, normalAccessor, buffers, bufferViews, accessors);
            }
            if(attributes.has("TEXCOORD_0")) {
                int texCoordAccessor = attributes.getInt("TEXCOORD_0");
                texCoords = readFloatAccessor(gltf, texCoordAccessor, buffers, bufferViews, accessors);
            }
            if(primitive.has("indices")) {
                int indicesAccessor = primitive.getInt("indices");
                indices = readIntAccessor(gltf, indicesAccessor, buffers, bufferViews, accessors);
            }
            
            MeshData meshData = new MeshData(meshId, MeshData.MeshType.GLTF);
            if(vertices != null) meshData.setVertices(vertices);
            if(normals != null) meshData.setNormals(normals);
            if(texCoords != null) meshData.setTexCoords(texCoords);
            if(indices != null) meshData.setIndices(indices);
            
            return meshData;
        } catch(Exception e) {
            throw new RuntimeException("Failed to parse glTF mesh", e);
        }
    }
    
    /**
     * Parse Buffers
     */
    private static List<ByteBuffer> parseBuffers(JSONObject gltf, String filePath) throws IOException {
        List<ByteBuffer> buffers = new ArrayList<>();
        if(!gltf.has("buffers")) return buffers;
        
        JSONArray bufferArray = gltf.getJSONArray("buffers");
        String baseDir = Paths.get(filePath).getParent().toString();
        
        for(int i = 0; i < bufferArray.length(); i++) {
            JSONObject buffer = bufferArray.getJSONObject(i);
            int byteLength = buffer.getInt("byteLength");
            
            if(buffer.has("uri")) {
                String uri = buffer.getString("uri");
                
                if(uri.startsWith("data:")) {
                    String base64Data = uri.substring(uri.indexOf(",") + 1);
                    byte[] data = Base64.getDecoder().decode(base64Data);
                    ByteBuffer byteBuffer = ByteBuffer.wrap(data);
                    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                    buffers.add(byteBuffer);
                } else {
                    String bufferPath = Paths.get(baseDir, uri).toString();
                    byte[] data = Files.readAllBytes(Paths.get(bufferPath));
                    ByteBuffer byteBuffer = ByteBuffer.wrap(data);
                    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                    buffers.add(byteBuffer);
                }
            }
        }
        
        return buffers;
    }
    
    private static float[] readFloatAccessor(
        JSONObject gltf,
        int accessorIndex,
        List<ByteBuffer> buffers,
        JSONArray bufferViews,
        JSONArray accessors
    ) {
        JSONObject accessor = accessors.getJSONObject(accessorIndex);
        int bufferViewIndex = accessor.getInt("bufferView");
        int count = accessor.getInt("count");
        String type = accessor.getString("type");
        int componentType = accessor.getInt("componentType");
        
        JSONObject bufferView = bufferViews.getJSONObject(bufferViewIndex);
        int bufferIndex = bufferView.getInt("buffer");
        int byteOffset = bufferView.optInt("byteOffset", 0);
        int byteLength = bufferView.getInt("byteLength");
        
        int accessorByteOffset = accessor.optInt("byteOffset", 0);
        int totalOffset = byteOffset + accessorByteOffset;
        
        ByteBuffer buffer = buffers.get(bufferIndex);
        buffer.position(totalOffset);
        
        int componentsPerElement = getComponentCount(type);
        float[] result = new float[count * componentsPerElement];
        
        if(componentType == COMPONENT_TYPE_FLOAT) {
            for(int i = 0; i < result.length; i++) {
                result[i] = buffer.getFloat();
            }
        } else {
            throw new RuntimeException("Unsupported component type: " + componentType);
        }
        
        return result;
    }
    
    private static int[] readIntAccessor(
        JSONObject gltf,
        int accessorIndex,
        List<ByteBuffer> buffers,
        JSONArray bufferViews,
        JSONArray accessors
    ) {
        JSONObject accessor = accessors.getJSONObject(accessorIndex);
        int bufferViewIndex = accessor.getInt("bufferView");
        int count = accessor.getInt("count");
        int componentType = accessor.getInt("componentType");
        
        JSONObject bufferView = bufferViews.getJSONObject(bufferViewIndex);
        int bufferIndex = bufferView.getInt("buffer");
        int byteOffset = bufferView.optInt("byteOffset", 0);
        
        int accessorByteOffset = accessor.optInt("byteOffset", 0);
        int totalOffset = byteOffset + accessorByteOffset;
        
        ByteBuffer buffer = buffers.get(bufferIndex);
        buffer.position(totalOffset);
        
        int[] result = new int[count];
        
        if(componentType == COMPONENT_TYPE_UNSIGNED_INT) {
            for(int i = 0; i < count; i++) {
                result[i] = buffer.getInt();
            }
        } else if(componentType == COMPONENT_TYPE_UNSIGNED_SHORT) {
            for(int i = 0; i < count; i++) {
                result[i] = buffer.getShort() & 0xFFFF;
            }
        } else {
            throw new RuntimeException("Unsupported component type for indices: " + componentType);
        }
        
        return result;
    }
    
    private static int getComponentCount(String type) {
        switch(type) {
            case "SCALAR": return 1;
            case "VEC2": return 2;
            case "VEC3": return 3;
            case "VEC4": return 4;
            case "MAT2": return 4;
            case "MAT3": return 9;
            case "MAT4": return 16;
            default: throw new RuntimeException("Unknown type: " + type);
        }
    }
    
    /**
     * Get Mesh Count
     */
    public static int getMeshCount(String filePath) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            JSONObject gltf = new JSONObject(content);
            if(gltf.has("meshes")) return gltf.getJSONArray("meshes").length();

            return 0;
        } catch(Exception e) {
            throw new RuntimeException("Failed to read glTF file: " + filePath, e);
        }
    }
}