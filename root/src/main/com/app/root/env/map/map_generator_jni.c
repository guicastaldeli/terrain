#define STB_IMAGE_WRITE_IMPLEMENTATION
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "stb_image_write.h"
#include "noise/map_generator.h"

static float* heightMapData = NULL;
static int* indicesData = NULL;
static float* normalsData = NULL;
static float* colorsData = NULL;
static float* pointData = NULL;

static int mapWidth = 0;
static int mapHeight = 0;
static int vertexCount = 0;
static int indexCount = 0;
static int pointCount = 0;

/**
 * Generate Map Mesh Data
 */
void generateMapMeshData(
    float** heightMap,
    int width,
    int height,
    float** vertices,
    int** indices,
    float** normals,
    float** colors,
    int* vertexCount,
    int* indexCount
) {
    *vertexCount = width * height;
    *indexCount = (width - 1) * (height - 1) * 6;

    *vertices = malloc(*vertexCount * 3 * sizeof(float));
    *indices = malloc(*indexCount * sizeof(int));
    *normals = malloc(*vertexCount * 3 * sizeof(float));
    *colors = malloc(*vertexCount * 4 * sizeof(float));

    // First, find the actual min and max heights
    float minHeight = 999999.0f;
    float maxHeight = -999999.0f;
    for(int z = 0; z < height; z++) {
        for(int x = 0; x < width; x++) {
            if(heightMap[x][z] < minHeight) minHeight = heightMap[x][z];
            if(heightMap[x][z] > maxHeight) maxHeight = heightMap[x][z];
        }
    }
    
    printf("DEBUG: Height range for colors: min=%.2f, max=%.2f\n", minHeight, maxHeight);

    for(int z = 0; z < height; z++) {
        for(int x = 0; x < width; x++) {
            int idx = (z * width + x) * 3;
            (*vertices)[idx] = (float)x - width / 2.0f;
            (*vertices)[idx+1] = heightMap[x][z] * 2.0f;
            (*vertices)[idx+2] = (float)z - height / 2.0f;

            int colorIdx = (z * width + x) * 4;
            float heightVal = heightMap[x][z];
            (*colors)[colorIdx + 3] = 1.0f;
            
            if(heightVal < -200.0f) {
                (*colors)[colorIdx] = 0.0f;
                (*colors)[colorIdx + 1] = 0.1f;
                (*colors)[colorIdx + 2] = 0.4f;
            } else if(heightVal < -100.0f) {
                (*colors)[colorIdx] = 0.0f;
                (*colors)[colorIdx + 1] = 0.2f;
                (*colors)[colorIdx + 2] = 0.5f;
            } else if(heightVal < -50.0f) {
                (*colors)[colorIdx] = 0.0f;
                (*colors)[colorIdx + 1] = 0.3f;
                (*colors)[colorIdx + 2] = 0.6f;
            } else if(heightVal < -20.0f) {
                (*colors)[colorIdx] = 0.0f;
                (*colors)[colorIdx + 1] = 0.4f;
                (*colors)[colorIdx + 2] = 0.7f;
            } else if(heightVal < 0.0f) {
                float t = (heightVal + 20.0f) / 20.0f;
                (*colors)[colorIdx] = t * 0.9f;
                (*colors)[colorIdx + 1] = 0.4f + t * 0.4f;
                (*colors)[colorIdx + 2] = 0.7f - t * 0.5f;
            } else if(heightVal < 5.0f) {
                (*colors)[colorIdx] = 0.9f;
                (*colors)[colorIdx + 1] = 0.8f;
                (*colors)[colorIdx + 2] = 0.2f;
            } else if(heightVal < 20.0f) {
                (*colors)[colorIdx] = 0.3f;
                (*colors)[colorIdx + 1] = 0.7f;
                (*colors)[colorIdx + 2] = 0.3f;
            } else if(heightVal < 50.0f) {
                (*colors)[colorIdx] = 0.2f;
                (*colors)[colorIdx + 1] = 0.6f;
                (*colors)[colorIdx + 2] = 0.2f;
            } else if(heightVal < 80.0f) {
                (*colors)[colorIdx] = 0.1f;
                (*colors)[colorIdx + 1] = 0.5f;
                (*colors)[colorIdx + 2] = 0.1f;
            } else if(heightVal < 100.0f) {
                (*colors)[colorIdx] = 0.5f;
                (*colors)[colorIdx + 1] = 0.5f;
                (*colors)[colorIdx + 2] = 0.5f;
            } else {
                (*colors)[colorIdx] = 1.0f;
                (*colors)[colorIdx + 1] = 1.0f;
                (*colors)[colorIdx + 2] = 1.0f;
            }
        }
    }

    int indicesIdx = 0;
    for(int z = 0; z < height - 1; z++) {
        for(int x = 0; x < width - 1; x++) {
            int topLeft = z * width + x;
            int topRight = topLeft + 1;
            int bottomLeft = (z + 1) * width + x;
            int bottomRight = bottomLeft + 1;

            (*indices)[indicesIdx++] = topLeft;
            (*indices)[indicesIdx++] = bottomLeft;
            (*indices)[indicesIdx++] = topRight;

            (*indices)[indicesIdx++] = topRight;
            (*indices)[indicesIdx++] = bottomLeft;
            (*indices)[indicesIdx++] = bottomRight;
        }
    }
}

