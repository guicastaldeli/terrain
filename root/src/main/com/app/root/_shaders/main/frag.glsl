#version 330

in vec4 uColor;
in vec2 texCoord;
in vec3 worldPos;
in float fragDistance;

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

uniform float uRenderDistance;
uniform vec3 uFogColor;
uniform float uFogDensity;

#include "text/text_frag.glsl"
#include "mesh/mesh_tex.glsl"
#include "../env/skybox/shaders/sb_frag.glsl"
#include "ui/ui_frag.glsl"

void main() {
    //Mesh
    if(shaderType == 0) {
        setMeshTex();

        float fogStart = uRenderDistance * 0.7;
        float fogEnd = uRenderDistance;
        float fogFactor = clamp((fragDistance - fogStart) / (fogEnd - fogStart), 0.0, 1.0);
        fragColor = mix(fragColor, vec4(uFogColor, fragColor.a), fogFactor);
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