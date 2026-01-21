#define STB_IMAGE_WRITE_IMPLEMENTATION
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "stb_image_write.h"
#include "_noise/map_generator.h"

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

    float minHeight = 999999.0f;
    float maxHeight = -999999.0f;
    for(int z = 0; z < height; z++) {
        for(int x = 0; x < width; x++) {
            if(heightMap[x][z] < minHeight) minHeight = heightMap[x][z];
            if(heightMap[x][z] > maxHeight) maxHeight = heightMap[x][z];
        }
    }
    
    printf("DEBUG: Height range for colors: min=%.2f, max=%.2f\n", minHeight, maxHeight);

    /**
     * 
     * 
     *  - IMPORTANT!!!: This is only temporary colors for the
     *               temporary noise, i will set the real map noise later
     *               for now this is the mountain noise colors to test
     *               things on the game.
     * 
     * 
     */
    for(int z = 0; z < height; z++) {
        for(int x = 0; x < width; x++) {
            int colorIdx = (z * width + x) * 4;
            float heightVal = heightMap[x][z];
            (*colors)[colorIdx + 3] = 1.0f;
            
            float centerX = width / 2.0f;
            float centerZ = height / 2.0f;
            float dx = x - centerX;
            float dz = z - centerZ;
            float distFromCenter = sqrtf(dx * dx + dz * dz);
            float normalizedDist = distFromCenter / (width * 0.5f);
            
            float normalizedHeight = (heightVal - minHeight) / (maxHeight - minHeight);
            
            if(normalizedHeight < 0.1f) {
                //Blue
                (*colors)[colorIdx] = 0.0f;
                (*colors)[colorIdx + 1] = 0.1f;
                (*colors)[colorIdx + 2] = 0.4f;
            } else if(normalizedHeight < 0.2f) { 
                // Green
                float noise = fractualSimplexNoise(x * 0.05f, z * 0.05f, 3, 0.4f, 2.0f);
                float baseGreen = 0.7f + noise * 0.15f;
                float redTint = 0.3f + noise * 0.1f;
                
                (*colors)[colorIdx] = redTint;
                (*colors)[colorIdx + 1] = baseGreen;
                (*colors)[colorIdx + 2] = 0.3f + noise * 0.1f;
            } else if(normalizedHeight < 0.4f) { 
                // Mountain Gray
                float noise = fractualSimplexNoise(x * 0.1f, z * 0.1f, 2, 0.3f, 2.0f) * 0.15f;
                float mountainBlend = (normalizedHeight - 0.2f) / 0.2f;
                float gray = 0.35f + mountainBlend * 0.25f + noise;
                
                (*colors)[colorIdx] = gray;
                (*colors)[colorIdx + 1] = gray;
                (*colors)[colorIdx + 2] = gray;
            } else { 
                // Snow
                float snowBlend = (normalizedHeight - 0.4f) / 0.6f;
                float baseGray = 0.6f;
                float smoothBlend = snowBlend * snowBlend * (3.0f - 2.0f * snowBlend);
                float color = baseGray + (1.0f - baseGray) * smoothBlend;
                
                float snowNoise = fractualSimplexNoise(x * 0.08f, z * 0.08f, 3, 0.3f, 2.0f) * 0.08f;
                color += snowNoise;
                
                if(color > 1.0f) color = 1.0f;
                if(color < baseGray) color = baseGray;
                
                (*colors)[colorIdx] = color;
                (*colors)[colorIdx + 1] = color;
                (*colors)[colorIdx + 2] = color;
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

JNIEXPORT jboolean JNICALL Java_main_com_app_root_env_NoiseGeneratorWrapper_generateMap(
    JNIEnv *env, 
    jobject obj, 
    jstring outputPath, 
    jlong seed,
    jint worldSize
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
    generatePoints(&collection, worldSize, 15);
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
    
    float** heightMap = malloc(worldSize * sizeof(float*));
    for(int i = 0; i < worldSize; i++) {
        heightMap[i] = malloc(worldSize * sizeof(float));
        for(int j = 0; j < worldSize; j++) {
            heightMap[i][j] = generateHeightMap(
                i, j, 
                worldSize,
                &collection
            );
        }
    }
    
    saveAsImage(heightMap, worldSize, worldSize, "map.png");
    
    float* vertices;
    int* indices;
    float* normals;
    float* colors;
    int vCount;
    int iCount;
    generateMapMeshData(
        heightMap, 
        worldSize, 
        worldSize, 
        &vertices, 
        &indices, 
        &normals, 
        &colors, 
        &vCount, 
        &iCount
    );
    mapWidth = worldSize;
    mapHeight = worldSize;
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
    
    if(strlen(path) > 0) generateMap(worldSize, path);
    freePointCollection(&collection);
    (*env)->ReleaseStringUTFChars(env, outputPath, path);
    
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_main_com_app_root_env_NoiseGeneratorWrapper_loadMapData(
    JNIEnv *env, 
    jobject obj, 
    jstring filePath
) {    
    const char *path = (*env)->GetStringUTFChars(env, filePath, 0);
    FILE* file = fopen(path, "rb");
    
    if(!file) {
        printf("Failed to open map file: %s\n", path);
        (*env)->ReleaseStringUTFChars(env, filePath, path);
        return JNI_FALSE;
    }

    unsigned long fileSeed;
    int chunksX, chunksZ;
    int storedPointCount;
    
    fread(&fileSeed, sizeof(unsigned long), 1, file);
    fread(&chunksX, sizeof(int), 1, file);
    fread(&chunksZ, sizeof(int), 1, file);
    fread(&storedPointCount, sizeof(int), 1, file);
    
    printf("Loading map: seed=%lu, chunks=%dx%d, points=%d\n", 
           fileSeed, chunksX, chunksZ, storedPointCount);
    
    fseek(file, storedPointCount * sizeof(Point), SEEK_CUR);
    
    int poissonCount;
    fread(&poissonCount, sizeof(int), 1, file);
    fseek(file, poissonCount * sizeof(PoissonPoint), SEEK_CUR);
    
    int width = chunksX * CHUNK_SIZE;
    int height = chunksZ * CHUNK_SIZE;
    
    if(heightMapData) free(heightMapData);
    if(indicesData) free(indicesData);
    if(normalsData) free(normalsData);
    if(colorsData) free(colorsData);
    if(pointData) free(pointData);
    heightMapData = malloc(width * height * sizeof(float));
    
    for(int cx = 0; cx < chunksX; cx++) {
        for(int cz = 0; cz < chunksZ; cz++) {
            float chunkHeights[CHUNK_SIZE][CHUNK_SIZE];
            fread(chunkHeights, sizeof(float), CHUNK_SIZE * CHUNK_SIZE, file);
            
            fseek(file, CHUNK_SIZE * CHUNK_SIZE * sizeof(unsigned char), SEEK_CUR);
            fseek(file, CHUNK_SIZE * CHUNK_SIZE * sizeof(int), SEEK_CUR);
            fseek(file, CHUNK_SIZE * CHUNK_SIZE * sizeof(unsigned char), SEEK_CUR);
            
            for(int x = 0; x < CHUNK_SIZE; x++) {
                for(int z = 0; z < CHUNK_SIZE; z++) {
                    int worldX = cx * CHUNK_SIZE + x;
                    int worldZ = cz * CHUNK_SIZE + z;
                    int index = worldX * height + worldZ;
                    heightMapData[index] = chunkHeights[x][z];
                }
            }
        }
    }
    
    mapWidth = width;
    mapHeight = height;
    vertexCount = width * height;
    indexCount = (width - 1) * (height - 1) * 6;
    
    printf("Loaded heightmap: %dx%d\n", mapWidth, mapHeight);
    
    float** heightMap = malloc(width * sizeof(float*));
    for(int i = 0; i < width; i++) {
        heightMap[i] = malloc(height * sizeof(float));
        for(int j = 0; j < height; j++) {
            heightMap[i][j] = heightMapData[i * width + j];
        }
    }
    
    float* vertices;
    int* indices;
    float* normals;
    float* colors;
    int vCount;
    int iCount;
    
    generateMapMeshData(
        heightMap, 
        width, 
        height, 
        &vertices, 
        &indices, 
        &normals, 
        &colors, 
        &vCount, 
        &iCount
    );
    
    indicesData = indices;
    normalsData = normals;
    colorsData = colors;
    
    for(int i = 0; i < width; i++) {
        free(heightMap[i]);
    }
    free(heightMap);
    
    fclose(file);
    (*env)->ReleaseStringUTFChars(env, filePath, path);
    
    printf("Successfully loaded map from file\n");
    return JNI_TRUE;
}

JNIEXPORT jfloatArray JNICALL Java_main_com_app_root_env_NoiseGeneratorWrapper_getHeightMapData(
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

JNIEXPORT jintArray JNICALL Java_main_com_app_root_env_NoiseGeneratorWrapper_getIndicesData(
    JNIEnv *env, 
    jobject obj
) {    
    if(!indicesData) return NULL;
    
    jintArray result = (*env)->NewIntArray(env, indexCount);
    (*env)->SetIntArrayRegion(env, result, 0, indexCount, indicesData);
    return result;
}

JNIEXPORT jfloatArray JNICALL Java_main_com_app_root_env_NoiseGeneratorWrapper_getNormalsData(
    JNIEnv *env, 
    jobject obj
) {    
    if(!normalsData) return NULL;
    
    jfloatArray result = (*env)->NewFloatArray(env, vertexCount * 3);
    (*env)->SetFloatArrayRegion(env, result, 0, vertexCount * 3, normalsData);
    return result;
}

JNIEXPORT jfloatArray JNICALL Java_main_com_app_root_env_NoiseGeneratorWrapper_getColorsData(
    JNIEnv *env, 
    jobject obj
) {    
    if(!colorsData) return NULL;
    
    jfloatArray result = (*env)->NewFloatArray(env, vertexCount * 4);
    (*env)->SetFloatArrayRegion(env, result, 0, vertexCount * 4, colorsData);
    return result;
}

JNIEXPORT jfloatArray JNICALL Java_main_com_app_root_env_NoiseGeneratorWrapper_getPointData(
    JNIEnv *env, 
    jobject obj
) {    
    if(!pointData) return NULL;
    
    jfloatArray result = (*env)->NewFloatArray(env, pointCount * 6);
    (*env)->SetFloatArrayRegion(env, result, 0, pointCount * 6, pointData);
    return result;
}

JNIEXPORT jint JNICALL Java_main_com_app_root_env_NoiseGeneratorWrapper_getMapWidth(
    JNIEnv *env, 
    jobject obj
) { 
    return mapWidth; 
}

JNIEXPORT jint JNICALL Java_main_com_app_root_env_NoiseGeneratorWrapper_getMapHeight(
    JNIEnv *env, 
    jobject obj
) { 
    return mapHeight; 
}

JNIEXPORT jint JNICALL Java_main_com_app_root_env_NoiseGeneratorWrapper_getVertexCount(
    JNIEnv *env, 
    jobject obj
) { 
    return vertexCount; 
}

JNIEXPORT jint JNICALL Java_main_com_app_root_env_NoiseGeneratorWrapper_getIndexCount(
    JNIEnv *env, 
    jobject obj
) { 
    return indexCount; 
}

JNIEXPORT jint JNICALL Java_main_com_app_root_env_NoiseGeneratorWrapper_getPointCount(
    JNIEnv *env, 
    jobject obj
) { 
    return pointCount; 
}