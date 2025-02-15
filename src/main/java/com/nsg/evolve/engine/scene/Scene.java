package com.nsg.evolve.engine.scene;

import com.nsg.evolve.engine.physics.Physics;
import com.nsg.evolve.engine.render.object.Entity;
import com.nsg.evolve.engine.render.object.Model;
import com.nsg.evolve.engine.render.object.cache.MaterialCache;
import com.nsg.evolve.engine.render.object.cache.TextureCache;
import com.nsg.evolve.engine.scene.lighting.SceneLights;
import com.nsg.evolve.game.terraingen.BiomeType;

import java.util.HashMap;
import java.util.Map;

public class Scene {

    private Map<String, Model> modelMap;
    private Projection projection;
    private TextureCache textureCache;
    private Camera camera;
    private SceneLights sceneLights;
    private SkyBox skyBox;
    private Fog fog;
    private Entity selectedEntity;
    private MaterialCache materialCache;
    private Physics physics;

    public Scene(int width, int height, BiomeType biomeType) {
        modelMap = new HashMap<>();
        projection = new Projection(width, height);
        textureCache = new TextureCache();
        camera = new Camera(biomeType);
        materialCache = new MaterialCache();
        physics = new Physics();
    }

    public void addEntity(Entity entity) {
        String modelId = entity.getModelId();
        Model model = modelMap.get(modelId);
        if (model == null) {
            throw new RuntimeException("Could not find model [" + modelId + "]");
        }
        model.getEntitiesList().add(entity);
    }

    public void addModel(Model model) {
        modelMap.put(model.getId(), model);
    }

    public Map<String, Model> getModelMap() {
        return modelMap;
    }

    public Projection getProjection() {
        return projection;
    }

    public void resize(int width, int height) {
        projection.updateProjMatrix(width, height);
    }

    public TextureCache getTextureCache() {
        return textureCache;
    }

    public Camera getCamera() {
        return camera;
    }

    public SceneLights getSceneLights() {
        return sceneLights;
    }

    public void setSceneLights(SceneLights sceneLights) {
        this.sceneLights = sceneLights;
    }

    public SkyBox getSkyBox() {
        return skyBox;
    }

    public void setSkyBox(SkyBox skyBox) {
        this.skyBox = skyBox;
    }

    public Fog getFog() {
        return fog;
    }

    public void setFog(Fog fog) {
        this.fog = fog;
    }

    public Entity getSelectedEntity() {
        return selectedEntity;
    }

    public void setSelectedEntity(Entity selectedEntity) {
        this.selectedEntity = selectedEntity;
    }

    public MaterialCache getMaterialCache() {
        return materialCache;
    }

    public Physics getPhysics() {
        return physics;
    }
}