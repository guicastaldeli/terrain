#pragma once
#include "main.h"
#include "poisson_disk.h"
#include "point_generator.h"
#include "domain_warp.h"
#include "noise.h"
#include "erosion.h"

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

#define OCTAVES 8
#define PERSISTENCE 0.5
#define LACUNARITY 2.0

#define FIXED_ISLAND_RADIUS 450.0f
#define FIXED_MOUNTAIN_RADIUS 150.0f
#define FIXED_MOUNTAIN_HEIGHT 400.0f
#define FIXED_BASE_HEIGHT 100.0f

#define MAX_ISLANDS 3000
#define MIN_ISLAND_RADIUS 500.0f
#define MAX_ISLAND_RADIUS 1000.0f
#define MIN_MOUNTAIN_RADIUS 10.0f
#define MAX_MOUNTAIN_RADIUS 500.0f
#define MAX_MOUNTAIN_HEIGHT 500.0f
#define BASE_HEIGHT_RANGE 80.0f

#define WATER_LEVEL 50.0f
#define OCEAN_DEPTH 100.0f

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

typedef struct {
    float centerX;
    float centerZ;
    float radius;
    float mountainRadius;
    float mountainHeight;
    float baseHeight;
} Island;

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