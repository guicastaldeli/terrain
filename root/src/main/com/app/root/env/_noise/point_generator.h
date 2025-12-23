#pragma once
#include "main.h"
#define MIN_POINT_RADIUS 30.0f
#define MAX_POINT_RADIUS 1000.f

typedef struct {
    float centerX;
    float centerZ;
    float radius;
    float heightScale;
    float ruggedness;
    float elevation;
} Point;

typedef struct {
    Point* points;
    int count;
    int capacity;
} PointCollection;

void generatePoints(
    PointCollection* collection, 
    int worldSize, 
    int targetCount
);
float pointMask(
    float worldX, 
    float worldZ,
    float centerX,
    float centerZ,
    float radius
);
void freePointCollection(PointCollection* collection);
void addPoint(PointCollection* collection, Point point);
void initCollection(PointCollection* collection, int initialCapacity);

