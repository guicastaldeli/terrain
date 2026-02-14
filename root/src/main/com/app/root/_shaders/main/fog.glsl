vec3 getColorFog(vec3 viewDir) {
    vec3 dir = normalize(viewDir);

    float heightFactor = clamp(dir.y * 0.5 + 0.5, 0.0, 1.0);

    float gradientStrength = 0.4;
    float gradientPower = 1.5;
    float gradient = mix(1.0 - gradientStrength, 1.0, pow(heightFactor, gradientPower));
    
    return uFogColor * gradient;
}

void setFog() {
    float fogStart = uRenderDistance * 0.7;
    float fogEnd = uRenderDistance;
    float fogFactor = clamp((fragDistance - fogStart) / (fogEnd - fogStart), 0.0, 1.0);
        
    
    vec3 viewDir = normalize(worldPos - uCameraPos);
    vec3 dynamicFogColor = getColorFog(viewDir);
        
    fragColor = mix(fragColor, vec4(dynamicFogColor, fragColor.a), fogFactor);
}