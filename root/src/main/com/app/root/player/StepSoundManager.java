package main.com.app.root.player;
import java.util.Map;
import org.joml.Vector3f;
import main.com.app.root.Tick;
import main.com.app.root._resources.AudioLoader;
import main.com.app.root.collision.types.StaticObject;
import main.com.app.root.env.world.Water;
import main.com.app.root.env.world.WorldGenerator;

public class StepSoundManager {
    private final AudioLoader audioLoader;

    private final Map<WorldGenerator.TerrainType, String> SOUND_MAP = Map.of(
        WorldGenerator.TerrainType.WATER, "player/swim.wav",
        WorldGenerator.TerrainType.SAND, "player/sand_walk.wav",
        WorldGenerator.TerrainType.ROCK, "player/rock_walk.wav",
        WorldGenerator.TerrainType.GRASS, "player/grass_walk.wav",
        WorldGenerator.TerrainType.SNOW, "player/snow_walk.wav"
    );

    private float stepTimer = 0.0f;
    private float stepInterval = 0.5f;
    private boolean wasMoving = false;

    private float volume = 0.3f;

    public StepSoundManager() {
        this.audioLoader = AudioLoader.getInstance();
        load();
    }

    /**
     * 
     * Play
     * 
     */
    private void play(WorldGenerator.TerrainType terrainType) {
        String soundFile = getSoundForTerrain(terrainType);
        if(soundFile != null) {
            audioLoader.playOverlapping(soundFile, volume, 500);
        }
    }

    /**
     * Get Sound for Terrain
     */
    private String getSoundForTerrain(WorldGenerator.TerrainType type) {
        return SOUND_MAP.getOrDefault(type, "player/grass_walk.wav");
    }
    
    public void setStepInterval(float interval) {
        this.stepInterval = Math.max(0.1f, interval);
    }
    
    public void adjustIntervalForSpeed(float movementSpeed, float baseSpeed) {
        if(movementSpeed > 0 && baseSpeed > 0) {
            float speedRatio = baseSpeed / movementSpeed;
            this.stepInterval = 0.5f * speedRatio;
            this.stepInterval = Math.max(0.2f, Math.min(1.0f, stepInterval));
        }
    }

    /**
     * 
     * Load
     * 
     */
    private void load() {
        for(String val : SOUND_MAP.values()) {
            audioLoader.load(val);
        }
    }

    /**
     * 
     * Update
     * 
     */
    public void update(
        boolean isMoving,
        boolean isOnGround,
        Vector3f position,
        StaticObject collider
    ) {
        if(!isMoving || collider == null) {
            stepTimer = 0.0f;
            wasMoving = false;
            return;
        }

        boolean isInWater = position.y < Water.LEVEL;
        
        WorldGenerator.TerrainType terrainType;
        if(isInWater) {
            terrainType = WorldGenerator.TerrainType.WATER;
        } else {
            if(!isOnGround) {
                stepTimer = 0.0f;
                wasMoving = false;
                return;
            }
            terrainType = collider.getTerrainTypeAt(position.x, position.z);
        }

        stepTimer += Tick.getIDeltaTime();
        if(stepTimer >= stepInterval) {
            play(terrainType);
            stepTimer = 0.0f;
        }

        wasMoving = true;
    }

    /**
     * 
     * Reset
     * 
     */
    public void reset() {
        stepTimer = 0.0f;
        wasMoving = false;
    }
}
