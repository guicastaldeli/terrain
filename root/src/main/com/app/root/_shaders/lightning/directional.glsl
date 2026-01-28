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
    float diff = max(dot(normal, normalize(-light.direction)), 0.0);
    return surfaceColor * light.color * light.intensity * diff;
}