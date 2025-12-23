#version 330

in vec4 uColor;
in vec2 texCoord;
out vec4 fragColor;

uniform sampler2D texSampler;
uniform int hasTex;
uniform int shaderType;
uniform vec3 textColor;

#include "text/text_frag.glsl"
#include "mesh/mesh_tex.glsl"
#include "../env/skybox/shaders/sb_frag.glsl"

void main() {
    //Mesh
    if(shaderType == 0) {
        setMeshTex();
    }
    //Skybox
    else if(shaderType == 2) {
        setSkyboxFrag();
    }
    //Text
    else if(shaderType == 1) {
        setTextFrag();
    }
}