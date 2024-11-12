package com.nsg.evolve.game.terraingen;

import static com.nsg.evolve.game.Config.TERRAIN_SIZE;

public class TerrainHeightMap {
    private float[][] heights;
    private BiomeType biomeType;

    public TerrainHeightMap(int sizeX, int sizeY, BiomeType biomeType) {
        heights = new float[sizeX][sizeY];
        this.biomeType = biomeType;
    }

    public float getHeightAt(int x, int y) {
        return heights[x][y];
    }

    public float getOffsetHeight(int x, int y) {
        x += TERRAIN_SIZE/2;
        y += TERRAIN_SIZE/2;
        return heights[x][y];
    }

    public void writeHeightTo(int x, int y, float value) {
        heights[x][y] = value;
    }

    public BiomeType getBiomeType() {
        return biomeType;
    }
}
