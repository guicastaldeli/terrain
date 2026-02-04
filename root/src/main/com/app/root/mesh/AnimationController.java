package main.com.app.root.mesh;
import java.util.*;

public class AnimationController {
    private Mesh mesh;
    private final Map<String, MeshAnimator> animators;
    private final Map<String, AnimatedModel> animatedModels;

    public AnimationController(Mesh mesh) {
        this.mesh = mesh;
        this.animators = new HashMap<>();
        this.animatedModels = new HashMap<>();
    }

    /**
     * Get Animator
     */
    public MeshAnimator getAnimator(String id) {
        return animators.get(id);
    }

    /**
     * 
     * Animated Model
     * 
     */
    public void registerAnimatedModel(String id, AnimatedModel model) {
        animatedModels.put(id, model);
        MeshAnimator animator = new MeshAnimator(model);
        animators.put(id, animator);
        
        MeshRenderer renderer = mesh.getMeshRenderer(id);
        if(renderer != null) {
            renderer.setMeshAnimator(animator);
        }
        System.out.println("Animated model added: " + id + " with " + model.getAnimationNames().size() + " animations");
    }

    public void removeAnimatedModel(String id) {
        animators.remove(id);
        animatedModels.remove(id);
        System.out.println("Removed animated model: " + id);
    }

    public AnimatedModel getAnimatedModel(String id) {
        return animatedModels.get(id);
    }

    public boolean hasAnimatedModel(String id) {
        return animatedModels.containsKey(id);
    }

    /**
     * 
     * Animation
     * 
     */
    public Set<String> getAnimationNames(String modelId) {
        AnimatedModel model = animatedModels.get(modelId);
        if(model != null) return model.getAnimationNames();
        return new HashSet<>();
    }

    public boolean hasAnimation(String modelId, String animationName) {
        AnimatedModel model = animatedModels.get(modelId);
        return model != null && model.hasAnimation(animationName);
    }

    /**
     * 
     * Play
     * 
     */
    public void play(String modelId, String animationName) {
        MeshAnimator meshAnimator = animators.get(modelId);
        if(meshAnimator != null) {
            meshAnimator.playAnimation(animationName);
            System.out.println("Player animations: " + animationName);
        } else {
            System.err.println("No animator found for model: " + modelId);
        }
    }

    public void play(
        String modelId,
        String animationName,
        boolean loop,
        float speed
    ) {
        MeshAnimator meshAnimator = animators.get(modelId);
        if(meshAnimator != null) {
            meshAnimator.playAnimation(animationName, loop, speed);
        } else {
            System.err.println("No animator found for model: " + modelId);
        }
    }

    /**
     * 
     * Pause
     * 
     */
    public void pause(String modelId) {
        MeshAnimator meshAnimator = animators.get(modelId);
        if(meshAnimator != null) meshAnimator.pause();
    }

    /**
     * 
     * Resume
     * 
     */
    public void resume(String modelId) {
        MeshAnimator meshAnimator = animators.get(modelId);
        if(meshAnimator != null) meshAnimator.resume();
    }

    /**
     * 
     * Stop
     * 
     */
    public void stop(String modelId) {
        MeshAnimator meshAnimator = animators.get(modelId);
        if(meshAnimator != null) meshAnimator.stop();
    }

    /**
     * 
     * Update
     * 
     */
    public void update(String modelId) {
        MeshAnimator meshAnimator = animators.get(modelId);
        if(meshAnimator != null) meshAnimator.update();
    }

    public void updateAll() {
        for(MeshAnimator meshAnimator : animators.values()) {
            meshAnimator.update();
        }
    }

    /**
     * Clear
     */
    public void clear() {
        animators.clear();
        animatedModels.clear();
    }
}
