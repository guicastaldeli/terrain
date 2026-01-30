package main.com.app.root.env.world;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.player.PlayerController;
import java.util.Random;
import org.joml.Vector3f;

public class Weather {
    private final WorldGenerator worldGenerator;
    private final Mesh mesh;
    private final PlayerController playerController;

    public static final float MOUNTAIN_LEVEL = 250.0f;

    private static final long SNOW_CHECK_INTERVAL = 500;
    private static final float SNOW_EFFECT_THRESHOLD = MOUNTAIN_LEVEL + 10.0f;
    private long lastSnowCheck = 0;
    
    public Weather(
        WorldGenerator worldGenerator,
        Mesh mesh, 
        PlayerController playerController
    ) {
        this.worldGenerator = worldGenerator;
        this.mesh = mesh;
        this.playerController = playerController;
    }

    /**
     * 
     * Mountain
     * 
     */

    /**
     * 
     * Snow
     * 
     */
    public void checkSnowEffect(Vector3f playerPos) {
        float terrainHeight = worldGenerator.getHeightAt(playerPos.x, playerPos.z);
        
        if(playerPos.y >= SNOW_EFFECT_THRESHOLD && 
            terrainHeight >= MOUNTAIN_LEVEL
        ) {
            createSnowEffect(playerPos);
        }
    }

    private void createSnowEffect(Vector3f playerPos) {
        Random random = new Random();
        
        int amount = 30;
        float size = 0.15f;
        float speed = 0.5f;
        float lifetime = 3.0f;
        
        mesh.getParticleManager()
            .create(
                playerPos, 
                new Vector3f(1.0f, 1.0f, 1.0f),
                amount, 
                size, 
                speed, 
                lifetime
            );
        
        Vector3f velNum = new Vector3f(5.0f, 2.0f, 5.0f);
        
        mesh.getParticleManager().getParticleSystem().setVelNum(velNum);
        
        mesh.getParticleManager().getParticleSystem().emit(playerPos, true, () -> {
            float whiteIntensity = 0.8f + random.nextFloat() * 0.2f;
            return new Vector3f(whiteIntensity, whiteIntensity, whiteIntensity);
        });
    }
    
    /**
     * Update
     */
    public void update(long currentTime) {
        if(currentTime - lastSnowCheck > SNOW_CHECK_INTERVAL) {
            Vector3f playerPos = playerController.getPosition();
            checkSnowEffect(playerPos);
            lastSnowCheck = currentTime;
        }
    }
}