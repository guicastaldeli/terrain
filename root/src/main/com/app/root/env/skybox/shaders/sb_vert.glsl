void setSkyboxVert() {
    mat4 skyboxView = mat4(mat3(view));
    gl_Position = projection * skyboxView * model * vec4(inPos, 1.0);
    uColor = aColor;
}