#define STB_IMAGE_WRITE_IMPLEMENTATION
#include "img.h"
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "stb_image_write.h"


void save(
    float** heightMap,
    int width,
    int height,
    const char* fileName
) {
    unsigned char *pixels = malloc(width * height * 3);

    float minVal = 999999.0f;
    float maxVal = -999999.0f;
    for(int i = 0; i < width; i++) {
        for(int j = 0; j < height; j++) {
            if(heightMap[i][j] < minVal) minVal = heightMap[i][j];
            if(heightMap[i][j] > maxVal) maxVal = heightMap[i][j];
        }
    }
    
    printf("Height range: min=%.2f, max=%.2f, range=%.2f\n", 
           minVal, maxVal, maxVal - minVal);
    
    float range = maxVal - minVal;
    if(range < 0.001f) range = 1.0f;
    
    for(int y = 0; y < height; y++) {
        for(int x = 0; x < width; x++) {
            float normalized = (heightMap[x][y] - minVal) / range;
            unsigned char gray = (unsigned char)(normalized * 255);
            
            int idx = (y * width + x) * 3;
            pixels[idx] = gray;
            pixels[idx + 1] = gray;  
            pixels[idx + 2] = gray;
        }
    }
    
    stbi_write_png(fileName, width, height, 3, pixels, width * 3);
    free(pixels);
}