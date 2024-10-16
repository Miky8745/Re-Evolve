package com.nsg.evolve.engine.render.renderers;

import com.nsg.evolve.engine.render.buffers.RenderBuffers;
import com.nsg.evolve.engine.render.object.Entity;
import com.nsg.evolve.engine.render.object.Model;
import com.nsg.evolve.engine.render.shaders.Shaders;
import com.nsg.evolve.engine.render.shaders.Uniforms;
import com.nsg.evolve.engine.scene.Scene;
import com.nsg.evolve.engine.utilities.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL43.*;

public class AnimationRender {

    private static final ResourceLocation animationsComputeShader =
            new ResourceLocation("shaders/animations/animations.comp");

    private Shaders shaderProgram;
    private Uniforms uniformsMap;

    public AnimationRender() {
        List<Shaders.ShaderModuleData> shaderModuleDataList = new ArrayList<>();
        shaderModuleDataList.add(new Shaders.ShaderModuleData(animationsComputeShader.path(), GL_COMPUTE_SHADER));
        shaderProgram = new Shaders(shaderModuleDataList);
        createUniforms();
    }

    public void cleanup() {
        shaderProgram.cleanup();
    }

    private void createUniforms() {
        uniformsMap = new Uniforms(shaderProgram.getProgramId());
        uniformsMap.createUniform("drawParameters.srcOffset");
        uniformsMap.createUniform("drawParameters.srcSize");
        uniformsMap.createUniform("drawParameters.weightsOffset");
        uniformsMap.createUniform("drawParameters.bonesMatricesOffset");
        uniformsMap.createUniform("drawParameters.dstOffset");
    }

    public void render(Scene scene, RenderBuffers globalBuffer) {
        shaderProgram.bind();
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, globalBuffer.getBindingPosesBuffer());
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, globalBuffer.getBonesIndicesWeightsBuffer());
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 2, globalBuffer.getBonesMatricesBuffer());
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 3, globalBuffer.getDestAnimationBuffer());

        int dstOffset = 0;
        for (Model model : scene.getModelMap().values()) {
            if (model.isAnimated()) {
                for (RenderBuffers.MeshDrawData meshDrawData : model.getMeshDrawDataList()) {
                    RenderBuffers.AnimMeshDrawData animMeshDrawData = meshDrawData.animMeshDrawData();
                    Entity entity = animMeshDrawData.entity();
                    Model.AnimatedFrame frame = entity.getAnimationData().getCurrentFrame();
                    int groupSize = (int) Math.ceil((float) meshDrawData.sizeInBytes() / (14 * 4));
                    uniformsMap.setUniform("drawParameters.srcOffset", animMeshDrawData.bindingPoseOffset());
                    uniformsMap.setUniform("drawParameters.srcSize", meshDrawData.sizeInBytes() / 4);
                    uniformsMap.setUniform("drawParameters.weightsOffset", animMeshDrawData.weightsOffset());
                    uniformsMap.setUniform("drawParameters.bonesMatricesOffset", frame.getOffset());
                    uniformsMap.setUniform("drawParameters.dstOffset", dstOffset);
                    glDispatchCompute(groupSize, 1, 1);
                    dstOffset += meshDrawData.sizeInBytes() / 4;
                }
            }
        }

        glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);
        shaderProgram.unbind();
    }
}
