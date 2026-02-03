package main.com.app.root.mesh;
import main.com.app.root.Tick;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MeshAnimator {
    private final AnimatedModel animatedModel;
    private Animation currentAnimation;
    private float currentTime;
    private boolean isPlaying;
    private boolean isLooping;
    private float playbackSpeed;
    private final Matrix4f[] boneMatrices;

    public MeshAnimator(AnimatedModel animatedModel) {
        this.animatedModel = animatedModel;
        this.currentTime = 0.0f;
        this.isPlaying = false;
        this.isLooping = true;
        this.playbackSpeed = 1.0f;
        this.boneMatrices = new Matrix4f[animatedModel.getMaxBones()];

        for(int i = 0; i < boneMatrices.length; i++) {
            boneMatrices[i] = new Matrix4f();
        }
    }

    /**
     * Get Animation Progress
     */
    public float getAnimationProgress() {
        if(currentAnimation == null || currentAnimation.getDuration() == 0) {
            return 0.0f;
        }
        return currentTime / currentAnimation.getDuration();
    }

    /**
     * Get Animated Model
     */
    public AnimatedModel getAnimatedModel() {
        return animatedModel;
    }

    /**
     * Get Node Transform
     */
    public Matrix4f getNodeTransform() {
        if(boneMatrices != null && boneMatrices.length > 0) {
            return new Matrix4f(boneMatrices[0]);
        }
        return new Matrix4f();
    }

    /**
     * Blend Animation
     */
    public void blendAnimation(String animationName, float blendTime) {
        //Do later..
        playAnimation(animationName);        
    }

    /**
     * Calculate Node Transform
     */
    private void calcNodeTransform(
    AnimatedModel.Bone bone,
    Matrix4f parentTransform,
    Animation animation
) {
    Matrix4f nodeTransform = new Matrix4f(bone.getLocalTransform());
    Animation.NodeAnimation nodeAnim = animation.getNodeAnimation(bone.getName());
    if(nodeAnim != null) {
        Vector3f position = nodeAnim.getInterpoledPosition(currentTime);
        Quaternionf rotation = nodeAnim.getInterpoledRotation(currentTime);
        Vector3f scale = nodeAnim.getInterpoledScale(currentTime);
        
        nodeTransform = new Matrix4f()
            .translate(position)
            .rotate(rotation)
            .scale(scale);
    }

    Matrix4f globalTransform = new Matrix4f(parentTransform).mul(nodeTransform);

    int boneId = bone.getId();
    if(boneId >= 0 && boneId < boneMatrices.length) {
        // HERE'S THE FIX: Apply global inverse transform and offset matrix
        Matrix4f finalTransform = new Matrix4f(
            animatedModel.getSkeleton().getGlobalInverseTransform()
        )
        .mul(globalTransform)
        .mul(bone.getOffsetMatrix());
        
        boneMatrices[boneId] = finalTransform;
    }

    for(AnimatedModel.Bone child : bone.getChildren()) {
        calcNodeTransform(child, globalTransform, animation);
    }
}

    /**
     * 
     * Bone
     * 
     */
    private void calcBoneTransforms() {
        if(currentAnimation == null) {
            return;
        }

        Matrix4f identity = new Matrix4f();
        calcNodeTransform(
            animatedModel.getSkeleton().getRootBone(),
            identity,
            currentAnimation
        );
    }
    
    public Matrix4f[] getBoneMatrices() {
        return boneMatrices;
    }

    public float[] getBoneMatricesArray() {
        float[] result = new float[boneMatrices.length * 16];
        for(int i = 0; i < boneMatrices.length; i++) {
            boneMatrices[i].get(result, i * 16);
        }
        return result;
    }

    /**
     * 
     * Play Animation
     * 
     */
    public void playAnimation(String name) {
        Animation anim = animatedModel.getAnimation(name);
        if(anim != null) {
            this.currentAnimation = anim;
            this.currentTime = 0.0f;
            this.isPlaying = true;
        } else {
            System.err.println("Aniamtion not found " + name);
        }
    }

    public void playAnimation(
        String name,
        boolean loop,
        float speed
    ) {
        this.isLooping = loop;
        this.playbackSpeed = speed;
        playAnimation(name);
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    /**
     * 
     * Playback
     * 
     */
    public void setPlaybackSpeed(float speed) {
        this.playbackSpeed = speed;
    }

    public float getPlaybackSpeed() {
        return playbackSpeed;
    }

    /**
     * 
     * Pause
     * 
     */
    public void pause() {
        isPlaying = false;
    }

    /**
     * 
     * Resume
     * 
     */
    public void resume() {
        if(currentAnimation != null) {
            isPlaying = true;
        }
    }

    /**
     * 
     * Stop
     * 
     */
    public void stop() {
        isPlaying = false;
        currentTime = 0.0f;
    }

    /**
     * 
     * Time
     * 
     */
    public void setTime(float time) {
        if(currentAnimation != null) {
            this.currentTime = Math.max(0, Math.min(time, currentAnimation.getDuration()));
            calcBoneTransforms();
        }
    }

    public float getCurrentTime() {
        return currentTime;
    }

    /**
     * 
     * Update
     * 
     */
    public void update() {
        if(!isPlaying || currentAnimation == null) {
            return;
        }

        currentTime += Tick.getIDeltaTime() * playbackSpeed;
        if(currentTime > currentAnimation.getDuration()) {
            if(isLooping) {
                currentTime = currentTime % currentAnimation.getDuration();
            } else {
                currentTime = currentAnimation.getDuration();
                isPlaying = false;
            }
        }

        calcBoneTransforms();
    }
}
