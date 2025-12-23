package main.com.app.root.collision;

import org.joml.Vector3f;

import main.com.app.root.collision.CollisionManager.CollisionType;

public class CollisionResult {
    public boolean collided = false;
    public Vector3f normal = new Vector3f();
    public float depth = 0.0f;
    public Collider otherCollider = null;   
    public CollisionType type = CollisionType.STATIC_OBJECT;

    public CollisionResult() {};
    public CollisionResult(
        boolean collided,
        Vector3f normal,
        float depth,
        Collider other,
        CollisionType type
    ) {
        this.collided = collided;
        this.normal.set(normal);
        this.depth = depth;
        this.otherCollider = other;
        this.type = type;
    }
}
