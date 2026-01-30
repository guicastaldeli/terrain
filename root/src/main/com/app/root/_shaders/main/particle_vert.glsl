void setParticleVert() {
    vec3 cameraRight = vec3(
        view[0][0],
        view[1][0],
        view[2][0]
    );
    vec3 cameraUp = vec3(
        view[0][1],
        view[1][1],
        view[2][1]
    );

    vec3 finalPos = inPos;
    
    vec3 particleCenter = vec3(model[3][0], model[3][1], model[3][2]);
        
    vec3 scaledVertex = finalPos * vec3(model[0][0], model[1][1], model[2][2]);

    finalPos = (cameraRight * scaledVertex.x) + (cameraUp * scaledVertex.y);
    finalPos += particleCenter;

    vec4 worldPosition = vec4(finalPos, 1.0);
    worldPos = worldPosition.xyz;

    uColor = aColor;
    texCoord = aTexCoord;

    vec4 viewPos = view * worldPosition;
    fragDistance = length(viewPos.xyz);
    gl_Position = projection * viewPos;
}