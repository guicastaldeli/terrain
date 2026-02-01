package main.com.app.root.env.world;
import main.com.app.root.Tick;
import main.com.app.root.TimeCycle;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.mesh.MeshData;
import main.com.app.root.mesh.particle.ParticleManager;
import main.com.app.root.mesh.particle.ParticleSystem;
import main.com.app.root.mesh.types.Particle;
import main.com.app.root.utils.ColorConverter;

import java.util.Random;
import org.joml.Vector3f;

public class WorldParticle {
    private final Tick tick;
    private final Mesh mesh;
    private final Random random;
    private ParticleManager particleManager;

    private Vector3f windDirection;
    private float windStrength;

    private float spawnRadius;
    private float spawnHeight;
    private float spawnTimer;
    private int particlesPerSec;

    private boolean isActive;
    private Vector3f playerPos;

    public WorldParticle(
        Tick tick, 
        Mesh mesh, 
        ParticleManager particleManager
    ) {
        this.tick = tick;
        this.mesh = mesh;
        this.particleManager = particleManager;
        this.random = new Random();

        this.windDirection = new Vector3f(1.0f, 0.0f, 0.0f).normalize();
        this.windStrength = 2.0f;

        this.spawnRadius = 120.0f;
        this.spawnHeight = 80.0f;
        this.spawnTimer = 0.0f;
        this.particlesPerSec = 15;

        this.isActive = false;
        this.playerPos = new Vector3f();
    }

    /**
     * Set wind parameters
     */
    public void setWind(Vector3f direction, float strength) {
        this.windDirection.set(direction).normalize();
        this.windStrength = strength;
    }

    /**
     * Is Active
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Get Particle Manager
     */
    public ParticleManager getParticleManager() {
        return particleManager;
    }
    
    /**
     * 
     * Start
     * 
     */
    public void start() {
        this.isActive = true;

        setSpawnRadius(100.0f);
        setSpawnHeight(1000.0f);
        setParticlesPerSec(55);
        setWind(new Vector3f(150.0f, 0.0f, 1.5f), 20.5f);
    }
    
    /**
     * 
     * Stop
     * 
     */
    public void stop() {
        this.isActive = false;
    }
    
    /**
     * 
     * Update
     * 
     */
    public void updatePlayerPosition(Vector3f position) {
        this.playerPos.set(position);
    }

    public void update() {
        if(!isActive) return;
        spawn();
        for(ParticleSystem particleSystem : particleManager.getParticleSystems()) {
            for(Particle particle : particleSystem.particles) {
                if(particle.swayAmplitude > 0) {
                    updateMotion(particle);
                }
            }
        }
    }

    private void updateMotion(Particle particle) {
        particle.swayPhase += particle.swayFrequency * tick.getDeltaTime();
        
        float swayX = (float)Math.sin(particle.swayPhase) * particle.swayAmplitude;
        float swayZ = (float)Math.cos(particle.swayPhase * 0.7f) * particle.swayAmplitude * 0.5f;
        
        particle.velocity.y += 2.0f * tick.getDeltaTime();
        //if(particle.velocity.y < -3.0f) particle.velocity.y = -3.0f;
        
        particle.velocity.x = swayX * 0.5f;
        particle.velocity.z = swayZ * 0.5f;
        
        Vector3f worldPos = new Vector3f(particle.basePosition);
        worldPos.add(particle.velocity);
        
        particle.position.set(worldPos);
        mesh.setPosition(particle.id, worldPos);
        
        particle.rotation += particle.rotationSpeed * tick.getDeltaTime();
        
        MeshData data = mesh.getData(particle.id);
        if(data != null) {
            data.setRotation(new Vector3f(0, 0, particle.rotation));
        }
    }


    /**
     * 
     * Spawn
     * 
     */
    public void setSpawnRadius(float radius) {
        this.spawnRadius = radius;
    }
    
    public void setSpawnHeight(float height) {
        this.spawnHeight = height;
    }
    
    public void setParticlesPerSec(int count) {
        this.particlesPerSec = count;
    }

    private void spawn() {
        spawnTimer += tick.getDeltaTime();
        float spawnInterval = 1.0f / particlesPerSec;

        while(spawnTimer >= spawnInterval) {
            spawnTimer -= spawnInterval;
            
            float angle = random.nextFloat() * (float)Math.PI * 2.0f;
            float distance = random.nextFloat() * spawnRadius;

            float spawnX = playerPos.x + (float)Math.cos(angle) * distance;
            float spawnY = playerPos.y + random.nextFloat() * spawnHeight;
            float spawnZ = playerPos.z + (float)Math.sin(angle) * distance;
            Vector3f spawnPos = new Vector3f(spawnX, spawnY, spawnZ);

            Vector3f color = getParticleColor();

            ParticleSystem particleSystem = new ParticleSystem(tick, mesh);
            particleSystem.setColor(color);
            particleSystem.setAmount(1);
            particleSystem.setSize(0.4f + random.nextFloat() * 0.8f);
            particleSystem.setSpeed(1.0f);
            particleSystem.setLifetime(15.0f);
            particleSystem.setMotion(true, 1.5f, 1.0f);

            particleSystem.emit(spawnPos, true);
            particleManager.getParticleSystems().add(particleSystem);

            if(!particleSystem.particles.isEmpty()) {
                Particle particle = particleSystem.particles.get(0);
                particle.position.set(spawnPos);
                particle.basePosition.set(spawnPos); 
                particle.velocity.set(
                    (random.nextFloat() - 0.5f) * 0.5f,
                    -random.nextFloat() * 0.5f,
                    (random.nextFloat() - 0.5f) * 0.5f
                );
                
                particle.swayPhase = random.nextFloat() * (float)Math.PI * 2.0f;
                particle.swayAmplitude = 1.5f * (0.8f + random.nextFloat() * 0.4f);
                particle.swayFrequency = 1.0f * (0.8f + random.nextFloat() * 0.4f);
                particle.rotationSpeed = (random.nextFloat() - 0.5f) * 4.0f;
            }
        }
    }
    
    /**
     * 
     * Colors
     * 
     */
    private Vector3f getParticleColor() {
        TimeCycle.TimePeriod period = tick.getTimeCycle().getCurrentTimePeriod();
        
        switch(period) {
            case MORNING:
            case AFTERNOON:
                return getRandomColorFromHex(
                    "#ADD8E6",
                    "#D3D3D3",
                    "#FFFFFF"
                );
                
            case DUSK:
                return getRandomColorFromHex(
                    "#D2B48C",
                    "#F5F5DC"
                );
                
            case NIGHT:
            case MIDNIGHT:
            case DAWN:
            default:
                return getRandomColorFromHex(
                    "#D3D3D3",
                    "#FFFFFF",
                    "#505050"
                );
        }
    }

    private Vector3f getRandomColorFromHex(String... hexColors) {
        String hex = hexColors[random.nextInt(hexColors.length)];
        float[] color = ColorConverter.hexToFloat(hex);
        return new Vector3f(color[0], color[1], color[2]);
    }

    /**
     * Cleanup
     */
    public void cleanup() {
        isActive = false;
    }
}
