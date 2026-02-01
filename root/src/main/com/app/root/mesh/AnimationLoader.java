package main.com.app.root.mesh;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import static org.lwjgl.assimp.Assimp.*;

public class AnimationLoader {
    private static final int MAX_BONES = 100;
    private static final int FLAGS = 
        aiProcess_Triangulate |
        aiProcess_FlipUVs |
        aiProcess_GenSmoothNormals |
        aiProcess_JoinIdenticalVertices |
        aiProcess_LimitBoneWeights;

    /**
     * 
     * Load
     * 
     */
    public static AnimatedModel loadAnimatedModel(String filePath, String meshId) {
        AIScene scene = aiImportFile(filePath, FLAGS);
        if(scene == null) {
            throw new RuntimeException("Failed to load animated model: " + filePath +
                "\nError: " + aiGetErrorString()
            );
        }

        try {
            MeshData meshData = loadMeshWithBones(scene, 0, meshId);
            AnimatedModel.Skeleton skeleton = loadSkeleton(scene);
            AnimatedModel animatedModel = new AnimatedModel(
                meshData, 
                skeleton, 
                MAX_BONES
            );

            int animationCount = scene.mNumAnimations();
            if(animationCount > 0) {
                PointerBuffer animationsBuffer = scene.mAnimations();
                for(int i = 0; i < animationCount; i++) {
                    AIAnimation aiAnimation = AIAnimation.create(animationsBuffer.get(i));
                    Animation animation = loadAnimation(aiAnimation, skeleton);
                    animatedModel.addAnimation(animation.getName(), animation);
                    System.out.println("Loaded animation: " + animation.getName() + 
                        " (duration: " + animation.getDuration() + "s)");
                }
            }
            
            return animatedModel;
        } finally {
            aiReleaseImport(scene);
        }
    }

    private static MeshData loadMeshWithBones(
        AIScene scene,
        int meshIndex,
        String meshId
    ) {
        PointerBuffer meshesBuffer = scene.mMeshes();
        AIMesh aiMesh = AIMesh.create(meshesBuffer.get(meshIndex));

        MeshData meshData = new MeshData(meshId, MeshData.MeshType.GLTF);

        /* Vertices */
        AIVector3D.Buffer vertices = aiMesh.mVertices();
        float[] vertexArray = new float[aiMesh.mNumVertices() * 3];
        for(int i = 0; i < aiMesh.mNumVertices(); i++) {
            AIVector3D vertex = vertices.get(i);
            vertexArray[i * 3] = vertex.x();
            vertexArray[i * 3 + 1] = vertex.y();
            vertexArray[i * 3 + 2] = vertex.z();
        }
        meshData.setVertices(vertexArray);

        /* Normals */
        if(aiMesh.mNormals() != null) {
            AIVector3D.Buffer normals = aiMesh.mNormals();
            float[] normalArray = new float[aiMesh.mNumVertices() * 3];
            for(int i = 0; i < aiMesh.mNumVertices(); i++) {
                AIVector3D normal = normals.get(i);
                normalArray[i * 3] = normal.x();
                normalArray[i * 3 + 1] = normal.y();
                normalArray[i * 3 + 2] = normal.z();
            }
            meshData.setNormals(normalArray);
        }

        /* Tex Coords */
        if(aiMesh.mTextureCoords(0) != null) {
            AIVector3D.Buffer texCoords = aiMesh.mTextureCoords(0);
            float[] texCoordArray = new float[aiMesh.mNumVertices() * 2];
            for(int i = 0; i < aiMesh.mNumVertices(); i++) {
                AIVector3D texCoord = texCoords.get(i);
                texCoordArray[i * 2] = texCoord.x();
                texCoordArray[i * 2 + 1] = texCoord.y(); 
            }
            meshData.setTexCoords(texCoordArray);
        }

        /* Indices */
        int faceCount = aiMesh.mNumFaces();
        int indexCount = faceCount * 3;
        int[] indices = new int[indexCount];
        AIFace.Buffer facesBuffer = aiMesh.mFaces();
        for(int i = 0; i < faceCount; i++) {
            AIFace face = facesBuffer.get(i);
            IntBuffer indicesBuffer = face.mIndices();
            indices[i * 3] = indicesBuffer.get(0);
            indices[i * 3 + 1] = indicesBuffer.get(1);
            indices[i * 3 + 2] = indicesBuffer.get(2);
        }
        meshData.setIndices(indices);

        int[] boneIds = new int[aiMesh.mNumVertices() * 4];
        float[] boneWeights = new float[aiMesh.mNumVertices() * 4];
        for(int i = 0; i < boneIds.length; i++) {
            boneIds[i] = 0;
            boneWeights[i] = 0.0f;
        }

        int numBones = aiMesh.mNumBones();
        if(numBones > 0) {
            PointerBuffer bonesBuffer = aiMesh.mBones();
            int[] boneCounter = new int[aiMesh.mNumVertices()];
            for(int boneIndex = 0; boneIndex < numBones && boneIndex < MAX_BONES; boneIndex++) {
                AIBone bone = AIBone.create(bonesBuffer.get(boneIndex));

                int numWeights = bone.mNumWeights();
                AIVertexWeight.Buffer weightsBuffer = bone.mWeights();
                for(int weightIndex = 0; weightIndex < numWeights; weightIndex++) {
                    AIVertexWeight weight = weightsBuffer.get(weightIndex);
                    int vertexId = weight.mVertexId();

                    if(boneCounter[vertexId] < 4) {
                        int offset = vertexId * 4 + boneCounter[vertexId];
                        boneIds[offset] = boneIndex;
                        boneWeights[offset] = weight.mWeight();
                        boneCounter[vertexId]++;
                    }
                }
            }
        }

        meshData.addData(MeshData.DataType.BONE_IDS, boneIds);
        meshData.addData(MeshData.DataType.BONE_WEIGHTS, boneWeights);
        return meshData;
    }

