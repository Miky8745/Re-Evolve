package com.nsg.evolve.engine.input;

import com.nsg.evolve.engine.render.object.Entity;
import com.nsg.evolve.engine.render.object.MeshData;
import com.nsg.evolve.engine.render.object.Model;
import com.nsg.evolve.engine.scene.Scene;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.primitives.Intersectionf;

import java.util.Collection;
import java.util.List;

public class Interactions {
    public static void selectEntity(Scene scene) {
        // Set mouse position to the center of the screen
        float x = 0.0f;  // Center of the screen in NDC is 0
        float y = 0.0f;  // Center of the screen in NDC is 0
        float z = -1.0f;

        Matrix4f invProjMatrix = scene.getProjection().getInvProjMatrix();
        Vector4f mouseDir = new Vector4f(x, y, z, 1.0f);
        mouseDir.mul(invProjMatrix);
        mouseDir.z = -1.0f;
        mouseDir.w = 0.0f;

        Matrix4f invViewMatrix = scene.getCamera().getInvViewMatrix();
        mouseDir.mul(invViewMatrix);
        Vector4f min = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
        Vector4f max = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
        Vector2f nearFar = new Vector2f();

        Entity selectedEntity = null;
        float closestDistance = Float.POSITIVE_INFINITY;
        Vector3f center = scene.getCamera().getPosition();

        Collection<Model> models = scene.getModelMap().values();
        Matrix4f modelMatrix = new Matrix4f();
        for (Model model : models) {
            List<Entity> entities = model.getEntitiesList();
            for (Entity entity : entities) {
                modelMatrix.translate(entity.getPosition()).scale(entity.getScale());
                for (MeshData meshData : model.getInteractionsMeshDataList()) {
                    Vector3f aabbMin = meshData.getAabbMin();
                    min.set(aabbMin.x, aabbMin.y, aabbMin.z, 1.0f);
                    min.mul(modelMatrix);
                    Vector3f aabMax = meshData.getAabbMax();
                    max.set(aabMax.x, aabMax.y, aabMax.z, 1.0f);
                    max.mul(modelMatrix);
                    if (Intersectionf.intersectRayAab(center.x, center.y, center.z, mouseDir.x, mouseDir.y, mouseDir.z,
                            min.x, min.y, min.z, max.x, max.y, max.z, nearFar) && nearFar.x < closestDistance) {
                        closestDistance = nearFar.x;
                        selectedEntity = entity;
                    }
                }
                modelMatrix.identity();
            }
        }

        scene.setSelectedEntity(selectedEntity);
    }
}