/**
 * Generate Vertex Positions
 */
void generateVertexPositions(float** heightMap, int width, int height, float** vertices) {
    *vertices = malloc(width * height * 3 * sizeof(float));
    for(int z = 0; z < height; z++) {
        for(int x = 0; x < width; x++) {
            int i = (z * width + x) * 3;
            (*vertices)[i] = (float)x - width / 2.0f;
            (*vertices)[i+1] = heightMap[x][z] * 1.0f;
            (*vertices)[i+2] = (float)z - height / 2.0f;
        }
    }
}

/**
 * Calculate Normals
 */
void calculateNormals(
    float* vertices,
    int* indices,
    int indexCount,
    float* normals,
    int vertexCount
) {
    for(int i = 0; i < vertexCount * 3; i++) {
        normals[i] = 0.0f;
    }

    for(int i = 0; i < indexCount; i += 3) {
        int idx1 = indices[i] * 3;
        int idx2 = indices[i+1] * 3;
        int idx3 = indices[i+2] * 3;

        float v1x = vertices[idx2] - vertices[idx1];
        float v1y = vertices[idx2+1] - vertices[idx1+1];
        float v1z = vertices[idx2+2] - vertices[idx1+2];

        float v2x = vertices[idx3] - vertices[idx1];
        float v2y = vertices[idx3+1] - vertices[idx1+1];
        float v2z = vertices[idx3+2] - vertices[idx1+2];

        float nx = v1y * v2z - v1z * v2y;
        float ny = v1z * v2x - v1x * v2z;
        float nz = v1x * v2y - v1y * v2x;

        float len = sqrt(nx * nx + ny * ny + nz * nz);
        if(len > 0) {
            nx /= len;
            ny /= len;
            nz /= len;
        }

        normals[idx1] += nx;
        normals[idx1+1] += ny;
        normals[idx1+2] += nz;

        normals[idx2] += nx;
        normals[idx2+1] += ny;
        normals[idx2+2] += nz;

        normals[idx3] += nx;
        normals[idx3+1] += ny;
        normals[idx3+2] += nz;
    }
    for(int i = 0; i < vertexCount; i++) {
        int idx = i * 3;
        float len = sqrt(
            normals[idx] * normals[idx] +
            normals[idx+1] * normals[idx+1] +
            normals[idx+2] * normals[idx+2]
        );
        if(len > 0) {
            normals[idx] /= len;
            normals[idx+1] /= len;
            normals[idx+2] /= len;
        }
    }
}

/**
 * Save as Image
 */