    private static AnimatedModel.Skeleton loadSkeleton(AIScene scene) {
        AINode rootNode = scene.mRootNode();

        Map<String, BoneInfo> boneMap = new HashMap<>();
        int boneCounter = 0;

        PointerBuffer meshesBuffer = scene.mMeshes();
        for(int meshIdx = 0; meshIdx < scene.mNumMeshes(); meshIdx++) {
            AIMesh aiMesh = AIMesh.create(meshesBuffer.get(meshIdx));
            int numBones = aiMesh.mNumBones();
            if(numBones > 0) {
                PointerBuffer bonesBuffer = aiMesh.mBones();
                for(int i = 0; i < numBones && boneCounter < MAX_BONES; i++) {
                    AIBone bone = AIBone.create(bonesBuffer.get(i));
                    String boneName = bone.mName().dataString();
                    if(!boneMap.containsKey(boneName)) {
                        Matrix4f offsetMatrix = convertMatrix(bone.mOffsetMatrix());
                        boneMap.put(boneName, new BoneInfo(boneCounter++, offsetMatrix));
                    }
                }
            }
        }

        if(boneMap.isEmpty()) {
            System.out.println("No mesh bones found - using node hierarchy for animation");
            collectAllNodes(rootNode, boneMap, new int[]{0});
        }

        System.out.println("Skeleton bones (total: " + boneMap.size() + "):");
        for(Map.Entry<String, BoneInfo> entry : boneMap.entrySet()) {
            System.out.println("  Bone: " + entry.getKey() + ", ID: " + entry.getValue().id);
        }

        Matrix4f globalInverseTransform = convertMatrix(rootNode.mTransformation()).invert();
        AnimatedModel.Bone rootBone = buildBoneHierarchy(rootNode, null, boneMap);

        return new AnimatedModel.Skeleton(rootBone, globalInverseTransform);
    }

    private static void collectAllNodes(AINode node, Map<String, BoneInfo> boneMap, int[] counter) {
        String nodeName = node.mName().dataString();
        if(!boneMap.containsKey(nodeName) && counter[0] < MAX_BONES) {
            Matrix4f offsetMatrix = new Matrix4f();
            boneMap.put(nodeName, new BoneInfo(counter[0]++, offsetMatrix));
        }
        
        // Recursively collect child nodes
        int numChildren = node.mNumChildren();
        if(numChildren > 0) {
            PointerBuffer childrenBuffer = node.mChildren();
            for(int i = 0; i < numChildren; i++) {
                AINode childNode = AINode.create(childrenBuffer.get(i));
                collectAllNodes(childNode, boneMap, counter);
            }
        }
    }

