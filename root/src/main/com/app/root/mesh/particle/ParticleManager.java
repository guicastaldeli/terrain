package main.com.app.root.mesh.particle;
import main.com.app.root.Tick;
import main.com.app.root.mesh.Mesh;
import java.util.*;

public class ParticleManager {
    private final Tick tick;
    private final Mesh mesh;
    private final List<ParticleSystem> particleSystems;
    
    public ParticleManager(Tick tick, Mesh mesh) {
        this.tick = tick;
        this.mesh = mesh;
        this.particleSystems = new ArrayList<>();
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
