void setCloudVert(vec3 finalPos, vec3 finalNormal) {
    vec4 worldPosition;
    
    if(isInstanced == 1) {
        worldPosition = vec4(finalPos, 1.0);
        worldPos = finalPos;
        normal = normalize(finalNormal);
    } else {
        worldPosition = model * vec4(finalPos, 1.0);
        worldPos = worldPosition.xyz;
        
        mat3 normalMatrix = mat3(transpose(inverse(model)));
        normal = normalize(normalMatrix * finalNormal);
    }
    
    uColor = hasColors > 0 ? aColor : vec4(1.0, 1.0, 1.0, 1.0);
    
    float normalizedY = (finalPos.y + 1000.0) / 2000.0;
    texCoord = vec2(0.0, normalizedY);
    
    vec4 viewPos = view * worldPosition;
    fragDistance = length(viewPos.xyz);
    gl_Position = projection * viewPos;
}