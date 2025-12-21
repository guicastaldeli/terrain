#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "noise/map_generator.h"

#define MAP_SIZE 256
#define CHUNK_SIZE 16

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

    for(int z = 0; z < height; z++) {
        for(int x = 0; x < width; x++) {
            int idx = (z * width + x) * 3;
            (*vertices)[idx] = (float)x - width / 2.0f;
            (*vertices)[idx+1] = heightMap[x][z] * 0.1f;
            (*vertices)[idx+2] = (float)z - height / 2.0f;

            int colorIdx = (z * width + x) * 4;
            float heightVal = heightMap[x][z] * 0.1f;
            (*colors)[colorIdx] = 0.2f;
            (*colors)[colorIdx + 1] = 0.7f;
            (*colors)[colorIdx + 2] = 0.2f;
            /* TO DO later...
            if(heightVal < 0.4f) {
                (*colors)[colorIdx] = 0.9f;
                (*colors)[colorIdx + 1] = 0.8f;
                (*colors)[colorIdx + 2] = 0.2f;
            } else if(heightVal < 0.7f) {
                (*colors)[colorIdx] = 0.2f;
                (*colors)[colorIdx + 1] = 0.7f;
                (*colors)[colorIdx + 2] = 0.2f;
            } else {
                (*colors)[colorIdx] = 0.5f;
                (*colors)[colorIdx + 1] = 0.5f;
                (*colors)[colorIdx + 2] = 0.5f;
            }
                */
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

    /* see this later too... xD
    calculateNormals(
        *vertices,
        *indices,
        *indexCount,
        *normals,
        *vertexCount
    );
    */
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

JNIEXPORT jboolean JNICALL Java_main_com_app_root_env_map_noise_MapGeneratorWrapper_generateMap(
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
    generatePoints(&collection, MAP_SIZE, 15);
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
    
    float** heightMap = malloc(MAP_SIZE * sizeof(float*));
    for(int i = 0; i < MAP_SIZE; i++) {
        heightMap[i] = malloc(MAP_SIZE * sizeof(float));
        for(int j = 0; j < MAP_SIZE; j++) {
            heightMap[i][j] = generateEnhancedHeight(i, j, &collection);
        }
    }
    
    simulateHydraulicErosion(heightMap, MAP_SIZE, 1000, 2);
    thermalErosion(heightMap, MAP_SIZE, 0.1f, 5);
    
    float* vertices;
    int* indices;
    float* normals;
    float* colors;
    int vCount;
    int iCount;
    generateTerrainMesh(
        heightMap, 
        MAP_SIZE, 
        MAP_SIZE, 
        &vertices, 
        &indices, 
        &normals, 
        &colors, 
        &vCount, 
        &iCount
    );
    mapWidth = MAP_SIZE;
    mapHeight = MAP_SIZE;
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

JNIEXPORT jboolean JNICALL Java_main_com_app_root_env_map_noise_MapGeneratorWrapper_loadTerrainData(
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

JNIEXPORT jfloatArray JNICALL Java_main_com_app_root_env_map_noise_MapGeneratorWrapper_getHeightMapData(
    JNIEnv *env, jobject obj) {
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

JNIEXPORT jintArray JNICALL Java_main_com_app_root_env_map_noise_MapGeneratorWrapper_getIndicesData(
    JNIEnv *env, 
    jobject obj
) {    
    if(!indicesData) return NULL;
    
    jintArray result = (*env)->NewIntArray(env, indexCount);
    (*env)->SetIntArrayRegion(env, result, 0, indexCount, indicesData);
    return result;
}

JNIEXPORT jfloatArray JNICALL Java_main_com_app_root_env_map_noise_MapGeneratorWrapper_getNormalsData(
    JNIEnv *env, 
    jobject obj
) {    
    if(!normalsData) return NULL;
    
    jfloatArray result = (*env)->NewFloatArray(env, vertexCount * 3);
    (*env)->SetFloatArrayRegion(env, result, 0, vertexCount * 3, normalsData);
    return result;
}

JNIEXPORT jfloatArray JNICALL Java_main_com_app_root_env_map_noise_MapGeneratorWrapper_getColorsData(
    JNIEnv *env, 
    jobject obj
) {    
    if(!colorsData) return NULL;
    
    jfloatArray result = (*env)->NewFloatArray(env, vertexCount * 4);
    (*env)->SetFloatArrayRegion(env, result, 0, vertexCount * 4, colorsData);
    return result;
}

JNIEXPORT jfloatArray JNICALL Java_main_com_app_root_env_map_noise_MapGeneratorWrapper_getPointData(
    JNIEnv *env, 
    jobject obj
) {    
    if(!pointData) return NULL;
    
    jfloatArray result = (*env)->NewFloatArray(env, pointCount * 6);
    (*env)->SetFloatArrayRegion(env, result, 0, pointCount * 6, pointData);
    return result;
}

JNIEXPORT jint JNICALL Java_main_com_app_root_env_map_noise_MapGeneratorWrapper_getMapWidth(
    JNIEnv *env, 
    jobject obj
) { 
    return mapWidth; 
}

JNIEXPORT jint JNICALL Java_main_com_app_root_env_map_noise_MapGeneratorWrapper_getMapHeight(
    JNIEnv *env, 
    jobject obj
) { 
    return mapHeight; 
}

JNIEXPORT jint JNICALL Java_main_com_app_root_env_map_noise_MapGeneratorWrapper_getVertexCount(
    JNIEnv *env, 
    jobject obj
) { 
    return vertexCount; 
}

JNIEXPORT jint JNICALL Java_main_com_app_root_env_map_noise_MapGeneratorWrapper_getIndexCount(
    JNIEnv *env, 
    jobject obj
) { 
    return indexCount; 
}

JNIEXPORT jint JNICALL Java_main_com_app_root_env_map_noise_MapGeneratorWrapper_getPointCount(
    JNIEnv *env, 
    jobject obj
) { 
    return pointCount; 
}