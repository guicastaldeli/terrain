#define MAX_DIRECTIONAL_LIGHTS 4

struct DirectionalLight {
    vec3 color;
    float intensity;
    vec3 direction;
    float range;
};

uniform int uDirectionalLightCount;
uniform DirectionalLight uDirectionalLights[MAX_DIRECTIONAL_LIGHTS];

vec3 calculateDirectionalLight(
    DirectionalLight light,
    vec3 surfaceColor,
    vec3 normal
) {
    vec3 lightDir = normalize(-light.direction);
    float diff = max(dot(normal, lightDir), 0.0);
    
    return surfaceColor * light.color * light.intensity * diff;
}

vec3 calculateAllDirectionalLights(
    vec3 surfaceColor,
    vec3 normal
) {
    vec3 res = vec3(0.0);
    for(int i = 0; i < MAX_DIRECTIONAL_LIGHTS; i++) {
        if(i >= uDirectionalLightCount) break;
        res += calculateDirectionalLight(
            uDirectionalLights[i],
            surfaceColor,
            normal
        );
    }
    return res;
}