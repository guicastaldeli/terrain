#include "map_generator.h"
#include "file_saver.h"
#include <math.h>

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

#define OCTAVES 8
#define PERSISTENCE 0.5
#define LACUNARITY 2.0

IslandParams generateIslandParams(
    unsigned long seed,
    int islandIndex,
    int totalIslands
) {
    IslandParams params;

    unsigned long islandSeed = seed + islandIndex * 12345;
    srand(islandSeed);
    params.islandRadius = WORLD_SIZE * (0.1f + (rand() % 200) / 1000.0f);

    float margin = params.islandRadius * 2.0f;
    
    params.centerX = margin + (rand() % (int)(WORLD_SIZE - 2 * margin));
    params.centerZ = margin + (rand() % (int)(WORLD_SIZE - 2 * margin));

    params.hasMountain = (rand() % 100) < 80;
    if(params.hasMountain) {
        params.mountainRadius = params.islandRadius * (0.2f + (rand() % 400) / 1000.0f);
        params.mountainHeight = 200.0f + (rand() % 400);
    } else {
        params.mountainRadius = 0;
        params.mountainHeight = 0;
    }

    params.baseHeight = 50.0f + (rand() % 100);
    params.floorHeight = (rand() % (int)params.baseHeight);
    
    printf("Island %d: radius=%.1f, center=(%.1f,%.1f), hasMountain=%d\n", 
           islandIndex, params.islandRadius, params.centerX, params.centerZ, params.hasMountain);
    
    return params;
}

/**
 * Generate Height Map
 */
float generateHeightMap(
    float worldX,
    float worldZ,
    PointCollection* collection,
    IslandParams* islands,
    int islandCount
) {
    float height = 0.0f;
    float bestIslandInfluence = 0.0f;
    for(int i = 0; i < islandCount; i++) {
        IslandParams* island = &islands[i];

        float dx = worldX - island->centerX;
        float dz = worldZ - island->centerZ;
        float distFromCenter = sqrtf(dx * dx + dz * dz);
        float normalizedDist = distFromCenter / island->islandRadius;

        float islandInfluence = 1.0f;
        if(normalizedDist > 0.7f) {
            float edgeFactor = (normalizedDist - 0.7f) / 0.3f;
            islandInfluence = 1.0f - (edgeFactor * edgeFactor);
            islandInfluence = fmaxf(0.0f, islandInfluence);
        }

        if(islandInfluence > bestIslandInfluence) {
            bestIslandInfluence = islandInfluence;

            float baseShape = 1.0f - normalizedDist * normalizedDist;
            height = island->baseHeight * baseShape * islandInfluence;
            if(island->hasMountain && distFromCenter < island->mountainRadius) {
                float mountainNorm = distFromCenter / island->mountainRadius;
                float mountainShape = 1.0f - mountainNorm * mountainNorm;
                height += island->mountainHeight * mountainShape;
            }
            height += island->floorHeight;
        }
    }

    if(bestIslandInfluence > 0.01f) {
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

        height += terrainNoise * 30.0f * bestIslandInfluence;
        height += detailNoise * 10.0f * bestIslandInfluence;
    }

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
    IslandParams* islands,
    int islandCount
) {
    int worldX = chunk->x * CHUNK_SIZE;
    int worldZ = chunk->z * CHUNK_SIZE;

    for(int x = 0; x < CHUNK_SIZE; x++) {
        for(int z = 0; z < CHUNK_SIZE; z++) {
            float globalX = worldX + x;
            float globalZ = worldZ + z;

            chunk->heightMap[x][z] = generateHeightMap(
                globalX, globalZ, 
                collection,
                islands,
                islandCount
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
void generateMap(const char* fileName) {
    unsigned long seed = (unsigned long)time(NULL) ^ (unsigned long)rand();
    int pointCount = 15 + (rand() % 20);
    initSystems(seed);

    int islandCount = 8 + (rand() % 9);
    IslandParams* islands = malloc(islandCount * sizeof(IslandParams));
    for(int i = 0; i < islandCount; i++) {
        islands[i] = generateIslandParams(seed, i, islandCount);
    }

    int chunksX = WORLD_SIZE / CHUNK_SIZE;
    int chunksZ = WORLD_SIZE / CHUNK_SIZE;

    float** worldHeightMap = malloc(WORLD_SIZE * sizeof(float*));
    unsigned char** riverMap = malloc(WORLD_SIZE * sizeof(unsigned char*));
    for(int i = 0; i < WORLD_SIZE; i++) {
        worldHeightMap[i] = malloc(WORLD_SIZE * sizeof(float));
        riverMap[i] = calloc(WORLD_SIZE, sizeof(unsigned char));
    }

    PointCollection pointCollection;
    initCollection(&pointCollection, pointCount);
    generatePoints(&pointCollection, WORLD_SIZE, pointCount);

    for(int x = 0; x < WORLD_SIZE; x++) {
        for(int z = 0; z < WORLD_SIZE; z++) {
            worldHeightMap[x][z] = generateHeightMap(
                x, z, 
                &pointCollection,
                islands,
                islandCount
            );
        }
        if((x + 1) % 100 == 0) {
            printf("  Base terrain: %.1f%%\n", (float)(x + 1) / WORLD_SIZE * 100.0f);
        }
    }
    //simulateHydraulicErosion(worldHeightMap, WORLD_SIZE, 3000, 3);
    //thermalErosion(worldHeightMap, WORLD_SIZE, 0.08f, 8);
    //generateRivers(worldHeightMap, riverMap, WORLD_SIZE, pointCount);

    PoissonCollection* objLocations = poissonDiskSampling(45.0f, WORLD_SIZE, WORLD_SIZE, 30);
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
                islands,
                islandCount
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
    
    for(int i = 0; i < WORLD_SIZE; i++) {
        free(worldHeightMap[i]);
        free(riverMap[i]);
    }
    free(worldHeightMap);
    free(riverMap);
    free(islands);

    for(int x = 0; x < chunksX; x++) {
        free(chunks[x]);
    }
    free(chunks);
    freePointCollection(&pointCollection);
    free(objLocations->points);
    free(objLocations);
}