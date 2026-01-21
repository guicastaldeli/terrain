#pragma once
#include "main.h"
#include "poisson_disk.h"
#include "point_generator.h"
#include "domain_warp.h"
#include "noise.h"
#include "erosion.h"

#define WORLD_SIZE 1024
#define CHUNK_SIZE 16

typedef struct {
    int x;
    int z;
    float heightMap[CHUNK_SIZE][CHUNK_SIZE];
    float waterMap[CHUNK_SIZE][CHUNK_SIZE];
    unsigned char pointId[CHUNK_SIZE][CHUNK_SIZE];
    unsigned char riverMap[CHUNK_SIZE][CHUNK_SIZE];
    unsigned char objMap[CHUNK_SIZE][CHUNK_SIZE];
} Chunk;

typedef struct {
    float islandRadius;
    float mountainRadius;
    float mountainHeight;
    float baseHeight;
    float floorHeight;
    int hasMountain;
    float centerX;
    float centerZ;
} IslandParams;

IslandParams generateIslandParams(
    unsigned long seed, 
    int islandIndex,
    int totalInslands
);
float generateHeightMap(
    float worldX,
    float worldZ,
    PointCollection* collection,
    IslandParams* islands,
    int islandCount
);
void generateChunk(
    Chunk* chunk,
    PointCollection* collection,
    PoissonCollection* objLocations,
    IslandParams* islands,
    int islandCount
);
void generateMap(const char* filename);