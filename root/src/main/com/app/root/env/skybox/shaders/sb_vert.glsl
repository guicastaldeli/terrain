void setSkyboxVert() {
    mat4 skyboxView = mat4(mat3(view));
    vec4 worldPosition = model * vec4(inPos, 1.0);
    
    gl_Position = projection * skyboxView * worldPosition;
    
    worldPos = vec3(worldPosition);

    float normalizedY = (inPos.y + 1000.0) / 2000.0;
    uColor = aColor;
    texCoord = vec2(0.0, normalizedY);
}