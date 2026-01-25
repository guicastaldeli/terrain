#include "map_generator.h"
#include "file_saver.h"
#include "img.h"
#include <math.h>

static Island islands[MAX_ISLANDS];

int islandCount = 0;
int islandsInitialized = 0;

void initializeIslands(int worldSize) {
    islandCount = MAX_ISLANDS;

    int gridSize = (int)sqrt(MAX_ISLANDS);
    float cellSize = (float)worldSize / gridSize;
    
    float gapFactor = 0.8f;
    float minGap = cellSize * (1.0f - gapFactor);
    
    printf("Island distribution: %dx%d grid, cell size: %.1f, min gap: %.1f\n", 
           gridSize, gridSize, cellSize, minGap);
    
    for(int i = 0; i < islandCount; i++) {
        int gridX = i % gridSize;
        int gridZ = i / gridSize;
        
        islands[i].centerX = gridX * cellSize + (cellSize * 0.2f) + 
                            (rand() % (int)(cellSize * gapFactor));
        islands[i].centerZ = gridZ * cellSize + (cellSize * 0.2f) + 
                            (rand() % (int)(cellSize * gapFactor));
        
        islands[i].radius = MIN_ISLAND_RADIUS + (rand() % (int)(MAX_ISLAND_RADIUS - MIN_ISLAND_RADIUS));
        
        float maxAllowedRadius = cellSize * gapFactor * 0.4f;
        if(islands[i].radius > maxAllowedRadius) {
            islands[i].radius = maxAllowedRadius;
        }
        
        if(rand() % 100 < 30) {
            islands[i].mountainRadius = MIN_MOUNTAIN_RADIUS + 
                (rand() % (int)(MAX_MOUNTAIN_RADIUS - MIN_MOUNTAIN_RADIUS));
            islands[i].mountainHeight = 50.0f + (rand() % (int)(MAX_MOUNTAIN_HEIGHT - 50.0f));
        } else {
            islands[i].mountainRadius = 0.0f;
            islands[i].mountainHeight = 0.0f;
        }
        
        islands[i].baseHeight = 30.0f + (rand() % (int)BASE_HEIGHT_RANGE);
    }
    
    islandsInitialized = 1;
}

float generateHeightMap(
    float worldX,
    float worldZ,
    int worldSize,
    PointCollection* collection
) {
    if(collection == NULL) {
        printf("WARNING: PointCollection is null, using fallback height generation\n");
        return fractualSimplexNoise(worldX * 0.01f, worldZ * 0.01f, 4, 0.5f, 2.0f) * 100.0f;
    }
    if(!islandsInitialized) {
        initializeIslands(worldSize);
    }
    
    float totalHeight = 0.0f;
    float maxInfluence = 0.0f;
    int nearbyIslands[8];
    float distances[8];
    int foundCount = 0;
    
    int searchRadius = 3;
    int gridSize = (int)sqrt(MAX_ISLANDS);
    float cellSize = (float)worldSize / gridSize;
    int currentGridX = (int)(worldX / cellSize);
    int currentGridZ = (int)(worldZ / cellSize);
    
    for(int gx = currentGridX - searchRadius; gx <= currentGridX + searchRadius; gx++) {
        for(int gz = currentGridZ - searchRadius; gz <= currentGridZ + searchRadius; gz++) {
            if(gx < 0 || gx >= gridSize || gz < 0 || gz >= gridSize) continue;
            
            int islandIndex = gz * gridSize + gx;
            if(islandIndex >= islandCount) continue;
            
            float dx = worldX - islands[islandIndex].centerX;
            float dz = worldZ - islands[islandIndex].centerZ;
            float distFromCenter = sqrtf(dx * dx + dz * dz);
            
            if(distFromCenter > islands[islandIndex].radius * 2.0f) continue;
            
            if(foundCount < 8) {
                nearbyIslands[foundCount] = islandIndex;
                distances[foundCount] = distFromCenter;
                foundCount++;
            }
        }
    }
    
    for(int i = 0; i < foundCount; i++) {
        int islandIndex = nearbyIslands[i];
        float distFromCenter = distances[i];
        float normalizedDist = distFromCenter / islands[islandIndex].radius;
        float islandFalloff = 1.0f;
        
        if(normalizedDist > 0.7f) {
            float edgeFactor = (normalizedDist - 0.7f) / 0.3f;
            islandFalloff = 1.0f - (edgeFactor * edgeFactor);
            islandFalloff = fmaxf(0.0f, islandFalloff);
        }
        
        float baseHeight = islands[islandIndex].baseHeight * (1.0f - normalizedDist * normalizedDist) * islandFalloff;
        
        float mountainHeight = 0.0f;
        if(islands[islandIndex].mountainRadius > 0 && distFromCenter < islands[islandIndex].mountainRadius) {
            float mountainNorm = distFromCenter / islands[islandIndex].mountainRadius;
            mountainHeight = islands[islandIndex].mountainHeight * (1.0f - mountainNorm * mountainNorm);
        }
        
        float terrainNoise = fractualSimplexNoise(
            worldX * 0.02f,
            worldZ * 0.02f,
            2,
            0.3f,
            2.0f
        );
        
        float islandHeight = baseHeight + mountainHeight + terrainNoise * 15.0f * islandFalloff;
        
        float proximityWeight = 1.0f - fminf(1.0f, normalizedDist);
        totalHeight += islandHeight * proximityWeight;
        maxInfluence += proximityWeight;
    }
    
    float oceanFloor = -OCEAN_DEPTH;
    if(maxInfluence < 0.1f) {
        oceanFloor = -OCEAN_DEPTH + fractualSimplexNoise(worldX * 0.01f, worldZ * 0.01f, 1, 0.2f, 2.0f) * 20.0f;
    }
    
    float finalHeight = maxInfluence > 0.1f ? (totalHeight / maxInfluence) : oceanFloor;
    
    finalHeight = fmaxf(finalHeight, -OCEAN_DEPTH - 30.0f);
    
    return finalHeight;
}

