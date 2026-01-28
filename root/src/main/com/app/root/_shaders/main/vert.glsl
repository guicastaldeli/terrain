#version 330 core

layout(location = 0) in vec3 inPos;
layout(location = 1) in vec2 aPos;
layout(location = 2) in vec4 aColor;
layout(location = 3) in vec2 aTexCoord;
layout(location = 4) in vec3 aNormal;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
uniform vec2 screenSize;

out vec3 worldPos;
out float fragDistance;
out vec3 normal;

out vec4 uColor;
out vec2 texCoord;

uniform int hasTex;
uniform int hasColors;
uniform int shaderType;

#include "text/text_vert.glsl"
#include "mesh/mesh_color.glsl"
#include "../env/skybox/shaders/sb_vert.glsl"
#include "ui/ui_vert.glsl"

void main() {
    //Mesh
    if(shaderType == 0) {
        setMeshColor();

        vec4 worldPosition = model * vec4(inPos, 1.0);
        worldPos = worldPosition.xyz;
        
        normal = normalize(mat3(transpose(inverse(model))) * aNormal);
        
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
    else if(shaderType == 3) {
        setUIVert();
    }
    else {
        gl_Position  = projection * view * model * vec4(inPos, 1.0);
        texCoord = aTexCoord;
    }
}