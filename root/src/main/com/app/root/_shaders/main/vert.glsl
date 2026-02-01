#version 330 core

layout(location = 0) in vec3 inPos;
layout(location = 1) in vec2 aPos;
layout(location = 2) in vec4 aColor;
layout(location = 3) in vec2 aTexCoord;
layout(location = 4) in vec3 aNormal;
layout(location = 5) in vec3 instancePosition;
layout(location = 6) in vec3 instanceRotation;
layout(location = 7) in float instanceScale;
layout(location = 8) in ivec4 aBoneIds;
layout(location = 9) in vec4 aBoneWeights;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
uniform int isInstanced;
uniform vec2 screenSize;

const int MAX_BONES = 100;
uniform mat4 boneMatrices[MAX_BONES];
uniform int hasAnimation;

out vec3 worldPos;
out float fragDistance;
out vec3 normal;

out vec4 uColor;
out vec2 texCoord;

uniform int hasTex;
uniform int hasColors;
uniform int shaderType;

uniform vec3 uCameraPos;
uniform float uWaterLevel;
uniform float uTime;

#include "text/text_vert.glsl"
#include "mesh/mesh_color.glsl"
#include "../env/skybox/shaders/sb_vert.glsl"
#include "ui/ui_vert.glsl"
#include "cloud_vert.glsl"
#include "particle_vert.glsl"

vec4 applyAnim(vec3 position, out vec3 animatedNormal) {
    if(hasAnimation == 0) {
        animatedNormal = aNormal;
        return vec4(position, 1.0);
    }

    vec4 totalPosition = vec4(0.0);
    vec3 totalNormal = vec3(0.0);

    for(int i = 0; i < 4; i++) {
        int boneId = aBoneIds[i];
        float weight = aBoneWeights[i];
        if(boneId >= 0 && boneId < MAX_BONES && weight > 0.0) {
            mat4 boneTransform = boneMatrices[boneId];

            vec4 localPosition = boneTransform * vec4(position, 1.0);
            totalPosition += localPosition * weight;

            mat3 boneRotation = mat3(boneTransform);
            vec3 localNormal = boneRotation * aNormal;
            totalNormal += localNormal * weight;
        }
    }

    if(length(totalPosition.xyz) < 0.001) {
        totalPosition = vec4(position, 1.0);
        totalNormal = aNormal;
    } else {
        totalPosition.w = 1.0;
        totalNormal = normalize(totalNormal);
    }

    animatedNormal = totalNormal;
    return totalPosition;
}

void main() {
    vec3 animatedNormal;
    vec4 animatedPosition = applyAnim(inPos, animatedNormal);

    vec3 finalPos = animatedPosition.xyz;
    vec3 finalNormal = animatedNormal;

    if(isInstanced == 1) {
        finalPos *= instanceScale;

        float cosY = cos(instanceRotation.y);
        float sinY = sin(instanceRotation.y);
        mat3 rotationMatrix = mat3(
            cosY, 0.0, sinY,
            0.0, 1.0, 0.0,
            -sinY, 0.0, cosY
        );

        finalPos = rotationMatrix * finalPos;
        finalNormal = rotationMatrix * finalNormal;

        finalPos += instancePosition;
    }

    //Mesh
    if(shaderType == 0) {
        vec4 worldPosition;

        if(isInstanced == 1) {
            worldPosition = vec4(finalPos, 1.0);
            worldPos = finalPos;
            normal = normalize(finalNormal);
        } else {
            worldPosition = model * vec4(finalPos, 1.0);
            worldPos = worldPosition.xyz;
            normal = normalize(mat3(transpose(inverse(model))) * finalNormal);
        }

        uColor = hasColors > 0 ? aColor : vec4(1.0, 1.0, 1.0, 1.0);
        texCoord = aTexCoord;

        vec4 viewPos = view * worldPosition;
        fragDistance = length(viewPos.xyz);
        gl_Position = projection * viewPos;
    }
    //Skybox
    else if(shaderType == 2) {
        setSkyboxVert();
    }
    //Text
    else if(shaderType == 1) {
        setTextVert();
    }
    //UI
    else if(shaderType == 3) {
        setUIVert();
    }
    //Clouds
    else if(shaderType == 4) {
        setCloudVert(finalPos, finalNormal);
    }
    //Particles
    else if(shaderType == 5) {
        setParticleVert();
    }
    else {
        if(isInstanced == 1) {
            gl_Position = projection * view * vec4(finalPos, 1.0);
        } else {
            gl_Position = projection * view * model * vec4(finalPos, 1.0);
        }
        texCoord = aTexCoord;
        uColor = aColor;
    }
}