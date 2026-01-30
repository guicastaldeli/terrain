void setMeshTex() {
    if(hasTex > 0) {
        fragColor = texture(texSampler, texCoord);
        fragColor.a = 0.8;
    } else {
        fragColor = uColor;
    }
}