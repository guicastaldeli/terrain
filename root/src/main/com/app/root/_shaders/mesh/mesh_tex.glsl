void setMeshTex() {
    if(hasTex > 0) {
        fragColor = texture(texSampler, texCoord);
        // DEBUG: Force 50% transparency for all textures
        fragColor.a = 0.8;
    } else {
        fragColor = uColor;
    }
}