void setMeshTex() {
    if(hasTex > 0) {
        fragColor = texture(texSampler, texCoord);
    } else {
        fragColor = uColor;
    }
    //fragColor = vec4(texCoord, 0.0, 1.0);
}