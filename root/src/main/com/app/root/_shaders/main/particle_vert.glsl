void setParticleVert() {
    vec3 cameraRight = vec3(
        view[0][0],
        view[1][0],
        view[2][0],
    );
    vec3 cameraUp = vec3(
        view[0][1],
        view[1][1],
        view[2][1],
    );

    vec3 finalPos = inPos;

    if(isInstanced == 1) {
        finalPos *= instanceScale;
        finalPos = (cameraRight * finalPos.x) + (cameraUp * finalPos.y);
        finalPos += instancePosition;
    } else {
        finalPos = (cameraRight * finalPos.x) + (cameraUp * finalPos.y);
        finalPos += position;
    }

    vec3 worldPosition = vec4(finalPos, 1.0);
    worldPos = worldPosition.xyz;

    uColor = hasColors > 0 ? aColor : vec4(1.0, 1.0, 1.0, 1.0);
    texCoord = aTexCoord;

    vec4 viewPos = view * worldPosition;
    fragDistance = length(viewPos.xyz);
    gl_Position = projection * viewPos;
}