#pragma once
#include "main.h"
#include "poisson_disk.h"
#include "point_generator.h"
#include "domain_warp.h"
#include "noise.h"
#include "erosion.h"
#include "map_generator.h"

float generateHeightMapForMainScreen(
    float worldX,
    float worldZ,
    int worldSize,
    PointCollection* collection
);
void generateChunkForMainScreen(
    Chunk* chunk,
    PointCollection* collection,
    PoissonCollection* objLocations,
    int worldSize,
    int chunkSize
);
void generateMapForMainScreen(
    int worldSize,
    int chunkSize, 
    const char* filename
);