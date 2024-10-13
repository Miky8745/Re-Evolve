package com.nsg.evolve.main;

import com.nsg.evolve.engine.Engine;
import com.nsg.evolve.engine.Window;
import com.nsg.evolve.engine.input.MouseInput;
import com.nsg.evolve.engine.interfaces.IAppLogic;
import com.nsg.evolve.engine.render.Render;
import com.nsg.evolve.engine.render.object.Entity;
import com.nsg.evolve.engine.render.object.Model;
import com.nsg.evolve.engine.scene.*;
import com.nsg.evolve.engine.scene.animations.AnimationData;
import com.nsg.evolve.engine.scene.lighting.SceneLights;
import com.nsg.evolve.engine.scene.lighting.lights.AmbientLight;
import com.nsg.evolve.engine.scene.lighting.lights.DirectionalLight;
import com.nsg.evolve.engine.sound.SoundBuffer;
import com.nsg.evolve.engine.sound.SoundListener;
import com.nsg.evolve.engine.sound.SoundManager;
import com.nsg.evolve.engine.sound.SoundSource;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.openal.AL11;

import static com.nsg.evolve.engine.input.Interactions.selectEntity;
import static org.lwjgl.glfw.GLFW.*;

public class Main implements IAppLogic {

    private static final float MOUSE_SENSITIVITY = 0.1f;
    private static final float MOVEMENT_SPEED = 0.001f;
    private float lightAngle;
    private float rotation;
    private AnimationData animationData1;
    private AnimationData animationData2;
    private SoundManager soundMgr;
    private Entity cubeEntity1;
    private Entity cubeEntity2;

    public static void main(String[] args) {
        Main main = new Main();
        Window.WindowOptions options = new Window.WindowOptions();
        options.antiAliasing = true;
        Engine gameEngine = new Engine("Evolve", options, main);
        try {
            gameEngine.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cleanup() {
        soundMgr.cleanup();
    }

    @Override
    public void init(Window window, Scene scene, Render render) {
        summonTerrain(scene);

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

        String bobModelId = "bobModel";
        Model bobModel = ModelLoader.loadModel(bobModelId, "resources/models/bob/boblamp.md5mesh",
                scene.getTextureCache(), scene.getMaterialCache(), true);
        scene.addModel(bobModel);
        Entity bobEntity = new Entity("bobEntity-1", bobModelId);
        bobEntity.setScale(0.05f);
        bobEntity.updateModelMatrix();
        animationData1 = new AnimationData(bobModel.getAnimationList().get(0));
        bobEntity.setAnimationData(animationData1);
        scene.addEntity(bobEntity);

        Entity bobEntity2 = new Entity("bobEntity-2", bobModelId);
        bobEntity2.setPosition(2, 0, 0);
        bobEntity2.setScale(0.025f);
        bobEntity2.updateModelMatrix();
        animationData2 = new AnimationData(bobModel.getAnimationList().get(0));
        bobEntity2.setAnimationData(animationData2);
        scene.addEntity(bobEntity2);

        render.setupData(scene);

        SceneLights sceneLights = new SceneLights();
        AmbientLight ambientLight = sceneLights.getAmbientLight();
        ambientLight.setIntensity(0.5f);
        ambientLight.setColor(0.3f, 0.3f, 0.3f);

        DirectionalLight dirLight = sceneLights.getDirLight();
        dirLight.setPosition(0, 1, 0);
        dirLight.setIntensity(1.0f);
        scene.setSceneLights(sceneLights);

        SkyBox skyBox = new SkyBox("resources/models/skybox/skybox.obj", scene.getTextureCache(),
                scene.getMaterialCache());
        skyBox.getSkyBoxEntity().setScale(1);
        skyBox.getSkyBoxEntity().updateModelMatrix();
        scene.setSkyBox(skyBox);

        scene.setFog(new Fog(true, new Vector3f(0.5f, 0.5f, 0.5f), 0.1f));

        Camera camera = scene.getCamera();
        camera.setPosition(-1.5f, 3.0f, 4.5f);
        camera.addRotation((float) Math.toRadians(15.0f), (float) Math.toRadians(390.f));

        initSounds(camera);

        lightAngle = 2.6f;
    }

    @Override
    public void input(Window window, Scene scene, long diffTimeMillis, boolean inputConsumed) {
        if (inputConsumed) {
            return;
        }

        float move = diffTimeMillis * MOVEMENT_SPEED;
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

        if (window.isKeyPressed(GLFW_KEY_LEFT)) {
            lightAngle -= 0.5f;
            if (lightAngle < -90) {
                lightAngle = -90;
            }
        } else if (window.isKeyPressed(GLFW_KEY_RIGHT)) {
            lightAngle += 0.5f;
            if (lightAngle > 90) {
                lightAngle = 90;
            }
        }

        MouseInput mouseInput = window.getMouseInput();
        if (mouseInput.isRightButtonPressed()) {
            Vector2f displVec = mouseInput.getDisplVec();
            camera.addRotation((float) Math.toRadians(
                            displVec.x * MOUSE_SENSITIVITY),
                    (float) Math.toRadians(displVec.y * MOUSE_SENSITIVITY));
        }

        if (mouseInput.isLeftButtonPressed()) {
            selectEntity(scene);
            if (scene.getSelectedEntity() != null) {
                System.out.println(scene.getSelectedEntity().getId());
            } else {
                System.out.println("Selected entity is null");
            }
        }

        SceneLights sceneLights = scene.getSceneLights();
        DirectionalLight dirLight = sceneLights.getDirLight();
        double angRad = Math.toRadians(lightAngle);
        dirLight.getDirection().x = (float) Math.sin(angRad);
        dirLight.getDirection().y = (float) Math.cos(angRad);
    }

    @Override
    public void update(Window window, Scene scene, long diffTimeMillis) {
        if (animationData1 != null) {
            animationData1.nextFrame();

            if (diffTimeMillis % 2 == 0) {
                animationData2.nextFrame();
            }
        }

        rotation += 1.5f;
        if (rotation > 360) {
            rotation = 0;
        }

        cubeEntity1.setRotation(1, 1, 1, (float) Math.toRadians(rotation));
        cubeEntity1.updateModelMatrix();

        cubeEntity2.setRotation(1, 1, 1, (float) Math.toRadians(360 - rotation));
        cubeEntity2.updateModelMatrix();
    }

    public void summonTerrain(Scene scene) {
        String terrainModelId = "terrain";
        Model terrainModel = ModelLoader.loadModel(terrainModelId, "resources/models/terrain/terrain.obj",
                scene.getTextureCache(), scene.getMaterialCache(), false);
        scene.addModel(terrainModel);
        Entity terrainEntity = new Entity("terrainEntity", terrainModelId);
        terrainEntity.setScale(100.0f);
        terrainEntity.updateModelMatrix();
        scene.addEntity(terrainEntity);
    }

    private void initSounds(Camera camera) {
        soundMgr = new SoundManager();
        soundMgr.setAttenuationModel(AL11.AL_EXPONENT_DISTANCE);
        soundMgr.setListener(new SoundListener(camera.getPosition()));

        SoundBuffer buffer = new SoundBuffer("resources/sounds/woo_scary.ogg");
        soundMgr.addSoundBuffer(buffer);
        SoundSource source = new SoundSource(true, true);
        source.setBuffer(buffer.getBufferId());
        soundMgr.addSoundSource("MUSIC", source);
        source.play();
    }
}