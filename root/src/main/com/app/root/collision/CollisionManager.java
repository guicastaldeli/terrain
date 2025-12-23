package main.com.app.root.collision;
import java.util.*;

public class CollisionManager {
    public enum CollisionType {
        STATIC_OBJECT,
        DYNAMIC_OBJECT
    }

    private List<Collider> staticColliders = new ArrayList<>();
    private List<Collider> dynamicColliders = new ArrayList<>();
}
