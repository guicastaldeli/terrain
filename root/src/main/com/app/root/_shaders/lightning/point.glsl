#define MAX_POINT_LIGHTS 32

struct PointLight {
    vec3 color;
    float intensity;
    vec3 position;
    float radius;
    float attenuation;
};

uniform PointLight uPointLights[MAX_POINT_LIGHTS];
uniform int uPointLightCount;

vec3 calculatePointLight(
    PointLight light, 
    vec3 surfaceColor, 
    vec3 normal, 
    vec3 fragPos
) {
    vec3 lightDir = normalize(light.position - fragPos);
    float diff = max(dot(normal, lightDir), 0.0);

    float distance = length(light.position - fragPos);
    if(distance > light.radius) {
        return vec3(0.0);
    }
    
    float attenuation = 1.0 / (1.0 + light.attenuation * distance * distance);
    
    float radiusFalloff = 1.0 - smoothstep(light.radius * 0.75, light.radius, distance);
    attenuation *= radiusFalloff;

    return surfaceColor * light.color * light.intensity * diff * attenuation;
}

vec3 calculateAllPointLights(
    vec3 surfaceColor,
    vec3 normal,
    vec3 fragPos
) {
    vec3 result = vec3(0.0);
    for(int i = 0; i < uPointLightCount; i++) {
        result += calculatePointLight(
            uPointLights[i],
            surfaceColor,
            normal,
            fragPos
        );
    }
    return result;
}