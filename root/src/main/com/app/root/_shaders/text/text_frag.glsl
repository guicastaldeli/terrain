void setTextFrag() {
    float alpha = texture(texSampler, texCoord).a;
    fragColor = vec4(uColor.rgb, alpha * uColor.a);
    if(alpha < 0.1) discard;
}