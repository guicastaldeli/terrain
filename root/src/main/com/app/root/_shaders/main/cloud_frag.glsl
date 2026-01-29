void setCloudFrag() {
    vec4 cloudColor = uColor;
    
    float heightFactor = texCoord.y;
    float gradientStrength = 0.4;
    float gradientPower = 1.5;
    float gradient = mix(1.0 - gradientStrength, 1.0, pow(heightFactor, gradientPower));
    cloudColor.rgb *= gradient;
    
    float starBrightness = uStarBrightness;
    if(starBrightness > 0.01) {
        vec3 starCoord = normalize(worldPos);
        float star = 0.0;
        
        for(int i = 0; i < 3; i++) {
            vec3 offset = vec3(float(i) * 123.456, float(i) * 789.012, float(i) * 345.678);
            vec3 p = floor((starCoord + offset) * 50.0);
            float hash = fract(sin(dot(p, vec3(12.9898, 78.233, 45.164))) * 43758.5453);
            if(hash > 0.98) {
                float dist = length(fract((starCoord + offset) * 50.0) - 0.5);
                star = max(star, smoothstep(0.5, 0.3, dist) * hash);
            }
        }
        
        star *= smoothstep(0.3, 0.5, heightFactor);
        cloudColor.rgb += vec3(star) * starBrightness;
    }
    
    fragColor = cloudColor;
    
    float fogStart = uRenderDistance * 0.5;
    float fogEnd = uRenderDistance;
    float fogFactor = clamp((fragDistance - fogStart) / (fogEnd - fogStart), 0.0, 1.0);
    
    vec3 viewDir = normalize(worldPos - uCameraPos);
    vec3 dynamicFogColor = getColorFog(viewDir);
    
    fragColor = mix(fragColor, vec4(dynamicFogColor, fragColor.a), fogFactor);
}