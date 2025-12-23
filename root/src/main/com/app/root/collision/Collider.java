package main.com.app.root.collision;
import main.com.app.root.player_controller.RigidBody;

public interface Collider {
    BoundingBox getBoundingBox();
    RigidBody getRigidBody();
    void onCollision(CollisionResult coll);
}
