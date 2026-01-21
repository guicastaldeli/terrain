#pragma once
#include "main.h"
#include "poisson_disk.h"
#include "point_generator.h"
#include "domain_warp.h"
#include "noise.h"
#include "erosion.h"

typedef struct {
    int x;
    int z;
    int chunkSize;
    float** heightMap;
    float** waterMap;
    unsigned char** pointId;
    unsigned char** riverMap;
    unsigned char** objMap;
} Chunk;

float generateHeightMap(
    float worldX,
    float worldZ,
    int worldSize,
    PointCollection* collection
);
void generateChunk(
    Chunk* chunk,
    PointCollection* collection,
    PoissonCollection* objLocations,
    int worldSize,
    int chunkSize
);
void generateMap(
    int worldSize,
    int chunkSize, 
    const char* filename
);