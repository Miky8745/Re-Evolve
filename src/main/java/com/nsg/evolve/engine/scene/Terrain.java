package com.nsg.evolve.engine.scene;

import com.nsg.evolve.engine.render.object.Entity;
import com.nsg.evolve.game.terraingen.BiomeType;
import com.nsg.evolve.game.terraingen.TerrainGen;

import static com.nsg.evolve.game.Config.TERRAIN_SIZE;

public class Terrain {
    private Entity terrain;
    private BiomeType biomeType;

    public Terrain(String modelId, BiomeType type) {
        terrain = new Entity("terrainEntity", modelId);
        terrain.setPosition(-TERRAIN_SIZE/2f, -TerrainGen.centerHeights.get(type), -TERRAIN_SIZE/2f);
        terrain.updateModelMatrix();
        this.biomeType = type;
    }

    public Entity getTerrain() {
        return terrain;
    }

    public BiomeType getType() {
        return biomeType;
    }
}

