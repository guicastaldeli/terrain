void setSkyboxFrag() {
    vec4 timeColor = uColor;

    if(shaderType == 2) {
        float heightFactor = texCoord.y;

        float gradientStrength = 0.4;
        float gradientPower = 1.5;
        float gradient = mix(1.0 - gradientStrength, 1.0, pow(heightFactor, gradientPower));
        timeColor.rgb *= gradient;

        float starBrightness = uStarBrightness;
        if(starBrightness > 0.01) {
            float rotationAngle = uTime * 0.01;
            vec3 rotatedCoord;
            rotatedCoord.x = worldPos.x * cos(rotationAngle) - worldPos.z * sin(rotationAngle);
            rotatedCoord.y = worldPos.y;
            rotatedCoord.z = worldPos.x * sin(rotationAngle) + worldPos.z * cos(rotationAngle);
            
            vec3 starCoord = normalize(rotatedCoord);
            float star = 0.0;

            for(int i = 0; i < 3; i++) {
                vec3 offset = vec3(float(i) * 123.456, float(i) * 789.012, float(i) * 345.678);
                vec3 p = floor((starCoord + offset) * 50.0);
                float hash = fract(sin(dot(p, vec3(12.9898, 78.233, 45.164))) * 43758.5453);
                
                if(hash > 0.98) {
                    vec2 starUV = ((starCoord.xy + offset.xy) * 50.0 - p.xy) - 0.5;
                    float starRotation = uTime * 0.1 + hash * 6.283;
                    
                    float sinRot = sin(starRotation);
                    float cosRot = cos(starRotation);
                    vec2 rotatedUV = vec2(
                        starUV.x * cosRot - starUV.y * sinRot,
                        starUV.x * sinRot + starUV.y * cosRot
                    );
                    
                    float starDepth = length(starCoord + offset);
                    float normalizedDepth = starDepth / 2.0;
                    
                    float baseSize = 0.4;
                    float sizeVariation = mix(0.2, 1.8, normalizedDepth);
                    float edgeThreshold = baseSize * sizeVariation;
                    
                    float edgeX = smoothstep(edgeThreshold, edgeThreshold - 0.1, abs(rotatedUV.x));
                    float edgeY = smoothstep(edgeThreshold, edgeThreshold - 0.1, abs(rotatedUV.y));
                    float squareShape = edgeX * edgeY;
                    
                    if(squareShape > 0.01) {
                        float dist = length(rotatedUV);
                        float starIntensity = squareShape * hash;
                        
                        float depthBrightness = 1.0 / (normalizedDepth + 1.0);
                        depthBrightness = clamp(depthBrightness, 0.1, 3.5);
                        starIntensity *= depthBrightness;
                        
                        float uniqueSeed = dot(p, vec3(127.1, 311.7, 74.7));
                        float phaseOffset = fract(sin(uniqueSeed) * 43758.5453) * 628.318;
                        float speedSeed = dot(p, vec3(269.5, 183.3, 421.9));
                        float twinkleSpeed = mix(1.0, 5.0, fract(sin(speedSeed) * 43758.5453));
                        
                        float twinkle = sin(uTime * twinkleSpeed + phaseOffset) * 0.3 + 0.7;
                    
                        starIntensity *= twinkle;
                        
                        star = max(star, starIntensity);
                    }
                }
            }

            star *= smoothstep(0.3, 0.5, heightFactor);
            timeColor.rgb += vec3(star) * starBrightness;
        }

        fragColor = timeColor;
        if(uCameraPos.y < uWaterLevel + 0.7) {
            float waterCutoff = 0.5;
            if(texCoord.y < waterCutoff) {
                vec4 waterColor = vec4(0.0, 0.1, 0.4, 0.4);
                fragColor.rgb = mix(fragColor.rgb, waterColor.rgb, waterColor.a);
            }
        }
    }
}