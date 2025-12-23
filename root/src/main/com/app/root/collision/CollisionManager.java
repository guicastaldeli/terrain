package main.com.app.root.collision;
import java.util.*;

import org.joml.Vector3f;

import main.com.app.root.collision.types.DynamicObject;
import main.com.app.root.collision.types.StaticObject;
import main.com.app.root.player_controller.RigidBody;

public class CollisionManager {
    public enum CollisionType {
        STATIC_OBJECT,
        DYNAMIC_OBJECT
    }

    private List<Collider> staticColliders = new ArrayList<>();
    private List<Collider> dynamicColliders = new ArrayList<>();

    /**
     * Add Static Collider
     */
    public void addStaticCollider(Collider coll) {
        staticColliders.add(coll);
    }

    /**
     * Add Dynamic Collider
     */
    public void addDynamicCollider(Collider coll) {
        dynamicColliders.add(coll);
    }

    /**
     * Remove Collider
     */
    public void removeCollider(Collider coll) {
        staticColliders.remove(coll);
        dynamicColliders.remove(coll);
    }

    /**
     * Check Collision
     */
    public CollisionResult checkCollision(RigidBody body) {
        BoundingBox bodyBounds = body.getBoundingBox();
        CollisionResult result = new CollisionResult();

        /* Static */
        for(Collider collider : staticColliders) {
            BoundingBox colliderBounds = collider.getBoundingBox();
            if(bodyBounds.intersects(colliderBounds)) {
                result = calcCollisionResponse(bodyBounds, colliderBounds);
                result.otherCollider = collider;
                result.type = 
                    collider instanceof StaticObject ?
                    CollisionType.STATIC_OBJECT : 
                    CollisionType.DYNAMIC_OBJECT;
            }
        }
        /* Dynamic */
        for(Collider collider : dynamicColliders) {
            if(collider.getRigidBody() == body) continue;

            BoundingBox colliderBounds = collider.getBoundingBox();
            if(bodyBounds.intersects(colliderBounds)) {
                result = calcCollisionResponse(bodyBounds, colliderBounds);
                result.otherCollider = collider;
                result.type = 
                    collider instanceof DynamicObject ?
                    CollisionType.DYNAMIC_OBJECT : 
                    CollisionType.STATIC_OBJECT;
            }
        }

        return result;
    }

    /**
     * Calculate Collision Response
     */
    private CollisionResult calcCollisionResponse(BoundingBox a, BoundingBox b) {
        float xOverlap = Math.min(a.maxX, b.maxX) - Math.max(a.minX, b.minX);
        float yOverlap = Math.min(a.maxY, b.maxY) - Math.max(a.minY, b.minY);
        float zOverlap = Math.min(a.maxZ, b.maxZ) - Math.max(a.minZ, b.minZ);
        
        float minOverlap = Math.min(Math.min(xOverlap, yOverlap), zOverlap);
        Vector3f normal = new Vector3f();
        if(minOverlap == xOverlap) {
            normal.set(a.maxX > b.maxX ? 1 : -1, 0, 0);
        } else if(minOverlap == yOverlap) {
            normal.set(0, a.maxY > b.maxY ? 1 : -1, 0);
        } else {
            normal.set(0, 0, a.maxZ > b.maxZ ? 1 : -1);
        }

        return new CollisionResult(
            true,
            normal,
            minOverlap,
            null,
            CollisionType.STATIC_OBJECT
        );
    }

    /**
     * Resolve Collision
     */
    public void resolveCollision(RigidBody body, CollisionResult collision) {
        if(!collision.collided) return;

        Vector3f correction = new Vector3f(collision.normal).mul(collision.depth);
        body.setPosition(body.getPosition().add(correction));

        Vector3f velocity = body.getVelocity();
        float dot = velocity.dot(collision.normal);
        if(dot < 0) {
            velocity.sub(collision.normal.mul(dot, new Vector3f()));
            if(collision.normal.y > 0.5f) {
                body.setOnGround(true);
            }
        }
        body.setVelocity(velocity);
    }

    /**
     * Update Dynamic Colliders
     */
    public void updateDynamicColliders(float deltaTime) {
        for(Collider coll : dynamicColliders) {
            RigidBody body = coll.getRigidBody();
            if(body != null && !body.isStatic()) {
                body.update(deltaTime);

                CollisionResult collision = checkCollision(body);
                if(collision.collided) {
                    resolveCollision(body, collision);
                } else {
                    body.setOnGround(false);
                }
            }
        }
    }
}
