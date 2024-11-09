package com.nsg.evolve.engine.physics;

import com.nsg.evolve.engine.Time;
import com.nsg.evolve.engine.scene.Scene;
import com.nsg.evolve.engine.utilities.Mth;
import org.joml.Vector3f;

public class Physics {

    private Gravity gravity;

    public Physics() {
        gravity = new Gravity();
    }

    public void update(Scene scene) {
        if (gravity.enabled) {
            gravity.update(scene);
        }

        moveEntities(scene);
    }

    public void moveEntities(Scene scene) {
        Vector3f pos = scene.getCamera().getPosition();
        Vector3f velocity = scene.getCamera().getVelocity();
        Vector3f updatedPos = Mth.add(pos, Mth.multiply(velocity, Time.deltaTime));
        scene.getCamera().setPosition(updatedPos.x, updatedPos.y, updatedPos.z);

        scene.getModelMap().values().forEach(m -> m.getEntitiesList().forEach(e -> {
            Vector3f updatedPosition = Mth.add(e.getPosition(), Mth.multiply(e.getVelocity(), Time.deltaTime));
            e.setPosition(updatedPosition.x, updatedPosition.y, updatedPosition.z);
        }));
    }

    public Gravity getGravity() {
        return gravity;
    }
}
