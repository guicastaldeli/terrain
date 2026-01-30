package main.com.app.root.mesh.particle;
import main.com.app.root.Tick;
import main.com.app.root.mesh.Mesh;
import org.joml.Vector3f;
import java.util.*;

public class ParticleManager {
    private final Tick tick;
    private final Mesh mesh;
    private final List<ParticleSystem> particleSystems;
    private ParticleSystem particleSystem;
    
    public ParticleManager(Tick tick, Mesh mesh) {
        this.tick = tick;
        this.mesh = mesh;
        this.particleSystems = new ArrayList<>();
    }

    public void create(
        Vector3f position, 
        Vector3f color, 
        int amount, 
        float size, 
        float speed, 
        float lifetime
    ) {
        particleSystem = new ParticleSystem(tick, mesh);
        particleSystem.setColor(color);
        particleSystem.setAmount(amount);
        particleSystem.setSize(size);
        particleSystem.setSpeed(speed);
        particleSystem.setLifetime(lifetime);
        
        particleSystems.add(particleSystem);
    }

    public ParticleSystem getParticleSystem() {
        return particleSystem;
    }

    public List<ParticleSystem> getParticleSystems() {
        return particleSystems;
    }

    /**
     * Update
     */
    public void update() {
        List<ParticleSystem> systemsToRemove = new ArrayList<>();
        for(ParticleSystem particleSystem : particleSystems) {
            particleSystem.update();
            if(!particleSystem.isActive()) {
                systemsToRemove.add(particleSystem);
            }
        }
        particleSystems.removeAll(systemsToRemove);
    }

    /**
     * Render
     */
    public void render() {
        for(ParticleSystem particleSystem : particleSystems) {
            particleSystem.render();
        }
    }

    /**
     * Cleanup
     */
    public void cleanup() {
        for(ParticleSystem particleSystem : particleSystems) {
            particleSystem.cleanup();
        }
        particleSystems.clear();
    }
}
