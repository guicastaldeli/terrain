package main.com.app.root.collision.types;
import main.com.app.root.collision.BoundingBox;
import main.com.app.root.collision.Collider;
import main.com.app.root.collision.CollisionResult;
import main.com.app.root.collision.CollisionManager.CollisionType;
import main.com.app.root.env.world.Water;
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

    /**
     * Check Collision
     */
    public CollisionResult checkCollision(BoundingBox playerBox) {
        boolean isInWater = 
            playerBox.maxY > Water.MIN_Y &&
            playerBox.minY < Water.LEVEL;
    
        if(isInWater) {
            Vector3f normal = new Vector3f(0, 1, 0);
            float submergedHeight = 
                Math.min(playerBox.maxY, Water.LEVEL) -
                Math.max(playerBox.minY, Water.MIN_Y);
            submergedHeight = Math.max(submergedHeight, 0.1f);

            return new CollisionResult(
                true,
                normal,
                Math.max(0, submergedHeight),
                this,
                CollisionType.DYNAMIC_OBJECT
            );
        }

        return new CollisionResult();
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

    /**
     * Set Collision
     */
    private static void setCollision(
        Vector3f position,
        BoundingBox bBox,
        RigidBody body, 
        CollisionResult collision
    ) {
        DynamicObject dynamicObj = (DynamicObject) collision.otherCollider;
        if("WATER".equals(dynamicObj.getObjectType())) {
            float submergedRatio = collision.depth / bBox.getSizeY();
            
            if(submergedRatio > 0) {
                System.out.println("Player submerged in water: " + (submergedRatio * 100) + "%");
                
                Vector3f velocity = body.getVelocity();
                velocity.mul(0.8f);
                body.setVelocity(velocity);
            }
            if(position.y - bBox.getSizeY() / 2 < Water.MIN_Y) {
                position.y = Water.MIN_Y + bBox.getSizeY()/2;
                body.setPosition(position);
                
                Vector3f velocity = body.getVelocity();
                if(velocity.y < 0) {
                    velocity.y = 0;
                }
                body.setVelocity(velocity);
            }

            body.setOnGround(false);
            return;
        }
    }

    /**
     * Resolve Collision
     */
    public static void resolveCollision(
        Vector3f position,
        BoundingBox bBox,
        RigidBody body, 
        CollisionResult collision
    ) {
        DynamicObject dynamicObj = (DynamicObject) collision.otherCollider;
        if("WATER".equals(dynamicObj.getObjectType())) {
            float submergedRatio = collision.depth / body.getBoundingBox().getSizeY();
            body.setInWater(true, submergedRatio);
            setCollision(
                position, 
                bBox, 
                body, 
                collision
            );
        }
    }
}