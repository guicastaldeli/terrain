void setMeshTex() {
    if(hasWorldTex > 0) {
        vec4 color = vec4(0.0);
        
        color += texture(uTexSand, texCoord) * vTexBlend.y;
        color += texture(uTexGrass, texCoord) * vTexBlend.z;
        color += texture(uTexRock, texCoord) * vTexBlend.w;
        color += texture(uTexSnow, texCoord) * vTexBlend4;
        
        fragColor = color;
    } else if(hasTex > 0) {
        fragColor = texture(texSampler, texCoord);
        fragColor.a = 0.8;
    } else {
        fragColor = uColor;
    }
}