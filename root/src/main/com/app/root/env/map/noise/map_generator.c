#include "map_generator.h"
#include "file_saver.h"

/*
** Generate Enhanced Height
*/
float generateEnhancedHeight(
    float worldX,
    float worldZ,
    PointCollection* collection
) {
    float warpedX = worldX;
    float warpedY = worldZ;
    domainWarp(&warpedX, &warpedY, 50.0f, 2);

    float baseNoise = fractualSimplexNoise(
        warpedX * 0.001f, 
        warpedY * 0.001f, 
        4, 0.5f, 2.0f
    );
    
    float mountainNoise = fractualSimplexNoise(
        warpedX * 0.003f, 
        warpedY * 0.003f, 
        6, 0.6f, 2.2f
    );
    
    float detailNoise = fractualSimplexNoise(
        warpedX * 0.01f, 
        warpedY * 0.01f, 
        3, 0.5f, 2.5f
    );

    float height = 
        baseNoise * 100.0f +
        mountainNoise * 200.0f +
        detailNoise * 50.0f;
    
    return height;
}

/*
** Generate Chunk
*/
void generateChunk(
    Chunk* chunk,
    PointCollection* collection,
    PoissonCollection* objLocations
) {
    int worldX = chunk->x * CHUNK_SIZE;
    int worldZ = chunk->z * CHUNK_SIZE;

    for(int x = 0; x < CHUNK_SIZE; x++) {
        for(int z = 0; z < CHUNK_SIZE; z++) {
            float globalX = worldX + x;
            float globalZ = worldZ + z;

            chunk->heightMap[x][z] = generateEnhancedHeight(globalX, globalZ, collection);
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

/*
** Generate Map
*/
void generateMap(const char* fileName) {
    unsigned long seed = (unsigned long)time(NULL) ^ (unsigned long)rand();
    int pointCount = 15 + (rand() % 20);
    initSystems(seed);

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
            worldHeightMap[x][z] = generateEnhancedHeight(x, z, &pointCollection);
        }
        if ((x + 1) % 100 == 0) {
            printf("  Base terrain: %.1f%%\n", (float)(x + 1) / WORLD_SIZE * 100.0f);
        }
    }
    simulateHydraulicErosion(worldHeightMap, WORLD_SIZE, 3000, 3);
    thermalErosion(worldHeightMap, WORLD_SIZE, 0.08f, 8);
    generateRivers(worldHeightMap, riverMap, WORLD_SIZE, pointCount);
    PoissonCollection* objLocations = poissonDiskSampling(25.0f, WORLD_SIZE, WORLD_SIZE, 30);
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
                objLocations
            );
            printf("  Chunks: %.1f%%\n", (float)(x + 1) / chunksX * 100.0f);
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

    for(int x = 0; x < chunksX; x++) {
        free(chunks[x]);
    }
    free(chunks);
    freePointCollection(&pointCollection);
    free(objLocations->points);
    free(objLocations);
}