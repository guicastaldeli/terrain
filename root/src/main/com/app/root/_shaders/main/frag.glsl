#version 330

in vec4 uColor;
in vec2 texCoord;
in vec3 worldPos;
in float fragDistance;
in vec3 normal;

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
uniform vec3 uDirectionalLightOrigin;

uniform float uRenderDistance;
uniform float uFogDensity;
uniform vec3 uFogColor;
uniform vec3 uCameraPos;

#include "text/text_frag.glsl"
#include "mesh/mesh_tex.glsl"
#include "../env/skybox/shaders/sb_frag.glsl"
#include "ui/ui_frag.glsl"
#include "lightning/ambient.glsl"
#include "lightning/directional.glsl"
#include "lightning/point.glsl"
#include "fog.glsl"

void main() {
    //Mesh
    if(shaderType == 0) {
        setMeshTex();
        /* TEST NORMALS, for test ONLY :>>
        vec3 finalColor = fragColor.rgb;
        if(length(normal) < 0.001) {
            finalColor = vec3(1.0, 0.0, 0.0);
        } else {
            vec3 normalizedNormal = normalize(normal);
            finalColor = normalizedNormal * 0.5 + 0.5;
        }
        
        fragColor = vec4(finalColor, fragColor.a);
        */
        
        vec3 finalColor = fragColor.rgb;
        vec3 normalizedNormal = normalize(normal);
        
        //Ambient Light
        vec3 ambientResult = calculateAmbientLight(
            uAmbientLight, 
            finalColor
        );
        
        //Directional Light
        vec3 directionalResult = calculateDirectionalLight(
            uDirectionalLight, 
            finalColor, 
            normalizedNormal,
            worldPos,
            uDirectionalLightOrigin
        );
        
        //Point Light
        vec3 pointResult = calculateAllPointLights(
            fragColor.rgb, 
            normalizedNormal, 
            worldPos
        );
        
        finalColor = pointResult;
        fragColor = vec4(finalColor, fragColor.a);

        //Fog
        setFog();
    }
    //Skybox
    else if(shaderType == 2) {
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