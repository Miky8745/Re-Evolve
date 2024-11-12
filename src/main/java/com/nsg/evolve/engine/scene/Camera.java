package com.nsg.evolve.engine.scene;

import com.nsg.evolve.game.terraingen.BiomeType;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Camera {

    private Vector3f position;
    private Vector2f rotation;
    private Matrix4f viewMatrix;
    private Matrix4f invViewMatrix;

    public boolean affectedByGravity;
    private Vector3f velocity;

    private BiomeType activeBiomeType;

    public Camera(BiomeType biomeType) {
        position = new Vector3f(0, 0, 0);  // Initial camera position
        rotation = new Vector2f(0, 0);  // Initial rotation: pitch (x), yaw (y)
        viewMatrix = new Matrix4f();
        invViewMatrix = new Matrix4f();
        recalculate();  // Set up initial view matrix
        affectedByGravity = false;
        velocity = new Vector3f(0,0,0);
        activeBiomeType = biomeType;
    }

    public void addRotation(float pitch, float yaw) {
        rotation.add(pitch, yaw);
        recalculate();
    }

    public Vector3f getPosition() {
        return position;
    }

    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    public Matrix4f getInvViewMatrix() {
        return invViewMatrix;
    }

    public void moveForward(float distance) {
        // Move forward in the direction the camera is facing, ignoring Y axis
        Vector3f forward = new Vector3f(
                (float) Math.sin(rotation.y), 0, -(float) Math.cos(rotation.y) // Only horizontal direction
        ).normalize().mul(distance);
        position.add(forward);
        recalculate();
    }

    public void moveBackwards(float distance) {
        // Move backward in the direction opposite to the camera facing direction, ignoring Y axis
        Vector3f backward = new Vector3f(
                (float) Math.sin(rotation.y), 0, -(float) Math.cos(rotation.y) // Only horizontal direction
        ).normalize().mul(-distance);
        position.add(backward);
        recalculate();
    }

    public void moveLeft(float distance) {
        // Move left (relative to the camera's right vector, which is perpendicular to forward)
        Vector3f left = new Vector3f(
                -(float) Math.cos(rotation.y), 0, -(float) Math.sin(rotation.y) // Perpendicular to forward
        ).normalize().mul(distance);
        position.add(left);
        recalculate();
    }

    public void moveRight(float distance) {
        // Move right (relative to the camera's right vector, which is perpendicular to forward)
        Vector3f right = new Vector3f(
                (float) Math.cos(rotation.y), 0, (float) Math.sin(rotation.y) // Perpendicular to forward
        ).normalize().mul(distance);
        position.add(right);
        recalculate();
    }

    public void moveUp(float distance) {
        // Move directly upward in world space (along the Y axis)
        position.y += distance;
        recalculate();
    }

    public void moveDown(float distance) {
        // Move directly downward in world space (along the Y axis)
        position.y -= distance;
        recalculate();
    }

    private void recalculate() {
        // Recalculate the view matrix based on the updated position and rotation
        viewMatrix.identity()
                .rotateX(rotation.x)  // Pitch (up/down rotation)
                .rotateY(rotation.y)  // Yaw (left/right rotation)
                .translate(-position.x, -position.y, -position.z);

        invViewMatrix.set(viewMatrix).invert();
    }

    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
        recalculate();
    }

    public void setRotation(float pitch, float yaw) {
        rotation.set(pitch, yaw);
        recalculate();
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector3f velocity) {
        this.velocity = velocity;
    }

    public BiomeType getActiveBiomeType() {
        return activeBiomeType;
    }

    public void setActiveBiomeType(BiomeType activeBiomeType) {
        this.activeBiomeType = activeBiomeType;
    }
}
