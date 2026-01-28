struct DirectionalLight {
    vec3 color;
    float intensity;
    vec3 direction;
};

uniform DirectionalLight uDirectionalLight;

vec3 calculateDirectionalLight(
    DirectionalLight light, 
    vec3 surfaceColor, 
    vec3 normal
) {
    vec3 lightDir = normalize(-light.direction);
    float diff = max(dot(normal, lightDir), 0.0);
    return surfaceColor * light.color * light.intensity * diff;
}