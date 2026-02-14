void setMeshColor() {
    vec4 worldPosition = model * vec4(inPos, 1.0);
    gl_Position = projection * view * worldPosition;
    worldPos = worldPosition.xyz;
    uColor = hasColors > 0 ? aColor : vec4(1.0, 1.0, 1.0, 1.0);
    texCoord = aTexCoord;
}