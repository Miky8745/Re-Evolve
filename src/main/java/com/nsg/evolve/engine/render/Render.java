package com.nsg.evolve.engine.render;

import com.nsg.evolve.engine.Window;
import com.nsg.evolve.engine.gui.QuadGenerator;
import com.nsg.evolve.engine.render.buffers.GBuffer;
import com.nsg.evolve.engine.render.buffers.RenderBuffers;
import com.nsg.evolve.engine.render.object.Model;
import com.nsg.evolve.engine.render.renderers.*;
import com.nsg.evolve.engine.scene.Scene;
import org.lwjgl.opengl.GL;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;
import static org.lwjgl.opengl.GL14.GL_FUNC_ADD;
import static org.lwjgl.opengl.GL14.glBlendEquation;
import static org.lwjgl.opengl.GL30.*;

public class Render {

    private AnimationRender animationRender;
    private GBuffer gBuffer;
    private LightsRender lightsRender;
    private RenderBuffers renderBuffers;
    private SceneRender sceneRender;
    private ShadowRender shadowRender;
    private SkyBoxRender skyBoxRender;
    private GUIRender customGUIRender;

    public Render(Window window) {
        GL.createCapabilities();
        glEnable(GL_MULTISAMPLE);
        glEnable(GL_DEPTH_TEST);

        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);

        // Support for transparencies
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        sceneRender = new SceneRender();
        skyBoxRender = new SkyBoxRender();
        shadowRender = new ShadowRender();
        lightsRender = new LightsRender();
        animationRender = new AnimationRender();
        gBuffer = new GBuffer(window);
        renderBuffers = new RenderBuffers();
        customGUIRender = new GUIRender();
    }

    public void cleanup() {
        sceneRender.cleanup();
        skyBoxRender.cleanup();
        shadowRender.cleanup();
        lightsRender.cleanup();
        animationRender.cleanup();
        gBuffer.cleanUp();
        renderBuffers.cleanup();
        customGUIRender.cleanup();
    }

    private void lightRenderFinish() {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    private void lightRenderStart(Window window) {
        resize(window.getWidth(), window.getHeight());

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, window.getWidth(), window.getHeight());

        glEnable(GL_BLEND);
        glBlendEquation(GL_FUNC_ADD);
        glBlendFunc(GL_ONE, GL_ONE);

        glBindFramebuffer(GL_READ_FRAMEBUFFER, gBuffer.getGBufferId());
    }

    public void render(Window window, Scene scene) {
        animationRender.render(scene, renderBuffers);
        shadowRender.render(scene, renderBuffers);
        sceneRender.render(scene, renderBuffers, gBuffer);
        lightRenderStart(window);
        lightsRender.render(scene, shadowRender, gBuffer);
        skyBoxRender.render(scene);
        lightRenderFinish();
        customGUIRender.renderAll();
    }

    public void resize(int width, int height) {
        QuadGenerator.Quad.resize(width, height);
    }

    public void setupData(Scene scene) {
        renderBuffers.loadStaticModels(scene);
        renderBuffers.loadAnimatedModels(scene);
        sceneRender.setupData(scene);
        shadowRender.setupData(scene);
        List<Model> modelList = new ArrayList<>(scene.getModelMap().values());
        modelList.forEach(m -> m.getMeshDataList().clear());
    }
}