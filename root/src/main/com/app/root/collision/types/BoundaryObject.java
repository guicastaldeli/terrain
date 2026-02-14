package main.com.app.root.collision.types;
import main.com.app.root.collision.BoundingBox;
import main.com.app.root.collision.Collider;
import main.com.app.root.collision.CollisionResult;
import main.com.app.root.env.world.WorldGenerator;
import main.com.app.root.player.RigidBody;
import org.joml.Vector3f;

public class BoundaryObject implements Collider {
    private BoundingBox bBox;
    private float distance;
    private float thickness = 100.0f;

    public BoundaryObject() {
        this.distance = WorldGenerator.WORLD_SIZE * 0.8f;
        this.bBox = new BoundingBox(
            -distance - thickness,
            -Float.MAX_VALUE,
            -distance - thickness,
            distance + thickness,
            Float.MAX_VALUE,
            distance + thickness  
        );
    }

    @Override 
    public BoundingBox getBoundingBox() {
        return bBox;
    }

    @Override
    public RigidBody getRigidBody() {
        return null;
    }
    
    @Override
    public void onCollision(CollisionResult coll) {}
    
    public boolean isOutsideBoundary(Vector3f position) {
        return Math.abs(position.x) > distance ||
            Math.abs(position.z) > distance;
    }

    /**
     * Get Boundary Normal
     */
    public Vector3f getBoundaryNormal(Vector3f position) {
        Vector3f normal = new Vector3f(0, 0, 0);
        if(Math.abs(position.x) > distance) {
            normal.x = position.x > 0 ? -1 : 1;
        } else if(Math.abs(position.z) > distance) {
            normal.z = position.z > 0 ? -1 : 1;
        }
        return normal;
    }

    /**
     * Get Boundary Far
     */
    public float getBoundaryFar(Vector3f position) {
        float xFar = Math.max(0, Math.abs(position.x) - distance);
        float zFar = Math.max(0, Math.abs(position.z) - distance);
        return Math.max(xFar, zFar);
    }

    public float getBoundaryDistance() {
        return distance;
    }
}
