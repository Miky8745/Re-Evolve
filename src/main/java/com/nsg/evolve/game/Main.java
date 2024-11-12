package com.nsg.evolve.game;

import com.nsg.evolve.engine.Engine;
import com.nsg.evolve.engine.Time;
import com.nsg.evolve.engine.Window;
import com.nsg.evolve.engine.gui.Button;
import com.nsg.evolve.engine.gui.QuadGenerator;
import com.nsg.evolve.engine.input.MouseInput;
import com.nsg.evolve.engine.interfaces.IAppLogic;
import com.nsg.evolve.engine.noise.PerlinNoise;
import com.nsg.evolve.engine.physics.Physics;
import com.nsg.evolve.engine.render.Render;
import com.nsg.evolve.engine.render.object.Entity;
import com.nsg.evolve.engine.render.object.Model;
import com.nsg.evolve.engine.scene.Terrain;
import com.nsg.evolve.engine.scene.*;
import com.nsg.evolve.engine.scene.lighting.SceneLights;
import com.nsg.evolve.engine.scene.lighting.lights.AmbientLight;
import com.nsg.evolve.engine.scene.lighting.lights.DirectionalLight;
import com.nsg.evolve.game.terraingen.BiomeType;
import com.nsg.evolve.game.terraingen.TerrainGen;
import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static com.nsg.evolve.engine.input.Interactions.selectEntity;
import static com.nsg.evolve.game.Config.*;
import static org.lwjgl.glfw.GLFW.*;

public class Main implements IAppLogic {

    private float rotation;
    private Entity cubeEntity1;
    private Entity cubeEntity2;
    private boolean buttonClicked = false;
    private Physics physics;

    public static void main(String[] args) {
        Main main = new Main();
        Window.WindowOptions options = new Window.WindowOptions();
        options.antiAliasing = true;
        Engine gameEngine = new Engine("Re-Evolve", options, main, BiomeType.BEACH);
        try {
            gameEngine.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cleanup() {
        // Nothing to do here
    }

    @Override
    public void init(Window window, Scene scene, Render render) {
        physics = new Physics();
        physics.getGravity().enabled = true;
        physics.getTerrainCollisions().enabled = true;

        scene.getCamera().affectedByGravity = true;

        PerlinNoise noise = PerlinNoise.generateNoise(TERRAIN_SIZE,TERRAIN_SIZE,400,12);

        Model terrainModel = TerrainGen.generateTerrain(scene, noise, BiomeType.BEACH);
        scene.addModel(terrainModel);

        Terrain terrain = new Terrain(terrainModel.getId(), BiomeType.BEACH);
        scene.addEntity(terrain.getTerrain());

        Model cubeModel = ModelLoader.loadModel("cube-model", "resources/models/cube/cube.obj",
                scene.getTextureCache(), scene.getMaterialCache(), false);
        scene.addModel(cubeModel);
        cubeEntity1 = new Entity("cube-entity-1", cubeModel.getId());
        cubeEntity1.setPosition(0, 2, -1);
        cubeEntity1.updateModelMatrix();
        scene.addEntity(cubeEntity1);

        cubeEntity2 = new Entity("cube-entity-2", cubeModel.getId());
        cubeEntity2.setPosition(-2, 2, -1);
        cubeEntity2.updateModelMatrix();
        scene.addEntity(cubeEntity2);

        render.setupData(scene);

        SceneLights sceneLights = new SceneLights();
        AmbientLight ambientLight = sceneLights.getAmbientLight();
        ambientLight.setIntensity(0.5f);
        ambientLight.setColor(0.3f, 0.3f, 0.3f);

        DirectionalLight dirLight = sceneLights.getDirLight();
        dirLight.setPosition(0, 1, 0);
        dirLight.setIntensity(1f);
        scene.setSceneLights(sceneLights);
        double angRad = Math.toRadians(2.501f);
        dirLight.getDirection().x = (float) Math.sin(angRad);
        dirLight.getDirection().y = (float) Math.cos(angRad);

        scene.setFog(new Fog(false, new Vector3f(0.5f, 0.5f, 0.5f), 0.1f));

        Camera camera = scene.getCamera();
        camera.setPosition(-1.5f, 3.0f, 4.5f);
        camera.addRotation(Math.toRadians(0), Math.toRadians(0));
    }

    @Override
    public void input(Window window, Scene scene) {
        if (window.getMouseInput().isLeftButtonPressed()) {
            QuadGenerator.registeredQuads.forEach(e -> {
                if (e instanceof Button button) {
                    if (button.click(window.getMouseInput().getCurrentPos())) {
                        buttonClicked = true;
                    }
                }
            });
        }

        if (buttonClicked) {
            buttonClicked = false;
            return;
        }

        float move = Time.deltaTimeMillis * MOVEMENT_SPEED;
        Camera camera = scene.getCamera();

        if (window.isKeyPressed(GLFW_KEY_W)) {
            camera.moveForward(move);
        } else if (window.isKeyPressed(GLFW_KEY_S)) {
            camera.moveBackwards(move);
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            camera.moveLeft(move);
        } else if (window.isKeyPressed(GLFW_KEY_D)) {
            camera.moveRight(move);
        }

        if (window.isKeyPressed(GLFW_KEY_SPACE)) {
            camera.moveUp(move);
        } else if (window.isKeyPressed(GLFW_KEY_LEFT_SHIFT)) {
            camera.moveDown(move);
        }

        MouseInput mouseInput = window.getMouseInput();
        if (mouseInput.isRightButtonPressed()) {
            Vector2f displVec = mouseInput.getDisplVec();
            camera.addRotation(Math.toRadians(
                            displVec.x * MOUSE_SENSITIVITY),
                    Math.toRadians(displVec.y * MOUSE_SENSITIVITY));
        }

        if (mouseInput.isLeftButtonPressed()) {
            selectEntity(scene);
            if (scene.getSelectedEntity() != null) {
                System.out.println(scene.getSelectedEntity().getId());
            }
        }
    }

    @Override
    public void update(Window window, Scene scene) {
        physics.update(scene);

        rotation += 1.5f;
        if (rotation > 360) {
            rotation = 0;
        }

        cubeEntity1.setRotation(1, 1, 1, Math.toRadians(rotation));
        cubeEntity1.updateModelMatrix();

        cubeEntity2.setRotation(1, 1, 1, Math.toRadians(360 - rotation));
        cubeEntity2.updateModelMatrix();
    }
}
