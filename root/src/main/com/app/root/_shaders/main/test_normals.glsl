/*
///
///
****** Basic Normals Test Shader :))
///
///
*/
void testNormals() {
    vec3 finalColor = fragColor.rgb;
    if(length(normal) < 0.001) {
        finalColor = vec3(1.0, 0.0, 0.0);
    } else {
        vec3 normalizedNormal = normalize(normal);
        finalColor = normalizedNormal * 0.5 + 0.5;
    }
        
    fragColor = vec4(finalColor, fragColor.a);
}