void saveAsImage(
    float** heightMap,
    int width,
    int height,
    const char* fileName
) {
    unsigned char* pixels = malloc(width * height * 3);

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

JNIEXPORT jboolean JNICALL Java_main_com_app_root_env_map_MapGeneratorWrapper_generateMap(
    JNIEnv *env, 
    jobject obj, 
    jstring outputPath, 
    jlong seed
) {    
    const char *path = (*env)->GetStringUTFChars(env, outputPath, 0); 

    if(heightMapData) free(heightMapData);
    if(indicesData) free(indicesData);
    if(normalsData) free(normalsData);
    if(colorsData) free(colorsData);
    if(pointData) free(pointData);
    initSystems((unsigned long)seed);
    
    PointCollection collection;
    initCollection(&collection, 50);
    generatePoints(&collection, WORLD_SIZE, 15);
    pointCount = collection.count;
    
    pointData = malloc(pointCount * 6 * sizeof(float));
    for(int i = 0; i < pointCount; i++) {
        int idx = i * 6;
        pointData[idx] = collection.points[i].centerX;
        pointData[idx + 1] = collection.points[i].centerZ;
        pointData[idx + 2] = collection.points[i].radius;
        pointData[idx + 3] = collection.points[i].heightScale;
        pointData[idx + 4] = collection.points[i].ruggedness;
        pointData[idx + 5] = collection.points[i].elevation;
    }
    
    float** heightMap = malloc(WORLD_SIZE * sizeof(float*));
    for(int i = 0; i < WORLD_SIZE; i++) {
        heightMap[i] = malloc(WORLD_SIZE * sizeof(float));
        for(int j = 0; j < WORLD_SIZE; j++) {
            heightMap[i][j] = generateHeightMap(i, j, &collection);
        }
    }
    
    saveAsImage(heightMap, WORLD_SIZE, WORLD_SIZE, "map.png");
    
    float* vertices;
    int* indices;
    float* normals;
    float* colors;
    int vCount;
    int iCount;
    generateMapMeshData(
        heightMap, 
        WORLD_SIZE, 
        WORLD_SIZE, 
        &vertices, 
        &indices, 
        &normals, 
        &colors, 
        &vCount, 
        &iCount
    );
    mapWidth = WORLD_SIZE;
    mapHeight = WORLD_SIZE;
    vertexCount = vCount;
    indexCount = iCount;
    
    heightMapData = malloc(mapWidth * mapHeight * sizeof(float));
    for(int i = 0; i < mapWidth; i++) {
        for(int j = 0; j < mapHeight; j++) {
            heightMapData[i * mapWidth + j] = heightMap[i][j];
        }
        free(heightMap[i]);
    }
    free(heightMap);
    
    indicesData = indices;
    normalsData = normals;
    colorsData = colors;
    
    if(strlen(path) > 0) generateMap(path);
    freePointCollection(&collection);
    (*env)->ReleaseStringUTFChars(env, outputPath, path);
    
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_main_com_app_root_env_map_MapGeneratorWrapper_loadMapData(
    JNIEnv *env, 
    jobject obj, 
    jstring filePath
) {    
    const char *path = (*env)->GetStringUTFChars(env, filePath, 0);
    FILE* file = fopen(path, "rb");
    
    if(!file) {
        (*env)->ReleaseStringUTFChars(env, filePath, path);
        return JNI_FALSE;
    }

    // implement the full file loader (file header and data)
    // HERE... later
    
    fclose(file);
    (*env)->ReleaseStringUTFChars(env, filePath, path);
    
    return JNI_TRUE;
}

JNIEXPORT jfloatArray JNICALL Java_main_com_app_root_env_map_MapGeneratorWrapper_getHeightMapData(
    JNIEnv *env, 
    jobject obj
) {
    if(!heightMapData) return NULL;
    
    jfloatArray result = (*env)->NewFloatArray(env, mapWidth * mapHeight);
    (*env)->SetFloatArrayRegion(
        env, 
        result, 
        0, 
        mapWidth * mapHeight, 
        heightMapData
    );
    return result;
}

JNIEXPORT jintArray JNICALL Java_main_com_app_root_env_map_MapGeneratorWrapper_getIndicesData(
    JNIEnv *env, 
    jobject obj
) {    
    if(!indicesData) return NULL;
    
    jintArray result = (*env)->NewIntArray(env, indexCount);
    (*env)->SetIntArrayRegion(env, result, 0, indexCount, indicesData);
    return result;
}

JNIEXPORT jfloatArray JNICALL Java_main_com_app_root_env_map_MapGeneratorWrapper_getNormalsData(
    JNIEnv *env, 
    jobject obj
) {    
    if(!normalsData) return NULL;
    
    jfloatArray result = (*env)->NewFloatArray(env, vertexCount * 3);
    (*env)->SetFloatArrayRegion(env, result, 0, vertexCount * 3, normalsData);
    return result;
}

JNIEXPORT jfloatArray JNICALL Java_main_com_app_root_env_map_MapGeneratorWrapper_getColorsData(
    JNIEnv *env, 
    jobject obj
) {    
    if(!colorsData) return NULL;
    
    jfloatArray result = (*env)->NewFloatArray(env, vertexCount * 4);
    (*env)->SetFloatArrayRegion(env, result, 0, vertexCount * 4, colorsData);
    return result;
}

JNIEXPORT jfloatArray JNICALL Java_main_com_app_root_env_map_MapGeneratorWrapper_getPointData(
    JNIEnv *env, 
    jobject obj
) {    
    if(!pointData) return NULL;
    
    jfloatArray result = (*env)->NewFloatArray(env, pointCount * 6);
    (*env)->SetFloatArrayRegion(env, result, 0, pointCount * 6, pointData);
    return result;
}

JNIEXPORT jint JNICALL Java_main_com_app_root_env_map_MapGeneratorWrapper_getMapWidth(
    JNIEnv *env, 
    jobject obj
) { 
    return mapWidth; 
}

JNIEXPORT jint JNICALL Java_main_com_app_root_env_map_MapGeneratorWrapper_getMapHeight(
    JNIEnv *env, 
    jobject obj
) { 
    return mapHeight; 
}

JNIEXPORT jint JNICALL Java_main_com_app_root_env_map_MapGeneratorWrapper_getVertexCount(
    JNIEnv *env, 
    jobject obj
) { 
    return vertexCount; 
}

JNIEXPORT jint JNICALL Java_main_com_app_root_env_map_MapGeneratorWrapper_getIndexCount(
    JNIEnv *env, 
    jobject obj
) { 
    return indexCount; 
}

JNIEXPORT jint JNICALL Java_main_com_app_root_env_map_MapGeneratorWrapper_getPointCount(
    JNIEnv *env, 
    jobject obj
) { 
    return pointCount; 
}