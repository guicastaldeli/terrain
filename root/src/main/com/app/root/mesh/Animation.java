package main.com.app.root.mesh;
import org.joml.Quaterniond;
import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Animation {
    private final String name;
    private final float duration;
    private final Map<String, NodeAnimation> nodeAnimations;

    public Animation(String name, float duration) {
        this.name = name;
        this.duration = duration;
        this.nodeAnimations = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public float getDuration() {
        return duration;
    }

    /**
     * 
     * Node Animation
     * 
     */
    public void addNodeAnimation(String nodeName, NodeAnimation nodeAnimation) {
        nodeAnimations.put(nodeName, nodeAnimation);
    }

    public NodeAnimation getNodeAnimation(String nodeName) {
        return nodeAnimations.get(nodeName);
    }

    public static class NodeAnimation {
        private final List<PositionKeyframe> positionKeyframes;
        private final List<RotationKeyframe> rotationKeyframes;
        private final List<ScaleKeyframe> scaleKeyframes;

        public NodeAnimation() {
            this.positionKeyframes = new ArrayList<>();
            this.rotationKeyframes = new ArrayList<>();
            this.scaleKeyframes = new ArrayList<>();
        }

        public void addPositionKeyframe(float time, Quaterniond rotation) {
            rotationKeyframes.add(new RotationKeyframe(time, rotation));
        }

        public void addScalekeyframe(float time, Vector3f scale) {
            scaleKeyframes.add(new ScaleKeyframe(time, scale));
        }

        /**
         * Get Interpolated Position
         */
        public Vector3f getInterpoledPosition(float animationTime) {
            if(positionKeyframes.isEmpty()) {
                return new Vector3f(0, 0, 0);
            }
            if(positionKeyframes.size() == 1) {
                return new Vector3f(positionKeyframes.get(0).position);
            }

            int frameIndex = 0;
            for(int i = 0; i < positionKeyframes.size() - 1; i++) {
                if(animationTime < positionKeyframes.get(i+1).time) {
                    frameIndex = i;
                    break;
                }
            }

            PositionKeyframe currentFrame = positionKeyframes.get(frameIndex);
            PositionKeyframe nextFrame = positionKeyframes.get(
                Math.min(
                    frameIndex + 1,
                    positionKeyframes.size() - 1
                )
            );

            float deltaTime = nextFrame.time - currentFrame.time;
            float factor = deltaTime > 0 ? (animationTime - currentFrame.time) / deltaTime : 0;
            factor = Math.max(0, Math.min(1, factor));

            Vector3f result = new Vector3f();
            currentFrame.position.lerp(nextFrame.position, factor, result);
            return result;
        }

        /**
         * Get Interpoated Rotation
         */
        public Quaterniond getInterpoledRotation(float animationTime) {
            if(rotationKeyframes.isEmpty()) {
                return new Quaterniond(0, 0, 0, 1);
            }
            if(rotationKeyframes.size() == 1) {
                return new Quaterniond(rotationKeyframes.get(0).rotation);
            }

            int frameIndex = 0;
            for(int i = 0; i < rotationKeyframes.size() - 1; i++) {
                if(animationTime < rotationKeyframes.get(i+1).time) {
                    frameIndex = i;
                    break;
                }
            }

            RotationKeyframe currentFrame = rotationKeyframes.get(frameIndex);
            RotationKeyframe nextFrame = rotationKeyframes.get(
                Math.min(
                    frameIndex + 1,
                    rotationKeyframes.size() - 1
                )
            );

            float deltaTime = nextFrame.time - currentFrame.time;
            float factor = deltaTime > 0 ? (animationTime - currentFrame.time) / deltaTime : 0;
            factor = Math.max(0, Math.min(1, factor));

            Quaterniond result = new Quaterniond();
            currentFrame.rotation.slerp(nextFrame.rotation, factor, result);
            return result;
        }

        /**
         * Get Interpoled Scale
         */
        public Vector3f getInterpoledScale(float animationTime) {
            if(scaleKeyframes.isEmpty()) {
                return new Vector3f(1, 1, 1);
            }
            if(scaleKeyframes.size() == 1) {
                return new Vector3f(scaleKeyframes.get(0).scale);
            }

            int frameIndex = 0;
            for(int i = 0; i < scaleKeyframes.size() - 1; i++) {
                if(animationTime < scaleKeyframes.get(i+1).time) {
                    frameIndex = i;
                    break;
                }
            }

            ScaleKeyframe currentFrame = scaleKeyframes.get(frameIndex);
            ScaleKeyframe nextFrame = scaleKeyframes.get(
                Math.min(
                    frameIndex + 1,
                    scaleKeyframes.size() - 1
                )
            );

            float deltaTime = nextFrame.time - currentFrame.time;
            float factor = deltaTime > 0 ? (animationTime - currentFrame.time) / deltaTime : 0;
            factor = Math.max(0, Math.min(1, factor));

            Vector3f result = new Vector3f();
            currentFrame.scale.lerp(nextFrame.scale, factor, result);
            return result;
        }

        public List<PositionKeyframe> getPositionKeyframes() {
            return positionKeyframes;
        }

        public List<RotationKeyframe> getRotationKeyframes() {
            return rotationKeyframes;
        }

        public List<ScaleKeyframe> getScaleKeyframes() {
            return scaleKeyframes;
        }
    }

    /**
     * 
     * Position Keyframe
     * 
     */
    public static class PositionKeyframe {
        public final float time;
        public final Vector3f position;

        public PositionKeyframe(float time, Vector3f position) {
            this.time = time;
            this.position = position;
        }
    }

    /**
     * 
     * Rotation Keyframe
     * 
     */
    public static class RotationKeyframe {
        public final float time;
        public final Quaterniond rotation;
        
        public RotationKeyframe(float time, Quaterniond rotation) {
            this.time = time;
            this.rotation = rotation;
        }
    }

    /**
     * 
     * Scale Keyframe
     * 
     */
    public static class ScaleKeyframe {
        public final float time;
        public final Vector3f scale;

        public ScaleKeyframe(float time, Vector3f scale) {
            this.time = time;
            this.scale = scale;
        }
    }
}
