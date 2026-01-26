package main.com.app.root.player;
import main.com.app.root.Tick;
import main.com.app.root.collision.BoundingBox;
import org.joml.Vector3f;

public class RigidBody {
    private final Tick tick;

    private final Vector3f position;
    private Vector3f velocity;
    private Vector3f acceleration;
    private final Vector3f size;

    private float mass;
    private boolean isStatic;
    private boolean onGround;
    private boolean isInWater = false;

    private boolean gravityEnabled = true;
    private float gravity = -30.0f;
    private float gravityScale = 3.0f;
    private float drag = 0.1f;
    private float submergedRatio = 0.0f;

    public RigidBody(Tick tick, Vector3f position, Vector3f size) {
        this.tick = tick;
        this.position = new Vector3f(position);
        this.size = new Vector3f(size);
        this.velocity = new Vector3f();
        this.acceleration = new Vector3f();
        this.mass = 1.0f;
        this.isStatic = false;
        this.onGround = false;
    }

    /**
     * Apply Force
     */
    public void applyForce(Vector3f force) {
        if(!isStatic) acceleration.add(force.div(mass));
    }

    /**
     * Position
     */
    public void setPosition(Vector3f position) { 
        this.position.set(position); 
    }

    public Vector3f getPosition() { 
        return new Vector3f(position); 
    }
    
    /**
     * Velocity
     */
    public void setVelocity(Vector3f velocity) { 
        this.velocity.set(velocity); 
    }

    public Vector3f getVelocity() { 
        return new Vector3f(velocity); 
    }
    
    /**
     * Size
     */
    public void setSize(Vector3f size) { 
        this.size.set(size); 
    }

    public Vector3f getSize() { 
        return new Vector3f(size); 
    }
    
    /**
     * On Ground
     */
    public void setOnGround(boolean onGround) { 
        this.onGround = onGround; 
    }

    public boolean isOnGround() { 
        return onGround; 
    }

    /**
     * In Water
     */
    public void setInWater(boolean inWater, float ratio) {
        this.isInWater = inWater;
        this.submergedRatio = ratio;
    }

    public boolean isInWater() {
        return isInWater;
    }

    public float getSubmergetRatio() {
        return submergedRatio;
    }
    
    /**
     * Static
     */
    public void setStatic(boolean isStatic) { 
        this.isStatic = isStatic; 
    }

    public boolean isStatic() { 
        return isStatic; 
    }
    
    /**
     * Mass
     */
    public float getMass() { 
        return mass; 
    }

    public void setMass(float mass) { 
        this.mass = mass; 
    }
    
    /**
     * Gravity
     */
    public float getGravity() {
        return gravity;
    }

    public void setGravityScale(float scale) { 
        this.gravityScale = scale; 
    }

    public float getGravityScale() {
        return gravityScale;
    }

    public void setGravityEnabled(boolean enabled) {
        this.gravityEnabled = enabled;
    }

    public boolean isGravityEnabled() {
        return gravityEnabled;
    }

    /**
     * Bounding Box
     */
    public BoundingBox getBoundingBox() {
        return new BoundingBox(
            position.x - size.x / 2,
            position.y - size.y / 2,
            position.z - size.z / 2,
            position.x + size.x / 2,
            position.y + size.y / 2,
            position.z + size.z / 2
        );
    }

    /**
     * Update
     */
    public void update() {
        float deltaTime = tick.getDeltaTime();
        if(isStatic) return;

        if(gravityEnabled && !onGround) {
            applyForce(new Vector3f(
                0, 
                gravity * mass * gravityScale, 
                0
            ));
        }
        velocity.add(acceleration.mul(deltaTime, new Vector3f()));
        velocity.mul(1.0f - (drag * deltaTime));

        Vector3f newPosition = 
            new Vector3f(position).add(
            velocity.mul(deltaTime, new Vector3f())
        );
        position.set(newPosition);
        
        acceleration.set(0, 0, 0);
    }
}