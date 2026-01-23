#include "main.h"
#include "map_generator.h"
#include <string.h>

/**
    * 
    * 
    *     Standalone World Map Image :)
    * 
    * 
 */
int main(int argc, char* argv[]) {
    const char* outputFile = "world.bin";
    unsigned long seed = time(NULL);
    int worldSize = 1024;
    int chunkSize = 32;

    for(int i = 1; i < argc; i++) {
        if(strcmp(argv[i], "--seed") == 0 && i + 1 < argc) {
            seed = atol(argv[++i]);
        } else if(strcmp(argv[i], "--output") == 0 && i + 1 < argc) {
            outputFile = argv[++i];
        } else if(strcmp(argv[i], "--size") == 0 && i + 1 < argc) {
            worldSize = atoi(argv[++i]);
        }
    }

    srand(seed);
    printf("Generating map with seed: %lu\n", seed);
    printf("World size: %d, Chunk size: %d\n", worldSize, chunkSize);
    printf("Output file: %s\n", outputFile);

    generateMap(worldSize, chunkSize, outputFile);

    printf("World generation complete!\n");
    return 0;
}