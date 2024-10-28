package com.nsg.evolve.engine.render.renderers;

import com.nsg.evolve.engine.render.buffers.GBuffer;
import com.nsg.evolve.engine.render.buffers.RenderBuffers;
import com.nsg.evolve.engine.render.object.Entity;
import com.nsg.evolve.engine.render.object.Material;
import com.nsg.evolve.engine.render.object.Model;
import com.nsg.evolve.engine.render.object.Texture;
import com.nsg.evolve.engine.render.object.cache.MaterialCache;
import com.nsg.evolve.engine.render.object.cache.TextureCache;
import com.nsg.evolve.engine.render.shaders.Shaders;
import com.nsg.evolve.engine.render.shaders.Uniforms;
import com.nsg.evolve.engine.scene.Scene;
import com.nsg.evolve.engine.utilities.ResourceLocation;
import org.lwjgl.system.MemoryUtil;
import org.tinylog.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL40.GL_DRAW_INDIRECT_BUFFER;
import static org.lwjgl.opengl.GL43.glMultiDrawElementsIndirect;

public class SceneRender {

    private static final ResourceLocation sceneVertexShader =
            new ResourceLocation("shaders/scene/scene.vert");

    private static final ResourceLocation sceneFragmentShader =
            new ResourceLocation("shaders/scene/scene.frag");


    public static final int MAX_DRAW_ELEMENTS = 100;
    public static final int MAX_ENTITIES = 50;
    private static final int COMMAND_SIZE = 5 * 4;
    private static final int MAX_MATERIALS = 20;
    private static final int MAX_TEXTURES = 16;
    private int animDrawCount;
    private int animRenderBufferHandle;
    private Map<String, Integer> entitiesIdxMap;
    private Shaders shaderProgram;
    private int staticDrawCount;
    private int staticRenderBufferHandle;
    private Uniforms uniformsMap;

    public SceneRender() {
        List<Shaders.ShaderModuleData> shaderModuleDataList = new ArrayList<>();
        shaderModuleDataList.add(new Shaders.ShaderModuleData(sceneVertexShader.getFileStream(), GL_VERTEX_SHADER));
        shaderModuleDataList.add(new Shaders.ShaderModuleData(sceneFragmentShader.getFileStream(), GL_FRAGMENT_SHADER));

        shaderProgram = new Shaders(shaderModuleDataList);
        createUniforms();
        entitiesIdxMap = new HashMap<>();
    }

    public void cleanup() {
        shaderProgram.cleanup();
        glDeleteBuffers(staticRenderBufferHandle);
        glDeleteBuffers(animRenderBufferHandle);
    }

    private void createUniforms() {
        uniformsMap = new Uniforms(shaderProgram.getProgramId());
        uniformsMap.createUniform("projectionMatrix");
        uniformsMap.createUniform("viewMatrix");

        for (int i = 0; i < MAX_TEXTURES; i++) {
            uniformsMap.createUniform("txtSampler[" + i + "]");
        }

        for (int i = 0; i < MAX_MATERIALS; i++) {
            String name = "materials[" + i + "]";
            uniformsMap.createUniform(name + ".diffuse");
            uniformsMap.createUniform(name + ".specular");
            uniformsMap.createUniform(name + ".reflectance");
            uniformsMap.createUniform(name + ".normalMapIdx");
            uniformsMap.createUniform(name + ".textureIdx");
        }

        for (int i = 0; i < MAX_DRAW_ELEMENTS; i++) {
            String name = "drawElements[" + i + "]";
            uniformsMap.createUniform(name + ".modelMatrixIdx");
            uniformsMap.createUniform(name + ".materialIdx");
        }

        for (int i = 0; i < MAX_ENTITIES; i++) {
            uniformsMap.createUniform("modelMatrices[" + i + "]");
        }
    }

