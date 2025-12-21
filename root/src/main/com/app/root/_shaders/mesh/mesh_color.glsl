void setMeshColor() {
    gl_Position = projection * view * model * vec4(inPos, 1.0);
    uColor = hasColors > 0 ? aColor : vec4(1.0, 1.0, 1.0, 1.0);
    texCoord = aTexCoord;
    
    //fragColor = vec4(texCoord, 0.0, 1.0);
}