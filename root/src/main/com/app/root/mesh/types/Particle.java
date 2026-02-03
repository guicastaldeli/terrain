package main.com.app.root.mesh.types;
import org.joml.Vector3f;

public class Particle {
    public Vector3f position;
    public Vector3f basePosition = new Vector3f();
    public Vector3f velocity;
    public Vector3f color;
    public float size;
    public float lifetime;
    public float maxLifetime;
    public String id;

    public float rotation;
    public float rotationSpeed;
    public float swayPhase;
    public float swayAmplitude;
    public float swayFrequency;
    public Vector3f initialVelocity;
    public Vector3f swayVelocity;   

    public Particle() {//
        this.position = new Vector3f();
        this.velocity = new Vector3f();
        this.color = new Vector3f();
        this.rotation = 0.0f;
        this.rotationSpeed = 0.0f;
        this.swayPhase = 0.0f;
        this.swayAmplitude = 1.0f;
        this.swayFrequency = 1.0f;
        this.initialVelocity = new Vector3f();
        this.swayVelocity = new Vector3f();
    }
}
