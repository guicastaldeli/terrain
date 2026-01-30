package main.com.app.root.mesh.particle;
import main.com.app.root.Tick;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.mesh.MeshData;
import main.com.app.root.mesh.types.Particle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.joml.Vector3f;

public class ParticleSystem {
    private final Tick tick;
    private final Mesh mesh;
    private final Random random;
    
    private List<Particle> particles;
    private boolean isActive;
    private Vector3f position;
    private Vector3f color;
    private float size;
    private float speed;
    private int amount;
    private float lifetime;
    private String particleId;

    public ParticleSystem(Tick tick, Mesh mesh) {
        this.tick = tick;
        this.mesh = mesh;
        this.random = new Random();
        this.particles = new ArrayList<>();
        this.isActive = false;
        this.position = new Vector3f();
        this.color = new Vector3f(1.0f, 1.0f, 1.0f);
        this.size = 0.1f;
        this.speed = 1.0f;
        this.amount = 10;
        this.lifetime = 2.0f;
    }

    /**
     * Set Color
     */
    public void setColor(float r, float g, float b) {
        this.color.set(r, g, b);
    }

    public void setColor(Vector3f color) {
        this.color.set(color);
    }

    /**
     * Set Size
     */
    public void setSize(float size) {
        this.size = size;
    }

    /**
     * Set Speed
     */
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    /**
     * Set Amount
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }

    /**
     * Set Lifetime
     */
    public void setLifetime(float lifetime) {
        this.lifetime = lifetime;
    }

    /**
     * Is Active
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Emit
     */
    public void emit(Vector3f position) {
        this.position.set(position);
        this.isActive = true;
        this.particleId = "particle_" + System.currentTimeMillis();

        for(int i = 0; i < amount; i++) {
            Particle particle = new Particle();
            particle.position.set(position);

            particle.velocity.set(
                (random.nextFloat() - 0.5f) * 2.0f * speed,
                random.nextFloat() * 3.0f * speed,
                (random.nextFloat() - 0.5f) * 2.0f * speed
            );

            particle.color.set(color);
            particle.size = size;
            particle.lifetime = lifetime;
            particle.maxLifetime = lifetime;
            particle.id = particleId + "_" + i;
            
            particles.add(particle);
            createMesh(particle);
        }
    }

    /**
     * Cretae Mesh
     */
    private void createMesh(Particle particle) {
        MeshData meshData = MeshData.createQuad(particle.id, particle.size);
        meshData.setColorRgb(
            (int)(particle.color.x * 255),
            (int)(particle.color.y * 255),
            (int)(particle.color.z * 255),
            255
        );
        meshData.setShaderType(5);

        mesh.add(particle.id, meshData);
        mesh.setPosition(particle.id, particle.position);
    }

    /**
     * Update
     */
    public void update() {
        if(!isActive) return;

        List<Particle> particlesToRemove = new ArrayList<>();
        for(Particle particle : particles) {
            particle.lifetime -= tick.getDeltaTime();
            if(particle.lifetime <= 0) {
                particlesToRemove.add(particle);
                continue;
            }

            particle.velocity.y -= 9.8f * tick.getDeltaTime() * speed;
            particle.position.add(
                particle.velocity.x * tick.getDeltaTime(),
                particle.velocity.y * tick.getDeltaTime(),
                particle.velocity.z * tick.getDeltaTime()
            );

            mesh.setPosition(particle.id, particle.position);

            float alpha = particle.lifetime / particle.maxLifetime;
            mesh.getData(particle.id).setTransparentColor(alpha);
        }

        for(Particle particle : particlesToRemove) {
            mesh.remove(particle.id);
            particles.remove(particle);
        }
        if(particles.isEmpty()) {
            isActive = false;
        }
    }

    /**
     * Render
     */
    public void render() {
        if(!isActive) return;

        for(Particle particle : particles) {
            mesh.render(particle.id, 5);
        }
    }

    /**
     * Cleanup
     */
    public void cleanup() {
        for(Particle particle : particles) {
            mesh.remove(particle.id);
        }
    }
}
