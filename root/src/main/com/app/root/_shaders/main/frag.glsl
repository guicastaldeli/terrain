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

uniform float uRenderDistance;
uniform vec3 uFogColor;
uniform float uFogDensity;

#include "text/text_frag.glsl"
#include "mesh/mesh_tex.glsl"
#include "../env/skybox/shaders/sb_frag.glsl"
#include "ui/ui_frag.glsl"
#include "lightning/ambient.glsl"
#include "lightning/directional.glsl"
#include "lightning/point.glsl"

void main() {
    //Mesh
    if(shaderType == 0) {
        setMeshTex();
        /*
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
        
        //Ambient lighting
        finalColor = calculateAmbientLight(uAmbientLight, finalColor);
        
        //Directional lighting
        finalColor += calculateDirectionalLight(uDirectionalLight, fragColor.rgb, normalize(normal));
        
        //Point lighting
        finalColor += calculatePointLight(uPointLight, fragColor.rgb, normalize(normal), worldPos);
        
        fragColor = vec4(finalColor, fragColor.a);

        //Fog
        float fogStart = uRenderDistance * 0.7;
        float fogEnd = uRenderDistance;
        float fogFactor = clamp((fragDistance - fogStart) / (fogEnd - fogStart), 0.0, 1.0);
        fragColor = mix(fragColor, vec4(uFogColor, fragColor.a), fogFactor);
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