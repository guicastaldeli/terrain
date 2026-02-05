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
    
    // Add these variables to track ground state
    private boolean wasOnGround = false;
    private float groundStickThreshold = 0.1f; // Stick to ground when close
    private float groundSnapDistance = 0.5f; // Snap to ground within this distance

    private boolean gravityEnabled = true;
    private float gravity = -20.0f;
    private float gravityScale = 3.0f;
    private float drag = 0.1f;
    private float submergedRatio = 0.0f;
    
    // Add for smoother movement
    private Vector3f lastSafePosition;
    private boolean needsPositionCorrection = false;

    public RigidBody(Tick tick, Vector3f position, Vector3f size) {
        this.tick = tick;
        this.position = new Vector3f(position);
        this.size = new Vector3f(size);
        this.velocity = new Vector3f();
        this.acceleration = new Vector3f();
        this.mass = 1.0f;
        this.isStatic = false;
        this.onGround = false;
        this.lastSafePosition = new Vector3f(position);
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
        // Update last safe position when we manually set position
        this.lastSafePosition.set(position);
    }

    public Vector3f getPosition() { 
        return new Vector3f(position); 
    }
    
    /**
     * Velocity
     */
    public void setVelocity(Vector3f velocity) { 
        this.velocity.set(velocity); 
        // When velocity is set manually, clear vertical velocity if on ground
        if(onGround && velocity.y < 0) {
            this.velocity.y = 0;
        }
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
        // Track ground state change
        if(this.onGround != onGround) {
            this.wasOnGround = this.onGround;
        }
        this.onGround = onGround; 
        
        // When we become on ground, zero vertical velocity
        if(onGround && velocity.y < 0) {
            velocity.y = 0;
        }
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
     * Apply ground sticking force to prevent bouncing
     */
    private void applyGroundSticking() {
        if(onGround && velocity.y < 0) {
            // Apply extra downward force when on ground to prevent bouncing
            float stickForce = -velocity.y * 5.0f; // Strong force to stick
            velocity.y += stickForce * tick.getDeltaTime();
            
            // Clamp to prevent overshooting
            if(velocity.y > 0) velocity.y = 0;
        }
    }
    
    /**
     * Get the last safe position (for collision recovery)
     */
    public Vector3f getLastSafePosition() {
        return new Vector3f(lastSafePosition);
    }
    
    /**
     * Mark position as safe (used after successful collision check)
     */
    public void markPositionAsSafe() {
        lastSafePosition.set(position);
        needsPositionCorrection = false;
    }
    
    /**
     * Check if we need to snap to ground
     */
    public boolean shouldSnapToGround(float groundHeight) {
        float playerBottom = position.y - size.y / 2;
        float distanceToGround = playerBottom - groundHeight;
        
        // Snap if we're close to ground and moving downward or stationary
        return Math.abs(distanceToGround) <= groundSnapDistance && 
               (velocity.y <= 0 || Math.abs(velocity.y) < 0.1f);
    }
    
    /**
     * Snap to ground position
     */
    public void snapToGround(float groundHeight) {
        float targetY = groundHeight + size.y / 2;
        position.y = targetY;
        velocity.y = 0;
        onGround = true;
        wasOnGround = true;
    }

    /**
     * Update
     */
    public void update() {
        float deltaTime = tick.getDeltaTime();
        deltaTime = Math.min(deltaTime, 0.1f);
        
        if(isStatic) return;

        // Store position before applying forces
        Vector3f previousPosition = new Vector3f(position);
        
        // Apply ground sticking to prevent bouncing
        applyGroundSticking();
        
        // Only apply gravity if not on ground
        if(gravityEnabled && !onGround) {
            applyForce(new Vector3f(
                0, 
                gravity * mass * gravityScale, 
                0
            ));
        } else if(onGround) {
            // When on ground, zero vertical acceleration
            acceleration.y = 0;
            
            // Apply slight downward force to stick to ground
            if(!wasOnGround) {
                // Just landed - apply extra sticking force
                velocity.y = Math.min(velocity.y, -1.0f); // Small downward force
            }
        }
        
        // Update velocity and position
        velocity.add(acceleration.mul(deltaTime, new Vector3f()));
        
        // Apply drag (except when on ground for horizontal movement)
        if(onGround) {
            // Only apply horizontal drag when on ground
            velocity.x *= (1.0f - (drag * deltaTime));
            velocity.z *= (1.0f - (drag * deltaTime));
            // No vertical drag when on ground
        } else {
            velocity.mul(1.0f - (drag * deltaTime));
        }
        
        // Clamp velocity when on ground to prevent micro-bouncing
        if(onGround && Math.abs(velocity.y) < 0.1f) {
            velocity.y = 0;
        }

        Vector3f newPosition = 
            new Vector3f(position).add(
            velocity.mul(deltaTime, new Vector3f())
        );
        
        // Store the movement for this frame
        position.set(newPosition);
        
        // Clear acceleration for next frame
        acceleration.set(0, 0, 0);
        
        // Update ground state tracking
        wasOnGround = onGround;
        
        // If we moved significantly and are on ground, mark as safe
        if(onGround && position.distanceSquared(previousPosition) > 0.0001f) {
            markPositionAsSafe();
        }
    }
}