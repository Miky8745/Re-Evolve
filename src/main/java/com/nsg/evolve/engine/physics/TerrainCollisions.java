package com.nsg.evolve.engine.physics;

import com.nsg.evolve.engine.scene.Scene;
import com.nsg.evolve.engine.utilities.Mth;
import com.nsg.evolve.game.terraingen.BiomeType;
import com.nsg.evolve.game.terraingen.TerrainGen;
import com.nsg.evolve.game.terraingen.TerrainHeightMap;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static com.nsg.evolve.game.Config.Character.CHARACTER_HEIGHT;

public class TerrainCollisions {
    public boolean enabled = true;
    public boolean movedLastUpdate = false;

    private Physics physics;

    public TerrainCollisions(Physics physics) {
        this.physics = physics;
    }

    public void update(Scene scene) {
        Vector3f position = scene.getCamera().getPosition();
        Vector3f velocity =  scene.getCamera().getVelocity();

        // Calculate height of the terrain on a given position
        float height;
        try {
            height = calculateTerrainHeightAt(new Vector2f(position.x, position.z), scene.getCamera().getActiveBiomeType());
        } catch (IndexOutOfBoundsException e) {
            movedLastUpdate = false;
            scene.getCamera().setOnGround(false);
            return;
        }

        if (position.y < height + CHARACTER_HEIGHT + CHARACTER_HEIGHT/3 && velocity.y < 0) {
            position.y = height + CHARACTER_HEIGHT;
            velocity.y = 0;
            scene.getCamera().setOnGround(true);
            movedLastUpdate = true;
        } else {
            movedLastUpdate = false;
        }
    }

    private float calculateTerrainHeightAt(Vector2f position, BiomeType biomeType) throws IndexOutOfBoundsException {
        TerrainHeightMap heightMap = TerrainGen.heights.get(biomeType);

        // Convert to integer grid coordinates for the four corners
        int x0 = (int) Math.floor(position.x);
        int y0 = (int) Math.floor(position.y);

        int x1 = x0 + 1;
        int y1 = y0 + 1;

        // Get the heights at the four surrounding points
        float x0y0 = heightMap.getOffsetHeight(x0, y0);
        float x1y0 = heightMap.getOffsetHeight(x1, y0);
        float x0y1 = heightMap.getOffsetHeight(x0, y1);
        float x1y1 = heightMap.getOffsetHeight(x1, y1);

        // Calculate interpolation weights (fractions of the current grid cell)
        float tx = position.x - x0;
        float ty = position.y - y0;

        // Interpolate in x-direction for both y0 and y1 rows
        float lerpY0 = Mth.simpleInterpolation(x0y0, x1y0, tx);
        float lerpY1 = Mth.simpleInterpolation(x0y1, x1y1, tx);

        // Interpolate in y-direction to get final height
        return Mth.simpleInterpolation(lerpY0, lerpY1, ty);
    }
}