    public void render(Scene scene, RenderBuffers renderBuffers, GBuffer gBuffer) {
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, gBuffer.getGBufferId());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, gBuffer.getWidth(), gBuffer.getHeight());
        glDisable(GL_BLEND);

        shaderProgram.bind();

        uniformsMap.setUniform("projectionMatrix", scene.getProjection().getProjMatrix());
        uniformsMap.setUniform("viewMatrix", scene.getCamera().getViewMatrix());

        TextureCache textureCache = scene.getTextureCache();
        List<Texture> textures = textureCache.getAll().stream().toList();
        int numTextures = textures.size();
        if (numTextures > MAX_TEXTURES) {
            Logger.warn("Only " + MAX_TEXTURES + " textures can be used");
        }
        for (int i = 0; i < Math.min(MAX_TEXTURES, numTextures); i++) {
            uniformsMap.setUniform("txtSampler[" + i + "]", i);
            Texture texture = textures.get(i);
            glActiveTexture(GL_TEXTURE0 + i);
            texture.bind();
        }

        int entityIdx = 0;
        for (Model model : scene.getModelMap().values()) {
            List<Entity> entities = model.getEntitiesList();
            for (Entity entity : entities) {
                uniformsMap.setUniform("modelMatrices[" + entityIdx + "]", entity.getModelMatrix());
                entityIdx++;
            }
        }

        // Static meshes
        int drawElement = 0;
        for (Model model: scene.getModelMap().values()) {
            if (model.isAnimated()) {
                continue;
            }
            List<Entity> entities = model.getEntitiesList();
            for (RenderBuffers.MeshDrawData meshDrawData : model.getMeshDrawDataList()) {
                for (Entity entity : entities) {
                    String name = "drawElements[" + drawElement + "]";
                    uniformsMap.setUniform(name + ".modelMatrixIdx", entitiesIdxMap.get(entity.getId()));
                    uniformsMap.setUniform(name + ".materialIdx", meshDrawData.materialIdx());
                    drawElement++;
                }
            }
        }
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, staticRenderBufferHandle);
        glBindVertexArray(renderBuffers.getStaticVaoId());
        glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_INT, 0, staticDrawCount, 0);

        // Animated meshes
        drawElement = 0;
        for (Model model: scene.getModelMap().values()) {
            if (!model.isAnimated()) {
                continue;
            }
            for (RenderBuffers.MeshDrawData meshDrawData : model.getMeshDrawDataList()) {
                RenderBuffers.AnimMeshDrawData animMeshDrawData = meshDrawData.animMeshDrawData();
                Entity entity = animMeshDrawData.entity();
                String name = "drawElements[" + drawElement + "]";
                uniformsMap.setUniform(name + ".modelMatrixIdx", entitiesIdxMap.get(entity.getId()));
                uniformsMap.setUniform(name + ".materialIdx", meshDrawData.materialIdx());
                drawElement++;
            }
        }
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, animRenderBufferHandle);
        glBindVertexArray(renderBuffers.getAnimVaoId());
        glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_INT, 0, animDrawCount, 0);

        glBindVertexArray(0);
        glEnable(GL_BLEND);
        shaderProgram.unbind();
    }

    private void setupAnimCommandBuffer(Scene scene) {
        List<Model> modelList = scene.getModelMap().values().stream().filter(Model::isAnimated).toList();
        int numMeshes = 0;
        for (Model model : modelList) {
            numMeshes += model.getMeshDrawDataList().size();
        }

        int firstIndex = 0;
        int baseInstance = 0;
        ByteBuffer commandBuffer = MemoryUtil.memAlloc(numMeshes * COMMAND_SIZE);
        for (Model model : modelList) {
            for (RenderBuffers.MeshDrawData meshDrawData : model.getMeshDrawDataList()) {
                // count
                commandBuffer.putInt(meshDrawData.vertices());
                // instanceCount
                commandBuffer.putInt(1);
                commandBuffer.putInt(firstIndex);
                // baseVertex
                commandBuffer.putInt(meshDrawData.offset());
                commandBuffer.putInt(baseInstance);

                firstIndex += meshDrawData.vertices();
                baseInstance++;
            }
        }
        commandBuffer.flip();

        animDrawCount = commandBuffer.remaining() / COMMAND_SIZE;

        animRenderBufferHandle = glGenBuffers();
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, animRenderBufferHandle);
        glBufferData(GL_DRAW_INDIRECT_BUFFER, commandBuffer, GL_DYNAMIC_DRAW);

        MemoryUtil.memFree(commandBuffer);
    }

    public void setupData(Scene scene) {
        setupEntitiesData(scene);
        setupStaticCommandBuffer(scene);
        setupAnimCommandBuffer(scene);
        setupMaterialsUniform(scene.getTextureCache(), scene.getMaterialCache());
    }

    private void setupEntitiesData(Scene scene) {
        entitiesIdxMap.clear();
        int entityIdx = 0;
        for (Model model : scene.getModelMap().values()) {
            List<Entity> entities = model.getEntitiesList();
            for (Entity entity : entities) {
                entitiesIdxMap.put(entity.getId(), entityIdx);
                entityIdx++;
            }
        }
    }

    private void setupMaterialsUniform(TextureCache textureCache, MaterialCache materialCache) {
        List<Texture> textures = textureCache.getAll().stream().toList();
        int numTextures = textures.size();
        if (numTextures > MAX_TEXTURES) {
            Logger.warn("Only " + MAX_TEXTURES + " textures can be used");
        }
        Map<String, Integer> texturePosMap = new HashMap<>();
        for (int i = 0; i < Math.min(MAX_TEXTURES, numTextures); i++) {
            texturePosMap.put(textures.get(i).getTexturePath(), i);
        }

        shaderProgram.bind();
        List<Material> materialList = materialCache.getMaterialsList();
        int numMaterials = materialList.size();
        for (int i = 0; i < numMaterials; i++) {
            Material material = materialCache.getMaterial(i);
            String name = "materials[" + i + "]";
            uniformsMap.setUniform(name + ".diffuse", material.getDiffuseColor());
            uniformsMap.setUniform(name + ".specular", material.getSpecularColor());
            uniformsMap.setUniform(name + ".reflectance", material.getReflectance());
            String normalMapPath = material.getNormalMapPath();
            int idx = 0;
            if (normalMapPath != null) {
                idx = texturePosMap.computeIfAbsent(normalMapPath, k -> 0);
            }
            uniformsMap.setUniform(name + ".normalMapIdx", idx);
            Texture texture = textureCache.getTexture(material.getTexturePath());
            idx = texturePosMap.computeIfAbsent(texture.getTexturePath(), k -> 0);
            uniformsMap.setUniform(name + ".textureIdx", idx);
        }
        shaderProgram.unbind();
    }

    private void setupStaticCommandBuffer(Scene scene) {
        List<Model> modelList = scene.getModelMap().values().stream().filter(m -> !m.isAnimated()).toList();
        int numMeshes = 0;
        for (Model model : modelList) {
            numMeshes += model.getMeshDrawDataList().size();
        }

        int firstIndex = 0;
        int baseInstance = 0;
        ByteBuffer commandBuffer = MemoryUtil.memAlloc(numMeshes * COMMAND_SIZE);
        for (Model model : modelList) {
            List<Entity> entities = model.getEntitiesList();
            int numEntities = entities.size();
            for (RenderBuffers.MeshDrawData meshDrawData : model.getMeshDrawDataList()) {
                // count
                commandBuffer.putInt(meshDrawData.vertices());
                // instanceCount
                commandBuffer.putInt(numEntities);
                commandBuffer.putInt(firstIndex);
                // baseVertex
                commandBuffer.putInt(meshDrawData.offset());
                commandBuffer.putInt(baseInstance);

                firstIndex += meshDrawData.vertices();
                baseInstance += entities.size();
            }
        }
        commandBuffer.flip();

        staticDrawCount = commandBuffer.remaining() / COMMAND_SIZE;

        staticRenderBufferHandle = glGenBuffers();
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, staticRenderBufferHandle);
        glBufferData(GL_DRAW_INDIRECT_BUFFER, commandBuffer, GL_DYNAMIC_DRAW);

        MemoryUtil.memFree(commandBuffer);
    }
}