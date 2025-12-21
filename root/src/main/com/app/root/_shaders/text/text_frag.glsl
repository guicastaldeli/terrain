void setTextFrag() {
    float alpha = texture(texSampler, texCoord).a;
    fragColor = vec4(textColor, alpha);
    if(alpha < 0.1) discard;
};