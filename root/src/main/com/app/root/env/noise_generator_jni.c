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
static PointCollection* globalCollection = NULL;

static int mapWidth = 0;
static int mapHeight = 0;
static int vertexCount = 0;
static int indexCount = 0;
static int pointCount = 0;

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

JNIEXPORT jboolean JNICALL Java_main_com_app_root_env_NoiseGeneratorWrapper_initNoise(
    JNIEnv *env, 
    jobject obj, 
    jlong seed,
    jint worldSize
) {
    initSystems((unsigned long)seed);
    
    if(globalCollection != NULL) {
        freePointCollection(globalCollection);
        free(globalCollection);
    }
    
    globalCollection = malloc(sizeof(PointCollection));
    initCollection(globalCollection, 50);
    generatePoints(globalCollection, worldSize, 15);
    
    printf("Noise system initialized with seed %lu, %d points generated\n", 
           (unsigned long)seed, globalCollection->count);
    
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_main_com_app_root_env_NoiseGeneratorWrapper_generateMap(
    JNIEnv *env, 
    jobject obj, 
    jstring outputPath, 
    jlong seed,
    jint worldSize,
    jint chunkSize
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
    
    
    
    heightMapData = malloc(mapWidth * mapHeight * sizeof(float));
    for(int i = 0; i < mapWidth; i++) {
        for(int j = 0; j < mapHeight; j++) {
            heightMapData[i * mapWidth + j] = heightMap[i][j];
        }
        free(heightMap[i]);
    }
    free(heightMap);
   
    
    if(strlen(path) > 0) generateMap(worldSize, chunkSize, path);
    freePointCollection(&collection);
    (*env)->ReleaseStringUTFChars(env, outputPath, path);
    
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_main_com_app_root_env_NoiseGeneratorWrapper_loadMapData(
    JNIEnv *env, 
    jobject obj, 
    jstring filePath,
    jint chunkSize
) {    
    const char *path = (*env)->GetStringUTFChars(env, filePath, 0);
    FILE* file = fopen(path, "rb");
    
    if(!file) {
        printf("Failed to open map file: %s\n", path);
        (*env)->ReleaseStringUTFChars(env, filePath, path);
        return JNI_FALSE;
    }

    unsigned long fileSeed;
    int chunksX; 
    int chunksZ;
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
    
    int width = chunksX * chunkSize;
    int height = chunksZ * chunkSize;
    
    if(heightMapData) free(heightMapData);
    if(indicesData) free(indicesData);
    if(normalsData) free(normalsData);
    if(colorsData) free(colorsData);
    if(pointData) free(pointData);
    heightMapData = malloc(width * height * sizeof(float));
    
    float* chunkHeights = malloc(chunkSize * chunkSize * sizeof(float));
    
    for(int cx = 0; cx < chunksX; cx++) {
        for(int cz = 0; cz < chunksZ; cz++) {
            fread(chunkHeights, sizeof(float), chunkSize * chunkSize, file);
            
            fseek(file, chunkSize * chunkSize * sizeof(unsigned char), SEEK_CUR);
            fseek(file, chunkSize * chunkSize * sizeof(int), SEEK_CUR);
            fseek(file, chunkSize * chunkSize * sizeof(unsigned char), SEEK_CUR);
            
            for(int x = 0; x < chunkSize; x++) {
                for(int z = 0; z < chunkSize; z++) {
                    int worldX = cx * chunkSize + x;
                    int worldZ = cz * chunkSize + z;
                    int index = worldX * height + worldZ;
                    heightMapData[index] = chunkHeights[x * chunkSize + z];
                }
            }
        }
    }
    
    free(chunkHeights);
    
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
    
    /*
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
    */
    
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

JNIEXPORT jfloatArray JNICALL Java_main_com_app_root_env_NoiseGeneratorWrapper_generateChunk(
    JNIEnv *env, 
    jobject obj, 
    jint chunkX, 
    jint chunkZ, 
    jint chunkSize,
    jint worldSize
) {
    if(globalCollection == NULL) {
        printf("ERROR: Noise system not initialized! Call initNoise first.\n");
        return NULL;
    }
    
    float* chunkData = malloc(chunkSize * chunkSize * sizeof(float));
    
    int worldStartX = chunkX * chunkSize;
    int worldStartZ = chunkZ * chunkSize;
    
    for(int x = 0; x < chunkSize; x++) {
        for(int z = 0; z < chunkSize; z++) {
            float worldX = worldStartX + x;
            float worldZ = worldStartZ + z;
            
            chunkData[x * chunkSize + z] = generateHeightMap(
                worldX, worldZ, 
                worldSize, 
                globalCollection
            );
        }
    }
    
    jfloatArray result = (*env)->NewFloatArray(env, chunkSize * chunkSize);
    (*env)->SetFloatArrayRegion(env, result, 0, chunkSize * chunkSize, chunkData);
    free(chunkData);
    
    return result;
}

JNIEXPORT jfloat JNICALL Java_main_com_app_root_env_NoiseGeneratorWrapper_getHeightAt(
    JNIEnv *env, 
    jobject obj, 
    jfloat worldX,
    jfloat worldZ,
    jint worldSize
) {
    if(globalCollection == NULL) {
        printf("ERROR: Noise system not initialized! Call initNoise first.\n");
        return 0.0f;
    }
    return generateHeightMap(worldX, worldZ, worldSize, globalCollection);
}

JNIEXPORT jboolean JNICALL Java_main_com_app_root_env_NoiseGeneratorWrapper_generateMapMetadata(
    JNIEnv *env, 
    jobject obj, 
    jstring outputPath, 
    jlong seed, 
    jint worldSize,
    jint chunkSize
) {
    const char *path = (*env)->GetStringUTFChars(env, outputPath, NULL);
   // initSystems((unsigned long) seed);

    FILE *file = fopen(path, "wb");
    if(file) {
        fwrite(&seed, sizeof(long), 1, file);
        fwrite(&worldSize, sizeof(int), 1, file);
        fwrite(&chunkSize, sizeof(int), 1, file);
        fclose(file);
        (*env)->ReleaseStringUTFChars(env, outputPath, path);
        return JNI_TRUE;
    }

    (*env)->ReleaseStringUTFChars(env, outputPath, path);
    return JNI_FALSE;
}

JNIEXPORT jfloat JNICALL Java_main_com_app_root_env_NoiseGeneratorWrapper_fractualSimplexNoise(
    JNIEnv *env, 
    jobject obj, 
    jfloat x,
    jfloat y,
    jint octaves,
    jfloat persistence,
    jfloat lacunarity
) {
    return fractualSimplexNoise(x, y, octaves, persistence, lacunarity);
}