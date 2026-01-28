struct DirectionalLight {
    vec3 color;
    float intensity;
    vec3 direction;
    float range;
};

uniform DirectionalLight uDirectionalLight;

vec3 calculateDirectionalLight(
    DirectionalLight light,
    vec3 surfaceColor,
    vec3 normal,
    vec3 fragPos,
    vec3 lightOrigin
) {
    vec3 lightDir = normalize(-light.direction);
    float diff = max(dot(normal, lightDir), 0.0);
    
    float distance = length(fragPos - lightOrigin);
    float attenuation = 1.0 - clamp(distance / light.range, 0.0, 1.0);
    attenuation = attenuation * attenuation;
    
    return surfaceColor * light.color * light.intensity * diff * attenuation;
}