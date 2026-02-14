struct AmbientLight {
    vec3 color;
    float intensity;
};

uniform AmbientLight uAmbientLight;

vec3 calculateAmbientLight(AmbientLight light, vec3 surfaceColor) {
    return surfaceColor * light.color * light.intensity;
}