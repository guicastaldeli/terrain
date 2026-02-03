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

    private boolean enableMotion;
    private float swayAmplitude;
    private float swayFrequency;

    private final Vector3f _rotationScratch = new Vector3f();
    private final float[] _colorBuffer = new float[16];

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
        this.enableMotion = false;
        this.swayAmplitude = 2.0f;
        this.swayFrequency = 1.0f;
        this.velNum = new Vector3f(1.0f, 1.0f, 1.0f);
    }

    public void setVelNum(Vector3f velNum) {
        this.velNum = velNum;
    }

    public void setColor(float r, float g, float b) {
        this.color.set(r, g, b);
    }

    public void setColor(Vector3f color) {
        this.color.set(color);
    }

    public void setSize(float size) {
        this.size = size;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setLifetime(float lifetime) {
        this.lifetime = lifetime;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setMotion(boolean enable, float swayAmplitude, float swayFrequency) {
        this.enableMotion = enable;
        this.swayAmplitude = swayAmplitude;
        this.swayFrequency = swayFrequency;
    }

    /**
     * Emit
     */
    public void emit(Vector3f position, boolean vel, Supplier<Vector3f> colorsSupplier) {
        this.vel = vel;
        this.position.set(position);
        this.isActive = true;
        this.particleId = "particle_" + System.currentTimeMillis() + "_" + random.nextInt(100000);

        for(int i = 0; i < amount; i++) {
            Particle particle = new Particle();
            particle.position.set(position);

            if(!enableMotion) {
                particle.velocity.set(
                    (random.nextFloat() - 0.5f) * 2.0f * speed,
                    random.nextFloat() * 3.0f * speed,
                    (random.nextFloat() - 0.5f) * 2.0f * speed
                );
            }

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

            particle.cachedMeshData = mesh.getData(particle.id);
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

        for(int i = particles.size() - 1; i >= 0; i--) {
            Particle particle = particles.get(i);
            particle.lifetime -= tick.getDeltaTime();

            if(particle.lifetime <= 0) {
                mesh.remove(particle.id);
                particles.remove(i);
                continue;
            }

            if(!enableMotion) {
                particle.velocity.y -= 9.8f * tick.getDeltaTime() * speed;
            }
            
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

            if(particle.cachedMeshData != null) {
                if(enableMotion) {
                    particle.rotation += particle.rotationSpeed * tick.getDeltaTime();
                    _rotationScratch.set(0, 0, particle.rotation);
                    particle.cachedMeshData.setRotation(_rotationScratch);
                }

                float alpha = particle.lifetime / particle.maxLifetime;
                for(int c = 0; c < 16; c += 4) {
                    _colorBuffer[c]     = particle.color.x;
                    _colorBuffer[c + 1] = particle.color.y;
                    _colorBuffer[c + 2] = particle.color.z;
                    _colorBuffer[c + 3] = alpha;
                }
                particle.cachedMeshData.setColors(_colorBuffer);
            }
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
        particles.clear();
    }
}