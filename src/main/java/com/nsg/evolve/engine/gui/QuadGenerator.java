package com.nsg.evolve.engine.gui;

import com.nsg.evolve.engine.interfaces.IGUIController;
import com.nsg.evolve.engine.interfaces.IGUIElement;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class QuadGenerator implements IGUIController {

    public static List<IGUIElement> registeredQuads = new ArrayList<>();

    @Override
    public List<IGUIElement> getActiveElements() {
        return registeredQuads;
    }

    public static class Quad implements IGUIElement {

        private int numVertices;
        private int vaoId;
        private List<Integer> vboIdList;

        public Quad(Vector2f min, Vector2f max, int layer) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                vboIdList = new ArrayList<>();
                float gl_layer = layer * -0.1f;

                float[] positions = new float[]{
                        min.x, min.y, gl_layer,
                        max.x, min.y, gl_layer,
                        min.x, max.y, gl_layer,
                        max.x, max.y, gl_layer,};
                float[] textCoords = new float[]{
                        0.0f, 0.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f,};
                int[] indices = new int[]{0, 2, 1, 1, 2, 3};
                numVertices = indices.length;

                vaoId = glGenVertexArrays();
                glBindVertexArray(vaoId);

                // Positions VBO
                int vboId = glGenBuffers();
                vboIdList.add(vboId);
                FloatBuffer positionsBuffer = stack.callocFloat(positions.length);
                positionsBuffer.put(0, positions);
                glBindBuffer(GL_ARRAY_BUFFER, vboId);
                glBufferData(GL_ARRAY_BUFFER, positionsBuffer, GL_STATIC_DRAW);
                glEnableVertexAttribArray(0);
                glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

                // Texture coordinates VBO
                vboId = glGenBuffers();
                vboIdList.add(vboId);
                FloatBuffer textCoordsBuffer = stack.callocFloat(textCoords.length);
                textCoordsBuffer.put(0, textCoords);
                glBindBuffer(GL_ARRAY_BUFFER, vboId);
                glBufferData(GL_ARRAY_BUFFER, textCoordsBuffer, GL_STATIC_DRAW);
                glEnableVertexAttribArray(1);
                glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

                // Index VBO
                vboId = glGenBuffers();
                vboIdList.add(vboId);
                IntBuffer indicesBuffer = stack.callocInt(indices.length);
                indicesBuffer.put(0, indices);
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboId);
                glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

                glBindBuffer(GL_ARRAY_BUFFER, 0);
                glBindVertexArray(0);
            }
        }

        public void cleanup() {
            vboIdList.forEach(GL30::glDeleteBuffers);
            glDeleteVertexArrays(vaoId);
        }

        public int getNumVertices() {
            return numVertices;
        }

        public int getVaoId() {
            return vaoId;
        }
    }
}
