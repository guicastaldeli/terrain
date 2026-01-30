package main.com.app.root.mesh.types;
import org.joml.Vector3f;

public class Particle {
    public Vector3f position;
    public Vector3f velocity;
    public Vector3f color;
    public float size;
    public float lifetime;
    public float maxLifetime;
    public String id;

    public Particle() {
        this.position = new Vector3f();
        this.velocity = new Vector3f();
        this.color = new Vector3f();
    }
}
