#include "map_generator.h"
#include "file_saver.h"
#include <math.h>

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

#define OCTAVES 8
#define PERSISTENCE 0.5
#define LACUNARITY 2.0

/**
 * Generate Height Map
 */
float generateHeightMap(
    float worldX,
    float worldZ,
    int worldSize,
    PointCollection* collection
) {
    /**
     * 
     * 
     *  - IMPORTANT!!!: This is only a temporary noise
     *               i will set the real map noise later
     *               for now this is the mountain noise to test
     *               things on the game.
     * 
     * 
     */
    float centerX = worldSize / 2.0f;
    float centerZ = worldSize / 2.0f;
    
    float dx = worldX - centerX;
    float dz = worldZ - centerZ;
    float distFromCenter = sqrtf(dx * dx + dz * dz);
    
    float maxRadius = worldSize * 0.45f;
    float normalizedDist = distFromCenter / maxRadius;
    
    float islandFalloff = 1.0f;
    if(normalizedDist > 0.7f) {
        float edgeFactor = (normalizedDist - 0.7f) / 0.3f;
        islandFalloff = 1.0f - (edgeFactor * edgeFactor);
        islandFalloff = fmaxf(0.0f, islandFalloff);
    }
    
    float baseHeight = 100.0f * (1.0f - normalizedDist * normalizedDist) * islandFalloff;
    float mountainHeight = 0.0f;
    float mountainRadius = worldSize * 0.15f;
    
    if(distFromCenter < mountainRadius) {
        float mountainNorm = distFromCenter / mountainRadius;
        mountainHeight = 400.0f * (1.0f - mountainNorm * mountainNorm);
    }
    
    float noiseInfluence = 1.0f;
    if(distFromCenter > mountainRadius) {
        float distanceFromMountain = distFromCenter - mountainRadius;
        float maxIslandRadius = maxRadius * 0.7f;
        noiseInfluence = 1.0f - (distanceFromMountain / (maxIslandRadius - mountainRadius));
        noiseInfluence = fmaxf(0.1f, fminf(1.0f, noiseInfluence));
    }
    
    float terrainNoise = fractualSimplexNoise(
        worldX * 0.01f,
        worldZ * 0.01f,
        4,
        0.5f,
        2.0f
    );
    float detailNoise = fractualSimplexNoise(
        worldX * 0.05f,
        worldZ * 0.05f,
        3,
        0.3f,
        2.0f
    );
    
    float height = baseHeight + mountainHeight;
    
    height += terrainNoise * 30.0f * islandFalloff * noiseInfluence;
    height += detailNoise * 10.0f * islandFalloff * noiseInfluence;
    
    height = fmaxf(height, 0.0f);
    return height;
}

/**
 * Generate Chunk
 */
void generateChunk(
    Chunk* chunk,
    PointCollection* collection,
    PoissonCollection* objLocations,
    int worldSize
) {
    int worldX = chunk->x * CHUNK_SIZE;
    int worldZ = chunk->z * CHUNK_SIZE;

    for(int x = 0; x < CHUNK_SIZE; x++) {
        for(int z = 0; z < CHUNK_SIZE; z++) {
            float globalX = worldX + x;
            float globalZ = worldZ + z;

            chunk->heightMap[x][z] = generateHeightMap(
                globalX, globalZ, 
                worldSize,
                collection
            );
            chunk->pointId[x][z] = 0;
            float bestMask = 0.0f;
            for(int i = 0; i < collection->count; i++) {
                float mask = pointMask(
                    globalX,
                    globalZ,
                    collection->points[i].centerX,
                    collection->points[i].centerZ,
                    collection->points[i].radius
                );
                if(mask > bestMask) {
                    bestMask = mask;
                    chunk->pointId[x][z] = i + 1;
                }
            }

            chunk->objMap[x][z] = 0;
            for(int i = 0; i < objLocations->count; i++) {
                float dx = globalX - objLocations->points[i].x;
                float dz = globalZ - objLocations->points[i].y;
                if(dx * dx + dz * dz < 4.0f) {
                    chunk->objMap[x][z] = 1;
                    break;
                }
            }
        }
    }
}

/**
 * Generate Map
 */
void generateMap(int worldSize, const char* fileName) {
    unsigned long seed = (unsigned long)time(NULL) ^ (unsigned long)rand();
    int pointCount = 15 + (rand() % 20);
    initSystems(seed);

    int chunksX = worldSize / CHUNK_SIZE;
    int chunksZ = worldSize / CHUNK_SIZE;

    float** worldHeightMap = malloc(worldSize * sizeof(float*));
    unsigned char** riverMap = malloc(worldSize * sizeof(unsigned char*));
    for(int i = 0; i < worldSize; i++) {
        worldHeightMap[i] = malloc(worldSize * sizeof(float));
        riverMap[i] = calloc(worldSize, sizeof(unsigned char));
    }

    PointCollection pointCollection;
    initCollection(&pointCollection, pointCount);
    generatePoints(&pointCollection, worldSize, pointCount);

    for(int x = 0; x < worldSize; x++) {
        for(int z = 0; z < worldSize; z++) {
            worldHeightMap[x][z] = generateHeightMap(
                x, z, 
                worldSize,
                &pointCollection
            );
        }
        if((x + 1) % 100 == 0) {
            printf("  Base terrain: %.1f%%\n", (float)(x + 1) / worldSize * 100.0f);
        }
    }
    simulateHydraulicErosion(worldHeightMap, worldSize, 3000, 3);
    thermalErosion(worldHeightMap, worldSize, 0.08f, 8);
    generateRivers(worldHeightMap, riverMap, worldSize, pointCount);

    PoissonCollection* objLocations = poissonDiskSampling(25.0f, worldSize, worldSize, 30);
    printf("Generated %d object locations\n", objLocations->count);

    Chunk** chunks = malloc(chunksX * sizeof(Chunk*));
    for(int x = 0; x < chunksX; x++) {
        chunks[x] = malloc(chunksX * sizeof(Chunk));
        for(int z = 0; z < chunksZ; z++) {
            chunks[x][z].x = x;
            chunks[x][z].z = z;
            memset(
                chunks[x][z].riverMap,
                0,
                CHUNK_SIZE * CHUNK_SIZE
            );

            for(int cx = 0; cx < CHUNK_SIZE; cx++) {
                for(int cz = 0; cz < CHUNK_SIZE; cz++) {
                    int worldX = x * CHUNK_SIZE + cx;
                    int worldZ = z * CHUNK_SIZE + cz;
                    chunks[x][z].heightMap[cx][cz] = worldHeightMap[worldX][worldZ];
                    chunks[x][z].riverMap[cx][cz] = riverMap[worldX][worldZ];
                }
            }

            generateChunk(
                &chunks[x][z],
                &pointCollection,
                objLocations,
                worldSize
            );
            //printf("  Chunks: %.1f%%\n", (float)(x + 1) / chunksX * 100.0f);
        }
    }

    saveMapToFile(
        fileName, 
        chunks,
        chunksX,
        chunksZ,
        &pointCollection,
        objLocations,
        seed
    );
    
    for(int i = 0; i < worldSize; i++) {
        free(worldHeightMap[i]);
        free(riverMap[i]);
    }
    free(worldHeightMap);
    free(riverMap);

    for(int x = 0; x < chunksX; x++) {
        free(chunks[x]);
    }
    free(chunks);
    freePointCollection(&pointCollection);
    free(objLocations->points);
    free(objLocations);
}