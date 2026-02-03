package main.com.app.root.env.world;
import main.com.app.root.Tick;
import main.com.app.root.TimeCycle;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.mesh.particle.ParticleManager;
import main.com.app.root.mesh.particle.ParticleSystem;
import main.com.app.root.mesh.types.Particle;
import main.com.app.root.utils.ColorConverter;

import java.util.ArrayDeque;
import java.util.List;
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

    private final Vector3f _scratch = new Vector3f();
    private final ArrayDeque<ParticleSystem> pool = new ArrayDeque<>();

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
        this.particlesPerSec = 10;

        this.isActive = false;
        this.playerPos = new Vector3f();
    }

    public void setWind(Vector3f direction, float strength) {
        this.windDirection.set(direction).normalize();
        this.windStrength = strength;
    }

    public boolean isActive() {
        return isActive;
    }

    public ParticleManager getParticleManager() {
        return particleManager;
    }
    
    public void start() {
        this.isActive = true;
        setWind(new Vector3f(150.0f, 0.0f, 1.5f), 20.5f);
    }
    
    public void stop() {
        this.isActive = false;
    }
    
    public void updatePlayerPosition(Vector3f position) {
        this.playerPos.set(position);
    }

    public void update() {
        if(!isActive) return;
        spawn();
        List<ParticleSystem> systems = particleManager.getParticleSystems();
        for(int i = 0; i < systems.size(); i++) {
            List<Particle> particles = systems.get(i).particles;
            for(int j = 0; j < particles.size(); j++) {
                Particle particle = particles.get(j);
                if(particle.swayAmplitude > 0) {
                    updateMotion(particle);
                }
            }
        }
        recycleSystems();
    }

    private void recycleSystems() {
        List<ParticleSystem> systems = particleManager.getParticleSystems();
        for(int i = systems.size() - 1; i >= 0; i--) {
            ParticleSystem particleSystem = systems.get(i);
            if(!particleSystem.isActive()) {
                systems.remove(i);
                particleSystem.particles.clear();
                pool.offer(particleSystem);
            }
        }
    }

    private void updateMotion(Particle particle) {
        float deltaTime = tick.getDeltaTime();
        particle.swayPhase += particle.swayFrequency * deltaTime;
        
        float swayX = (float)Math.sin(particle.swayPhase) * particle.swayAmplitude;
        float swayZ = (float)Math.cos(particle.swayPhase * 0.7f) * particle.swayAmplitude * 0.5f;
        
        particle.velocity.y += 2.0f * deltaTime;
        particle.velocity.x = swayX * 0.5f;
        particle.velocity.z = swayZ * 0.5f;

        particle.position.x = particle.basePosition.x + particle.velocity.x;
        particle.position.y += particle.velocity.y * deltaTime;
        particle.position.z = particle.basePosition.z + particle.velocity.z;

        mesh.setPosition(particle.id, particle.position);

        particle.rotation += particle.rotationSpeed * deltaTime;

        if(particle.cachedMeshData != null) {
            _scratch.set(0, 0, particle.rotation);
            particle.cachedMeshData.setRotation(_scratch);
        }
    }

    public void setSpawnRadius(float radius) {
        this.spawnRadius = radius;
    }
    
    public void setSpawnHeight(float height) {
        this.spawnHeight = height;
    }
    
    public void setParticlesPerSec(int count) {
        this.particlesPerSec = count;
    }

    private ParticleSystem getOrCreate() {
        ParticleSystem particleSystem = pool.poll();
        if(particleSystem != null) {
            particleSystem.particles.clear();
            return particleSystem;
        }
        return new ParticleSystem(tick, mesh);
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

            ParticleSystem particleSystem = getOrCreate();
            particleSystem.setColor(getParticleColor());
            particleSystem.setAmount(1);
            particleSystem.setSize(0.4f + random.nextFloat() * 0.8f);
            particleSystem.setSpeed(1.0f);
            particleSystem.setLifetime(15.0f);
            particleSystem.setMotion(true, 1.5f, 1.0f);

            Vector3f spawnPos = new Vector3f(spawnX, spawnY, spawnZ);
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

                particle.cachedMeshData = mesh.getData(particle.id);
            }
        }
    }
    
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

    public void cleanup() {
        isActive = false;
    }
}