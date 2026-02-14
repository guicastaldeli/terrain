package main.com.app.root.mesh;
import org.joml.Matrix4f;
import java.util.*;

public class AnimatedModel {
    private final MeshData meshData;
    private final Skeleton skeleton;
    private final Map<String, Animation> animations;
    private final int maxBones;

    public AnimatedModel(
        MeshData meshData,
        Skeleton skeleton,
        int maxBones
    ) {
        this.meshData = meshData;
        this.skeleton = skeleton;
        this.animations = new HashMap<>();
        this.maxBones = maxBones;
    }

    public void addAnimation(String name, Animation animation) {
        animations.put(name, animation);
    }

    public Animation getAnimation(String name) {
        return animations.get(name);
    }

    public boolean hasAnimation(String name) {
        return animations.containsKey(name);
    }

    public Set<String> getAnimationNames() {
        return animations.keySet();
    }

    public MeshData getMeshData() {
        return meshData;
    }

    /**
     * 
     * Skeleton
     * 
     */
    public Skeleton getSkeleton() {
        return skeleton;
    }

    public int getMaxBones() {
        return maxBones;
    }

    public static class Skeleton {
        private final Bone rootBone;
        private final Map<String, Bone> boneMap;
        private final Matrix4f globalInverseTransform;

        public Skeleton(Bone rootBone, Matrix4f globalInverseTransform) {
            this.rootBone = rootBone;
            this.boneMap = new HashMap<>();
            this.globalInverseTransform = globalInverseTransform;
            buildBoneMap(rootBone);
        }

        private void buildBoneMap(Bone bone) {
            boneMap.put(bone.getName(), bone);
            for(Bone child : bone.getChildren()) {
                buildBoneMap(child);
            }
        }

        public Bone getRootBone() {
            return rootBone;
        }

        public Bone getBone(String name) {
            return boneMap.get(name);
        }

        public Map<String, Bone> getBoneMap() {
            return boneMap;
        }

        public Matrix4f getGlobalInverseTransform() {
            return globalInverseTransform;
        }
    }

    /**
     * 
     * Bone
     * 
     */
    public static class Bone {
        private final String name;
        private final int id;
        private final Matrix4f offsetMatrix;
        private final Matrix4f localTransform;
        private final List<Bone> children;
        private Bone parent;
        private boolean isRoot;

        public Bone(
            String name,
            int id,
            Matrix4f offsetMatrix,
            Matrix4f localTransform
        ) {
            this.name = name;
            this.id = id;
            this.offsetMatrix = offsetMatrix;
            this.localTransform = new Matrix4f(localTransform);
            this.children = new ArrayList<>();
            this.parent = null;
            this.isRoot = (name.equals("Armature") || parent == null);
        }

        public boolean isRoot() {
            return isRoot;
        }

        public void addChild(Bone child) {
            children.add(child);
            child.parent = this;
        }

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }

        public void setParent(Bone parent) {
            this.parent = parent;
            this.isRoot = (parent == null);
        }

        public Matrix4f getOffsetMatrix() {
            return offsetMatrix;
        }

        public Matrix4f getLocalTransform() {
            return localTransform;
        }

        public void setLocalTransform(Matrix4f transform) {
            this.localTransform.set(transform);
        }

        public List<Bone> getChildren() {
            return children;
        }

        public Bone getParent() {
            return parent;
        }
    }
}
