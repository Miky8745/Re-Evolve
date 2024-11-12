package com.nsg.evolve.engine.physics;

import com.nsg.evolve.engine.Time;
import com.nsg.evolve.engine.scene.Scene;

public class Gravity {

    public boolean enabled = false;

    private float g = 9.81f;

    public void update(Scene scene) {
        if (scene.getCamera().affectedByGravity) {
            scene.getCamera().getVelocity().y -= g * Time.deltaTime;
        }
    }

    public float getG() {
        return g;
    }

    public void setG(float g) {
        this.g = g;
    }
}
