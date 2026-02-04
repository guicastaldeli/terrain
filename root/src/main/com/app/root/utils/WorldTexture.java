package main.com.app.root.utils;
import main.com.app.root.mesh.MeshData;
import main.com.app.root._resources.TextureLoader;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.env.world.Chunk;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.system.MemoryUtil.memAllocFloat;
import static org.lwjgl.system.MemoryUtil.memFree;

public class WorldTexture {
    public static final Map<Integer, String> IndexMap = Map.of(
        0, "water",
        1, "sand",
        2, "grass",
        3, "rock",
        4, "snow"
    );
    private static final Map<String, String> TEX_MAP = Map.of(
        "water", "water.png",
        "sand", "sand.png",
        "grass", "grass.png",
        "rock", "rock1.png",
        "snow", "snow.png"
    );

    private final static String TEX_PATH = "root/src/main/com/app/root/_resources/texture/env/";

    private static int texBlendVbo;
    public static int[] worldTextures = new int[5];
    public static boolean hasWorldTextures = false;

    private static void checkGLError(String location) {
        int error = glGetError();
        if(error != GL_NO_ERROR) {
            System.err.println("OpenGL error at " + location + ": " + error);
        }
    }

    public static void setWorldTextures(Map<String, Integer> texMap) {
        for(Map.Entry<Integer, String> val : IndexMap.entrySet()) {
            int i = val.getKey();
            String texName = val.getValue();
            if(texMap.containsKey(texName)) {
                worldTextures[i] = texMap.get(texName);
                hasWorldTextures = true;
            }
        }
    }

    public boolean hasWorldTextures() {
        return hasWorldTextures;
    }

    /**
     * 
     * Generate UVs
     * 
     */
    public static float[] generateUVs(int chunkX, int chunkZ) {
        int heightDataSize = Chunk.CHUNK_SIZE + 1;
        float[] uvs = new float[heightDataSize * heightDataSize * 2];
        float uvScale = 4.0f;

        for(int x = 0; x < heightDataSize; x++) {
            for(int z = 0; z < heightDataSize; z++) {
                int i = (x * heightDataSize + z) * 2;
                uvs[i] = (x / (float)Chunk.CHUNK_SIZE) * uvScale;
                uvs[i+1] = (z / (float)Chunk.CHUNK_SIZE) * uvScale;
            }
        }

        return uvs;
    }

    /**
     * 
     * Buffers
     * 
     */
    public static void buffers(MeshData meshData) {
        float[] texBlends = meshData.getTexBlends();
        if(texBlends != null && texBlends.length > 0) {
            texBlendVbo = glGenBuffers();
            checkGLError("after glGenBuffers (texBlends)");
            
            glBindBuffer(GL_ARRAY_BUFFER, texBlendVbo);
            checkGLError("after glBindBuffer (texBlends)");
            
            FloatBuffer blendBuffer = memAllocFloat(texBlends.length);
            blendBuffer.put(texBlends).flip();
            glBufferData(GL_ARRAY_BUFFER, blendBuffer, GL_STATIC_DRAW);
            checkGLError("after glBufferData (texBlends)");
            memFree(blendBuffer);
            
            glVertexAttribPointer(10, 4, GL_FLOAT, false, 5 * Float.BYTES, 0);
            checkGLError("after glVertexAttribPointer (texBlends 0-3)");
            glEnableVertexAttribArray(10);
            checkGLError("after glEnableVertexAttribArray (texBlends 0-3)");
            
            glVertexAttribPointer(11, 1, GL_FLOAT, false, 5 * Float.BYTES, 4 * Float.BYTES);
            checkGLError("after glVertexAttribPointer (texBlend 4)");
            glEnableVertexAttribArray(11);
            checkGLError("after glEnableVertexAttribArray (texBlend 4)");
        }
    }

    /**
     * 
     * Render
     * 
     */
    public static void render(
        MeshData meshData,
        ShaderProgram shaderProgram, 
        boolean hasTex,
        int texId
    ) {
        boolean isChunk = 
            meshData != null && 
            meshData.getId() != null && 
            meshData.getId().startsWith("chunk_");
        
        boolean isWater = 
            meshData != null && 
            meshData.getId() != null && 
            meshData.getId().startsWith("water_");
            
        if(isChunk && hasWorldTextures) {
            shaderProgram.setUniform("hasWorldTex", 1);
            
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, worldTextures[0]);
            glActiveTexture(GL_TEXTURE0 + 1);
            glBindTexture(GL_TEXTURE_2D, worldTextures[1]);
            glActiveTexture(GL_TEXTURE0 + 2);
            glBindTexture(GL_TEXTURE_2D, worldTextures[2]);
            glActiveTexture(GL_TEXTURE0 + 3);
            glBindTexture(GL_TEXTURE_2D, worldTextures[3]);
            glActiveTexture(GL_TEXTURE0 + 4);
            glBindTexture(GL_TEXTURE_2D, worldTextures[4]);
            
            shaderProgram.setUniform("uTexWater", 0);
            shaderProgram.setUniform("uTexSand", 1);
            shaderProgram.setUniform("uTexGrass", 2);
            shaderProgram.setUniform("uTexRock", 3);
            shaderProgram.setUniform("uTexSnow", 4);
        } else if(isWater && worldTextures[0] > 0) {
            shaderProgram.setUniform("hasWorldTex", 0);
            shaderProgram.setUniform("isWater", 1);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, worldTextures[0]);
            shaderProgram.setUniform("uWaterOpacity", 0.5f);
        } else {
            shaderProgram.setUniform("hasWorldTex", 0);
            shaderProgram.setUniform("isWater", 0);
            if(hasTex) {
                glActiveTexture(GL_TEXTURE0);
                glBindTexture(GL_TEXTURE_2D, texId);
            }
        }
    }
    
    /**
     * 
     * Load
     * 
     */
    public static Map<String, Integer> load() {
        Map<String, Integer> textures = new HashMap<>();

        for(Map.Entry<Integer, String> indexVal : IndexMap.entrySet()) {
            for(Map.Entry<String, String> texVal : TEX_MAP.entrySet()) {
                if(indexVal.getValue().equals(texVal.getKey())) {
                    String texPath = TEX_PATH + texVal.getValue();
                    int texId = TextureLoader.load(texPath);
                    textures.put(texVal.getKey(), texId);
                }
            }
        }

        return textures;
    }

    public static void cleanup() {
        if(texBlendVbo != 0) glDeleteBuffers(texBlendVbo);
    }
}
