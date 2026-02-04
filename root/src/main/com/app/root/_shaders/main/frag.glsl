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
uniform float uTime;
uniform float uWaterLevel;
uniform float uPlayerTerrainHeight;

uniform sampler2D uTexSand;
uniform sampler2D uTexGrass;
uniform sampler2D uTexRock;
uniform sampler2D uTexSnow;
uniform int hasWorldTex;

in vec4 vTexBlend;
in float vTexBlend4;

uniform int isWater;
uniform float uWaterOpacity;

#include "text/text_frag.glsl"
#include "mesh/mesh_tex.glsl"
#include "../env/skybox/shaders/sb_frag.glsl"
#include "ui/ui_frag.glsl"
#include "lightning/ambient.glsl"
#include "lightning/directional.glsl"
#include "lightning/point.glsl"
#include "fog.glsl"
#include "cloud_frag.glsl"
#include "test_normals.glsl"
#include "particle_frag.glsl"

void main() {
    //Mesh
    if(shaderType == 0) {
        //testNormals();
        
        if(isWater == 1) {
            vec4 waterColor = texture(texSampler, texCoord);
            waterColor.a *= uWaterOpacity;
            fragColor.rgb = waterColor.rgb;
        } else {
            setMeshTex();
        }
        
        vec3 finalColor = fragColor.rgb;
        vec3 normalizedNormal = normalize(normal);
        
        //Ambient Light
        vec3 ambientResult = calculateAmbientLight(
            uAmbientLight, 
            finalColor
        );
        
        //Directional Light
        vec3 directionalResult = calculateAllDirectionalLights(
            finalColor,
            normalizedNormal
        );
        
        //Point Light
        vec3 pointResult = calculateAllPointLights(
            fragColor.rgb, 
            normalizedNormal, 
            worldPos
        );
        
        finalColor = 
            ambientResult + 
            directionalResult + 
            pointResult;
        
        fragColor = vec4(finalColor, fragColor.a);
        setFog();

        if(worldPos.y < uWaterLevel) {
            vec4 waterColor = vec4(0.0, 0.1, 0.4, 0.4);
            fragColor.rgb = mix(fragColor.rgb, waterColor.rgb, waterColor.a);
        }
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
    else if(shaderType == 4) {
        setCloudFrag();
    }
    else if(shaderType == 5) {
        setParticleFrag();
    }
}