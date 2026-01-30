package main.com.app.root.mesh.particle;
import main.com.app.root.Tick;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.mesh.MeshData;
import main.com.app.root.mesh.MeshData.MeshType;
import main.com.app.root.mesh.types.Particle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import org.joml.Vector3f;

public class ParticleSystem {
    private final Tick tick;
    private final Mesh mesh;
    private final Random random;
    
    public List<Particle> particles;
    private boolean isActive;
    private Vector3f position;
    private Vector3f color;
    private float size;
    private float speed;
    private int amount;
    private float lifetime;
    private String particleId;

    private boolean vel;
    private Vector3f velNum;

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
     * Set Vel Num
     */
    public void setVelNum(Vector3f velNum) {
        this.velNum = velNum;
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
    public void emit(
        Vector3f position, 
        boolean vel,
        Supplier<Vector3f> colorsSupplier
    ) {
        this.vel = vel;

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

            if(colorsSupplier != null) {
                particle.color.set(colorsSupplier.get());
            } else {
                particle.color.set(this.color);
            }

            particle.size = size;
            particle.lifetime = lifetime;
            particle.maxLifetime = lifetime;
            particle.id = particleId + "_" + i;
            
            particles.add(particle);
            createMesh(particle);
        }
    }

    public void emit(Vector3f position, boolean vel) {
        emit(position, vel, null);
    }

    /**
     * Create Mesh
     */
    public void createMesh(Particle particle) {
        try {
            mesh.add(particle.id, MeshType.QUAD);
            mesh.setPosition(particle.id, particle.position);

            MeshData data = mesh.getData(particle.id);
            if(data != null) {
                data.setShaderType(5);
                
                float[] colors = new float[16];
                for(int i = 0; i < 16; i += 4) {
                    colors[i] = particle.color.x;
                    colors[i+1] = particle.color.y;
                    colors[i+2] = particle.color.z;
                    colors[i+3] = 1.0f;
                }
                data.setColors(colors);
                mesh.getMeshRenderer(particle.id).updateColors(colors);
            }
            
            mesh.setScale(particle.id, particle.size);
        } catch(Exception err) {
            System.err.println("Failed to create mesh: " + err.getMessage());
            err.printStackTrace();
        }
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
            
            if(vel) {
                particle.position.add(
                    particle.velocity.x * tick.getDeltaTime() * velNum.x,
                    particle.velocity.y * tick.getDeltaTime() * velNum.y,
                    particle.velocity.z * tick.getDeltaTime() * velNum.z
                );
            } else {
                particle.position.add(
                    particle.velocity.x * tick.getDeltaTime(),
                    particle.velocity.y * tick.getDeltaTime(),
                    particle.velocity.z * tick.getDeltaTime()
                );
            }

            mesh.setPosition(particle.id, particle.position);

            float alpha = particle.lifetime / particle.maxLifetime;
            MeshData data = mesh.getData(particle.id);
            if(data != null) {
                float[] colors = new float[16];
                for(int i = 0; i < 16; i += 4) {
                    colors[i] = particle.color.x;
                    colors[i+1] = particle.color.y;
                    colors[i+2] = particle.color.z;
                    colors[i+3] = alpha;
                }
                data.setColors(colors);
            }
        }

        for(Particle particle : particlesToRemove) {
            mesh.remove(particle.id);
            particles.remove(particle);
        }
        particles.removeAll(particlesToRemove);
        
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
        particles.clear();
    }
}