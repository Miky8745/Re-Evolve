package com.nsg.evolve.engine.render.renderers;

import com.nsg.evolve.engine.gui.QuadGenerator;
import com.nsg.evolve.engine.interfaces.IGUIElement;
import com.nsg.evolve.engine.render.shaders.Shaders;
import com.nsg.evolve.engine.render.shaders.Uniforms;
import com.nsg.evolve.engine.utilities.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class GUIRender {

    private static final ResourceLocation guiVertexShader =
            new ResourceLocation("shaders/gui/gui.vert");

    private static final ResourceLocation guiFragmentShader =
            new ResourceLocation("shaders/gui/gui.frag");

    private Shaders shaderProgram;
    private Uniforms uniformsMap;

    public GUIRender() {
        List<Shaders.ShaderModuleData> shaderModuleDataList = new ArrayList<>();
        shaderModuleDataList.add(new Shaders.ShaderModuleData(guiVertexShader.getFileStream(), GL_VERTEX_SHADER));
        shaderModuleDataList.add(new Shaders.ShaderModuleData(guiFragmentShader.getFileStream(), GL_FRAGMENT_SHADER));

        shaderProgram = new Shaders(shaderModuleDataList);
        createUniforms();
    }

    public void cleanup() {
        shaderProgram.cleanup();

        QuadGenerator.registeredQuads.forEach(IGUIElement::cleanup);
    }

    private void createUniforms() {
        uniformsMap = new Uniforms(shaderProgram.getProgramId());
        uniformsMap.createUniform("txtSampler");
    }

    public void render(IGUIElement element) {
        shaderProgram.bind();

        uniformsMap.setUniform("txtSampler", 0);

        glBindVertexArray(element.getVaoId());
        glDrawElements(GL_TRIANGLES, element.getNumVertices(), GL_UNSIGNED_INT, 0);

        shaderProgram.unbind();
    }

    public void renderAll() {
        for (IGUIElement element : QuadGenerator.registeredQuads) {
            render(element);
        }
    }
}
