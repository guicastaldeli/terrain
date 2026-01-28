package main.com.app.root.collision;
import main.com.app.root.collision.types.BoundaryObject;
import main.com.app.root.collision.types.DynamicObject;
import main.com.app.root.collision.types.StaticObject;
import main.com.app.root.env.world.Water;
import main.com.app.root.player.RigidBody;
import org.joml.Vector3f;
import java.util.*;

public class CollisionManager {
    public enum CollisionType {
        STATIC_OBJECT,
        DYNAMIC_OBJECT,
        MAP
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
        
        for(Collider collider : staticColliders) {
            if(collider instanceof BoundaryObject) {
                BoundaryObject boundaryCollider = (BoundaryObject) collider;
                Vector3f position = body.getPosition();
                
                if(boundaryCollider.isOutsideBoundary(position)) {
                    Vector3f normal = boundaryCollider.getBoundaryNormal(position);
                    float penetration = boundaryCollider.getBoundaryFar(position);
                    
                    return new CollisionResult(
                        true,
                        normal,
                        penetration,
                        boundaryCollider,
                        CollisionType.STATIC_OBJECT
                    );
                }
            }
        }
        for(Collider collider : staticColliders) {
            if(collider instanceof DynamicObject) {
                DynamicObject dynamicObj = (DynamicObject) collider;
                if("WATER".equals(dynamicObj.getObjectType())) {
                    CollisionResult result = dynamicObj.checkCollision(bodyBounds);
                    if(result.collided) {
                        return result;
                    }
                }
            }
        }
        for(Collider collider : staticColliders) {
            if(collider instanceof StaticObject) {
                StaticObject staticObj = (StaticObject) collider;
                if(staticObj.isMap()) {
                    CollisionResult result = staticObj.checkMapCollision(bodyBounds);
                    if(result.collided) {
                        return result;
                    }
                }
            }
        }
        
        return new CollisionResult();
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
        if(!collision.collided) {
            body.setInWater(false, 0.0f);
            body.setOnGround(false);
            return;
        }

        Vector3f position = body.getPosition();
        BoundingBox bBox = body.getBoundingBox();

        if(collision.otherCollider instanceof BoundaryObject) {
            BoundaryObject boundaryObject = (BoundaryObject) collision.otherCollider;
            
            Vector3f newPosition = new Vector3f(position);
            if(Math.abs(position.x) > boundaryObject.getBoundaryDistance()) {
                newPosition.x = Math.copySign(boundaryObject.getBoundaryDistance(), position.x);
            }
            if(Math.abs(position.z) > boundaryObject.getBoundaryDistance()) {
                newPosition.z = Math.copySign(boundaryObject.getBoundaryDistance(), position.z);
            }
            
            body.setPosition(newPosition);
            
            Vector3f velocity = body.getVelocity();
            if(collision.normal.x != 0) velocity.x = 0;
            if(collision.normal.z != 0) velocity.z = 0;
            body.setVelocity(velocity);
            
            return;
        }
        if(collision.otherCollider instanceof DynamicObject) {
            DynamicObject dynObj = (DynamicObject) collision.otherCollider;
            if("WATER".equals(dynObj.getObjectType())) {
                for(Collider collider : staticColliders) {
                    if(collider instanceof StaticObject) {
                        StaticObject staticObj = (StaticObject) collider;
                        if(staticObj.isMap()) {
                            float terrainHeight = staticObj.getHeightAtWorld(position.x, position.z);
                            float playerBottom = bBox.minY;
                            
                            if(playerBottom <= terrainHeight + 5.0f && terrainHeight > Water.LEVEL - 10.0f) {
                                CollisionResult terrainCollision = staticObj.checkMapCollision(bBox);
                                if(terrainCollision.collided) {
                                    StaticObject.resolveCollision(position, bBox, body, terrainCollision);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
            
            DynamicObject.resolveCollision(position, bBox, body, collision);
            return;
        }
        if(collision.otherCollider instanceof StaticObject) {
            StaticObject.resolveCollision(position, bBox, body, collision);
        } else {
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

            if(collision.otherCollider != null) collision.otherCollider.onCollision(collision);
        }
        
        if(!(collision.otherCollider instanceof DynamicObject) || 
            !"WATER".equals(((DynamicObject)collision.otherCollider).getObjectType())
        ) {
            body.setInWater(false, 0.0f);
        }
    }

    /**
     * Update Dynamic Colliders
     */
    public void updateDynamicColliders(float deltaTime) {
        for(Collider coll : dynamicColliders) {
            RigidBody body = coll.getRigidBody();
            if(body != null && !body.isStatic()) {
                body.update();

                CollisionResult collision = checkCollision(body);
                if(collision.collided) {
                    resolveCollision(body, collision);
                } else {
                    body.setOnGround(false);
                }
                if(coll instanceof DynamicObject) {
                    ((DynamicObject) coll).update(deltaTime);
                }
            }
        }
    }
}
