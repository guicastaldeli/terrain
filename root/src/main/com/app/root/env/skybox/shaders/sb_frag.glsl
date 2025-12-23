void setSkyboxFrag() {
    vec4 timeColor = uColor;

    if(shaderType == 2) {
        float heigthFactor = texCoord.y;

        float gradientStrength = 0.4;
        float gradientPower = 1.5;
        float gradient = mix(1.0 - gradientStrength, 1.0, pow(heigthFactor, gradientPower));
        timeColor.rgb *= gradient;

        float starBrightness = uStarBrightness;
        if(starBrightness > 0.01) {
            vec3 starCoord = normalize(worldPos);
            float star = 0.0;

            for(int i = 0; i < 3; i++) {
                vec3 offset = vec3(float(i) * 123.456, float(i) * 789.012, float(i) * 345.678);
                vec3 p = floor((starCoord + offset) * 50.0);
                float hash = fract(sin(dot(p, vec3(12.9898, 78.233, 45.164))) * 43758.5453);
                if(hash > 0.98) {
                    float dist = length(fract((starCoord + offset) * 50.0) - 0.5);
                    star = max(star, smoothstep(0.5, 0.3, dist) * hash);
                }
            }

            star *= smoothstep(0.3, 0.5, heigthFactor);
            timeColor.rgb += vec3(star) * starBrightness;
        }

        fragColor = timeColor;
    }
}

/* TEST
void setSkyboxFrag() {
    if(shaderType == 2) {
        vec3 starCoord = normalize(worldPos);
        
        if(abs(starCoord.y) > 0.9) {
            vec3 grid = floor(starCoord * 20.0);
            if(mod(grid.x, 2.0) == 0.0 && mod(grid.z, 2.0) == 0.0) {
                fragColor = vec4(1.0, 1.0, 1.0, 1.0);
                return;
            }
        }
        
        float heightFactor = texCoord.y;
        float gradientStrength = 0.4;
        float gradientPower = 1.5;
        float gradient = mix(1.0 - gradientStrength, 1.0, pow(heightFactor, gradientPower));
        
        fragColor = uColor;
        fragColor.rgb *= gradient;
    }
}
*/