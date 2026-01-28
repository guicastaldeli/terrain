struct PointLight {
    vec3 color;
    float intensity;
    vec3 position;
    float radius;
    float attenuation;
};

uniform PointLight uPointLight;

vec3 calculatePointLight(
    PointLight light, 
    vec3 surfaceColor, 
    vec3 normal, 
    vec3 fragPos
) {
    vec3 lightDir = normalize(light.position - fragPos);
    float diff = max(dot(normal, lightDir), 0.0);

    float distance = length(light.position - fragPos);
    float attenuation = 1.0 / (1.0 + light.attenuation * distance * distance);

    return surfaceColor * light.color * light.intensity * diff * attenuation;
}