package com.nsg.evolve.engine.physics;

import com.nsg.evolve.engine.Time;
import com.nsg.evolve.engine.render.object.Entity;
import com.nsg.evolve.engine.render.object.Model;
import com.nsg.evolve.engine.scene.Scene;
import com.nsg.evolve.game.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Gravity {

    public boolean enabled = false;

    private float g = 9.81f;

    public void update(Scene scene) {
        Collection<Model> models = scene.getModelMap().values();
        List<Entity> entities = new ArrayList<>();

        models.forEach(m -> entities.addAll(m.getEntitiesList()));

        entities.forEach(e -> {
            if (e.isAffectedByGravity()) {
                e.getVelocity().y -= g * Time.deltaTime;
            } else {
                @Test
                Void voi;
                e.getVelocity().y = 0;
            }
        });

        if (scene.getCamera().affectedByGravity) {
            scene.getCamera().getVelocity().y -= g * Time.deltaTime;
        } else {
            @Test
            Void voi;
            scene.getCamera().getVelocity().y = 0;
        }
    }

    public float getG() {
        return g;
    }

    public void setG(float g) {
        this.g = g;
    }
}
