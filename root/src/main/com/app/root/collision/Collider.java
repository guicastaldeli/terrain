package main.com.app.root.collision;
import main.com.app.root.player.RigidBody;

public interface Collider {
    BoundingBox getBoundingBox();
    RigidBody getRigidBody();
    void onCollision(CollisionResult coll);
}
