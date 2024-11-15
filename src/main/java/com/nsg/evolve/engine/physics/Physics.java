package com.nsg.evolve.engine.physics;

import com.nsg.evolve.engine.Time;
import com.nsg.evolve.engine.scene.Scene;
import com.nsg.evolve.engine.utilities.Mth;
import org.joml.Vector3f;

public class Physics {

    private Gravity gravity;
    private TerrainCollisions terrainCollisions;

    public Physics() {
        gravity = new Gravity(this);
        terrainCollisions = new TerrainCollisions(this);
    }

    public void update(Scene scene) {
        if (gravity.enabled) {
            gravity.update(scene);
        }

        if (terrainCollisions.enabled) {
            terrainCollisions.update(scene);
        }

        moveEntities(scene);
    }

    public void moveEntities(Scene scene) {
        Vector3f pos = scene.getCamera().getPosition();
        Vector3f velocity = scene.getCamera().getVelocity();
        Vector3f updatedPos = Mth.add(pos, Mth.multiply(velocity, Time.deltaTime));

        scene.getCamera().setPosition(updatedPos.x, updatedPos.y, updatedPos.z);
    }

    public Gravity getGravity() {
        return gravity;
    }

    public TerrainCollisions getTerrainCollisions() {
        return terrainCollisions;
    }
}
