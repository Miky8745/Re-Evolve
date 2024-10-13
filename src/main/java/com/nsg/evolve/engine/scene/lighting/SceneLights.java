package com.nsg.evolve.engine.scene.lighting;

import com.nsg.evolve.engine.scene.lighting.lights.AmbientLight;
import com.nsg.evolve.engine.scene.lighting.lights.DirectionalLight;
import com.nsg.evolve.engine.scene.lighting.lights.PointLight;
import com.nsg.evolve.engine.scene.lighting.lights.SpotLight;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class SceneLights {

    private AmbientLight ambientLight;
    private DirectionalLight dirLight;
    private List<PointLight> pointLights;
    private List<SpotLight> spotLights;

    public SceneLights() {
        ambientLight = new AmbientLight();
        pointLights = new ArrayList<>();
        spotLights = new ArrayList<>();
        dirLight = new DirectionalLight(new Vector3f(1, 1, 1), new Vector3f(0, 1, 0), 1.0f);
    }

    public AmbientLight getAmbientLight() {
        return ambientLight;
    }

    public DirectionalLight getDirLight() {
        return dirLight;
    }

    public List<PointLight> getPointLights() {
        return pointLights;
    }

    public List<SpotLight> getSpotLights() {
        return spotLights;
    }

    public void setSpotLights(List<SpotLight> spotLights) {
        this.spotLights = spotLights;
    }
}
