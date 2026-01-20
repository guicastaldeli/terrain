package main.com.app.root.collision.types;

import main.com.app.root.collision.BoundingBox;
import main.com.app.root.collision.Collider;
import main.com.app.root.collision.CollisionResult;
import main.com.app.root.player.RigidBody;

import org.joml.Vector3f;

public class DynamicObject implements Collider {
    private RigidBody rigidBody;
    private BoundingBox bBox;
    private String objectType;
    
    public DynamicObject(RigidBody rigidBody, String objectType) {
        this.rigidBody = rigidBody;
        this.objectType = objectType;
        updateBounds();
    }
    
    public DynamicObject(RigidBody rigidBody) {
        this(rigidBody, "");
    }

    /**
     * Update Bounds
     */
    private void updateBounds() {
        Vector3f pos = rigidBody.getPosition();
        Vector3f size = rigidBody.getSize();

        this.bBox = new BoundingBox(
            pos.x - size.x / 2,
            pos.y - size.y / 2,
            pos.z - size.z / 2,
            pos.x + size.x / 2,
            pos.y + size.y / 2,
            pos.z + size.z / 2
        );
    }

    /**
     * Handle Collision
     */
    private void handleCollision(CollisionResult coll) {
        switch (objectType) {
            case "WATER":
                rigidBody.applyForce(new Vector3f(0, 5.0f * rigidBody.getMass(), 0));
                Vector3f vel = rigidBody.getVelocity();
                vel.mul(0.8f);
                rigidBody.setVelocity(vel);
                break;
            case "PLAYER":
                System.out.println("Player collided with something");
                break;
                
            default:
                break;
        }
    }

    public void update(float deltaTime) {
        if(rigidBody != null && !rigidBody.isStatic()) {
            rigidBody.update();
        }
    }

    @Override
    public BoundingBox getBoundingBox() {
        updateBounds();
        return bBox;
    }

    @Override
    public RigidBody getRigidBody() {
        return rigidBody;
    }

    @Override
    public void onCollision(CollisionResult coll) {
        handleCollision(coll);
    }
    
    public String getObjectType() {
        return objectType;
    }
}