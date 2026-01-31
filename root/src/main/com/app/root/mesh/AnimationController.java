package main.com.app.root.mesh;
import java.util.HashMap;
import java.util.Map;

public class AnimationController {
    private final Map<String, MeshAnimator> animators;
    private final Map<String, AnimatedModel> animatedModels;

    public AnimationController() {
        this.animators = new HashMap<>();
        this.animatedModels = new HashMap<>();
    }
}
