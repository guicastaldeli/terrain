void setCloudFrag() {
    vec3 skyColor = uFogColor;
    
    vec3 cloudColor = mix(skyColor, uColor.rgb, 0.3);
    
    float heightGradient = texCoord.y;
    cloudColor *= mix(1.2, 0.8, heightGradient);
    
    float noise = fract(sin(dot(texCoord.xy, vec2(12.9898, 78.233))) * 43758.5453);
    cloudColor += noise * 0.1;
    
    fragColor = vec4(cloudColor, 0.4);
}