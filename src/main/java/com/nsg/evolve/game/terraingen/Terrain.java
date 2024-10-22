package com.nsg.evolve.game.terraingen;

import com.nsg.evolve.engine.render.object.Entity;

public class Terrain {
    private Entity terrain;

    public Terrain(String modelId) {
        terrain = new Entity("terrainEntity", modelId);
    }

    public Entity getTerrain() {
        return terrain;
    }
}
