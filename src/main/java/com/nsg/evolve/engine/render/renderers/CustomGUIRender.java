package com.nsg.evolve.engine.render.renderers;

import com.nsg.evolve.engine.gui.QuadGenerator;
import com.nsg.evolve.engine.interfaces.IGUIElement;
import com.nsg.evolve.engine.render.shaders.Shaders;
import com.nsg.evolve.engine.render.shaders.Uniforms;

import java.util.ArrayList;
import java.util.List;

import static com.nsg.evolve.engine.Utilities.genPath;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class CustomGUIRender {

    private Shaders shaderProgram;
    private Uniforms uniformsMap;

    public CustomGUIRender() {
        List<Shaders.ShaderModuleData> shaderModuleDataList = new ArrayList<>();
        shaderModuleDataList.add(new Shaders.ShaderModuleData(genPath("shaders/customgui/customgui.vert"), GL_VERTEX_SHADER));
        shaderModuleDataList.add(new Shaders.ShaderModuleData(genPath("shaders/customgui/customgui.frag"), GL_FRAGMENT_SHADER));

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
