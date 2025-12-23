void setSkyboxFrag() {
    vec4 timeColor = uColor;
    float timeFactor = 0.0f;
    float gradientY = (gl_FragCoord.y / screenSize.y);
    if(shaderType == 2) {
        timeColor.rgb *= (0.8 + gradientY * 0.2);
        fragColor = timeColor;
    }
}