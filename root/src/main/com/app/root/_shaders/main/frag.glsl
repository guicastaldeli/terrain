#version 330

in vec4 uColor;
in vec2 texCoord;
out vec4 fragColor;

uniform sampler2D texSampler;
uniform int hasTex;
uniform int shaderType;
uniform vec3 textColor;

uniform vec3 uSkyColorStart;
uniform vec3 uSkyColorEnd;
uniform vec3 uNextColorStart;
uniform float uBlendFactor;
uniform float uStarBrightness;
in vec3 worldPos;

#include "text/text_frag.glsl"
#include "mesh/mesh_tex.glsl"
#include "../env/skybox/shaders/sb_frag.glsl"
#include "ui/ui_frag.glsl"

void main() {
    //Mesh
    if(shaderType == 0) {
        setMeshTex();
    }
    //Skybox
    if(shaderType == 2) {
        setSkyboxFrag();
    }
    //Text
    else if(shaderType == 1) {
        setTextFrag();
    }
    //UI
    else if(shaderType == 3) {
        setUIFrag();
    }
}