    private static Animation loadAnimation(AIAnimation aiAnimation, AnimatedModel.Skeleton skeleton) {
        String name = aiAnimation.mName().dataString();
        if(name.isEmpty()) name = "Animation_" + System.currentTimeMillis();

        float duration = (float) aiAnimation.mDuration();
        float ticksPerSec = (float) aiAnimation.mTicksPerSecond();
        if(ticksPerSec == 0) ticksPerSec = 25.0f;
        float durationInSecs = duration / ticksPerSec;

        Animation animation = new Animation(
            name, 
            durationInSecs, 
            ticksPerSec
        );

        int numChannels = aiAnimation.mNumChannels();
        PointerBuffer channelsBuffer = aiAnimation.mChannels();

        for(int i = 0; i < numChannels; i++) {
            AINodeAnim nodeAnim = AINodeAnim.create(channelsBuffer.get(i));
            String nodeName = nodeAnim.mNodeName().dataString();
            Animation.NodeAnimation animNode = new Animation.NodeAnimation();

            int numPosKeys = nodeAnim.mNumPositionKeys();
            AIVectorKey.Buffer posKeys = nodeAnim.mPositionKeys();
            for(int j = 0; j < numPosKeys; j++) {
                AIVectorKey key = posKeys.get(j);
                float time = (float) key.mTime() / ticksPerSec;
                AIVector3D pos = key.mValue();
                animNode.addPositionKeyframe(
                    time, 
                    new Vector3f(
                        pos.x(), 
                        pos.y(), 
                        pos.z()
                    )
                );
            }

            int numRotationKeys = nodeAnim.mNumRotationKeys();
            AIQuatKey.Buffer rotationKeys = nodeAnim.mRotationKeys();
            for(int j = 0; j < numRotationKeys; j++) {
                AIQuatKey key = rotationKeys.get(j);
                float time = (float) key.mTime() / ticksPerSec;
                AIQuaternion rotation = key.mValue();
                animNode.addRoationKeyframe(
                    time, 
                    new Quaternionf(
                        rotation.x(), 
                        rotation.y(), 
                        rotation.z(), 
                        rotation.w()
                    )
                );
            }

            int numScaleKeys = nodeAnim.mNumScalingKeys();
            AIVectorKey.Buffer scaleKeys = nodeAnim.mScalingKeys();
            for(int j = 0; j < numScaleKeys; j++) {
                AIVectorKey key = scaleKeys.get(j);
                float time = (float) key.mTime() / ticksPerSec;
                AIVector3D scale = key.mValue();
                animNode.addScalekeyframe(
                    time,
                    new Vector3f(
                        scale.x(),
                        scale.y(),
                        scale.z() 
                    )
                );
            }

            animation.addNodeAnimation(nodeName, animNode);
        }

        return animation;
    }

    /**
     * Build Bone Hierarchy
     */
    private static AnimatedModel.Bone buildBoneHierarchy(
    AINode node,
    AnimatedModel.Bone parent,
    Map<String, BoneInfo> boneMap
) {
    String nodeName = node.mName().dataString();
    Matrix4f nodeTransform = convertMatrix(node.mTransformation());

    BoneInfo boneInfo = boneMap.get(nodeName);
    int boneId = boneInfo != null ? boneInfo.id : -1;
    Matrix4f offsetMatrix = boneInfo != null ? boneInfo.offsetMatrix : new Matrix4f();

    AnimatedModel.Bone bone = new AnimatedModel.Bone(
        nodeName, 
        boneId, 
        offsetMatrix, 
        nodeTransform
    );

    // BUG FIX: Set parent on the bone BEFORE adding children
    if (parent != null) {
        bone.setParent(parent);
    }

    int numChildren = node.mNumChildren();
    if(numChildren > 0) {
        PointerBuffer childrenBuffer = node.mChildren();
        for(int i = 0; i < numChildren; i++) {
            AINode childNode = AINode.create(childrenBuffer.get(i));
            // BUG FIX: Pass 'bone' as parent, not 'parent'
            AnimatedModel.Bone childBone = buildBoneHierarchy(childNode, bone, boneMap);
            bone.addChild(childBone);
        }
    }

    return bone;
}

    /**
     * Convert Matrix
     */
    private static Matrix4f convertMatrix(AIMatrix4x4 aiMatrix) {
        Matrix4f matrix = new Matrix4f();
        matrix.m00(aiMatrix.a1());
        matrix.m10(aiMatrix.a2());
        matrix.m20(aiMatrix.a3());
        matrix.m30(aiMatrix.a4());
        matrix.m01(aiMatrix.b1());
        matrix.m11(aiMatrix.b2());
        matrix.m21(aiMatrix.b3());
        matrix.m31(aiMatrix.b4());
        matrix.m02(aiMatrix.c1());
        matrix.m12(aiMatrix.c2());
        matrix.m22(aiMatrix.c3());
        matrix.m32(aiMatrix.c4());
        matrix.m03(aiMatrix.d1());
        matrix.m13(aiMatrix.d2());
        matrix.m23(aiMatrix.d3());
        matrix.m33(aiMatrix.d4());
        return matrix;
    }

    /**
     * 
     * Bone Info
     * 
     */
    private static class BoneInfo {
        int id;
        Matrix4f offsetMatrix;

        BoneInfo(int id, Matrix4f offsetMatrix) {
            this.id = id;
            this.offsetMatrix = offsetMatrix;
        }
    }
}
