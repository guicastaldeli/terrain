void setUIFrag() {
    if(hasTex > 0) {
        vec4 texColor = texture(texSampler, texCoord);
        fragColor = texColor * uColor;
    } else {
        fragColor = uColor;
    }
}