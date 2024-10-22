package com.nsg.evolve.game.terraingen;

import com.nsg.evolve.engine.noise.PerlinNoise;
import com.nsg.evolve.engine.render.object.Entity;

import static com.nsg.evolve.game.Config.NOISE_MULTIPLIER;
import static com.nsg.evolve.game.Config.TERRAIN_SIZE;

public class Terrain {
    private Entity terrain;

    public Terrain(String modelId, PerlinNoise noise) {
        terrain = new Entity("terrainEntity", modelId);
        terrain.setPosition(-TERRAIN_SIZE/2f, -noise.getComposedNoiseAt(TERRAIN_SIZE/2, TERRAIN_SIZE/2) * NOISE_MULTIPLIER, -TERRAIN_SIZE/2f);
        terrain.updateModelMatrix();
    }

    public Entity getTerrain() {
        return terrain;
    }
}
