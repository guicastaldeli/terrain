package main.com.app.root.env.world;
import java.util.HashMap;
import java.util.*;

public class Chunk {
    public static int CHUNK_SIZE = 64;
    
    public Map<String, ChunkData> loadedChunks = new HashMap<>();
    public Map<String, ChunkData> cachedChunks = new HashMap<>();

    /**
     * Get Coords
     */
    public static int[] getCoords(float worldX, float worldZ) {
        int x = (int)Math.floor((worldX + MAX_WORLD_SIZE / 2) / CHUNK_SIZE);
        int z = (int)Math.floor((worldZ + MAX_WORLD_SIZE / 2) / CHUNK_SIZE);
        return new int[]{ x, z };
    }

    /**
     * Get Id
     */
    public static String getId(int x, int z) {
        return "chunk_" + x + "_" + z;
    }

    
}