/**
 * Generate Chunk
 */
void generateChunk(
    Chunk* chunk,
    PointCollection* collection,
    PoissonCollection* objLocations,
    int worldSize,
    int chunkSize
) {
    int worldX = chunk->x * chunkSize;
    int worldZ = chunk->z * chunkSize;

    for(int x = 0; x < chunkSize; x++) {
        for(int z = 0; z < chunkSize; z++) {
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
void generateMap(
    int worldSize,
    int chunkSize, 
    const char* fileName
) {
    unsigned long seed = (unsigned long)time(NULL) ^ (unsigned long)rand();
    int pointCount = 15 + (rand() % 20);
    initSystems(seed);

    int chunksX = worldSize / chunkSize;
    int chunksZ = worldSize / chunkSize;

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

    save(worldHeightMap, worldSize, worldSize, "world.png");

    simulateHydraulicErosion(worldHeightMap, worldSize, 3000, 3);
    thermalErosion(worldHeightMap, worldSize, 0.08f, 8);
    generateRivers(worldHeightMap, riverMap, worldSize, pointCount);

    PoissonCollection* objLocations = poissonDiskSampling(25.0f, worldSize, worldSize, 30);
    printf("Generated %d object locations\n", objLocations->count);

    Chunk** chunks = malloc(chunksX * sizeof(Chunk*));
    for(int x = 0; x < chunksX; x++) {
        chunks[x] = malloc(chunksZ * sizeof(Chunk));
        
        for(int z = 0; z < chunksZ; z++) {
            chunks[x][z].x = x;
            chunks[x][z].z = z;
            chunks[x][z].chunkSize = chunkSize;
            
            chunks[x][z].heightMap = malloc(chunkSize * sizeof(float*));
            chunks[x][z].waterMap = malloc(chunkSize * sizeof(float*));
            chunks[x][z].pointId = malloc(chunkSize * sizeof(unsigned char*));
            chunks[x][z].riverMap = malloc(chunkSize * sizeof(unsigned char*));
            chunks[x][z].objMap = malloc(chunkSize * sizeof(unsigned char*));
            
            for(int i = 0; i < chunkSize; i++) {
                chunks[x][z].heightMap[i] = malloc(chunkSize * sizeof(float));
                chunks[x][z].waterMap[i] = malloc(chunkSize * sizeof(float));
                chunks[x][z].pointId[i] = malloc(chunkSize * sizeof(unsigned char));
                chunks[x][z].riverMap[i] = malloc(chunkSize * sizeof(unsigned char));
                chunks[x][z].objMap[i] = malloc(chunkSize * sizeof(unsigned char));
            }
            
            for(int cx = 0; cx < chunkSize; cx++) {
                for(int cz = 0; cz < chunkSize; cz++) {
                    int worldX = x * chunkSize + cx;
                    int worldZ = z * chunkSize + cz;
                    chunks[x][z].heightMap[cx][cz] = worldHeightMap[worldX][worldZ];
                    chunks[x][z].riverMap[cx][cz] = riverMap[worldX][worldZ];
                }
            }

            generateChunk(
                &chunks[x][z],
                &pointCollection,
                objLocations,
                worldSize,
                chunkSize
            );
        }
    }

    saveMapToFile(
        fileName, 
        chunks,
        chunksX,
        chunksZ,
        chunkSize,
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