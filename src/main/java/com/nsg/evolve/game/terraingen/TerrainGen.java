package com.nsg.evolve.game.terraingen;

import com.nsg.evolve.engine.noise.PerlinNoise;
import com.nsg.evolve.engine.render.object.Material;
import com.nsg.evolve.engine.render.object.MeshData;
import com.nsg.evolve.engine.render.object.Model;
import com.nsg.evolve.engine.scene.Scene;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nsg.evolve.game.Config.TERRAIN_SIZE;
import static com.nsg.evolve.game.Config.Terrain.BEACH_NOISE_MULTIPLIER;
import static com.nsg.evolve.game.terraingen.TerrainGen.HeightGetters.getYPositionForBiomeType;

public class TerrainGen {

    public static Map<BiomeType, Float> centerHeights = new HashMap<>();

    public static Model generateTerrain(Scene scene, PerlinNoise noise, BiomeType biomeType) {
        List<float[]> shaderData = generateShaderData(noise, biomeType);
        float[] positions = shaderData.get(0);
        float[] normals = shaderData.get(1);
        float[] textCoords = shaderData.get(2);
        int[] indices = generateIndices();

        shaderData = generateTangentsAndBitangents(positions, textCoords, indices);
        float[] tangents = shaderData.get(0);
        float[] bitangents = shaderData.get(1);

        int materials = registerMaterials(scene);

        MeshData data = new MeshData(
                positions,
                normals,
                tangents,
                bitangents,
                textCoords,
                indices,
                new int[]{0},
                new float[]{0},
                new Vector3f(0,-100,0),
                new Vector3f(0,-100,0)
        );
        data.setMaterialIdx(materials);

        return new Model(
                "terrain",
                new ArrayList<>(List.of(data)),
                new ArrayList<>()
        );
    }

    private static int registerMaterials(Scene scene) {
        Material material = new Material();

        scene.getTextureCache().createTexture("resources/textures/sand.png");
        material.setTexturePath("resources/textures/sand.png");
        scene.getMaterialCache().addMaterial(material);
        return material.getMaterialIdx();
    }

    private static List<float[]> generateShaderData(PerlinNoise noise, BiomeType biomeType) {
        List<Vector3f> vPositions = new ArrayList<>();
        List<Vector3f> vNormals = new ArrayList<>();
        List<Vector2f> vTexCoords = new ArrayList<>();

        for (int x = 0; x < TERRAIN_SIZE; x++) {
            for (int z = 0; z < TERRAIN_SIZE; z++) {
                float y = getYPositionForBiomeType(x, z, noise, biomeType);
                if (x == TERRAIN_SIZE/2 && z == TERRAIN_SIZE/2) {
                    centerHeights.put(biomeType, y);
                }

                vPositions.add(new Vector3f(x, y, z));

                vNormals.add(calculateNormal(x, z, biomeType, noise));

                float u = (float) x / (TERRAIN_SIZE - 1);
                float v = (float) z / (TERRAIN_SIZE - 1);
                vTexCoords.add(new Vector2f(u, v));
            }
        }

        float[] positions = new float[vPositions.size() * 3];
        float[] normals = new float[vNormals.size() * 3];
        float[] texCoords = new float[vTexCoords.size() * 2];

        for (int i = 0; i < vPositions.size(); i++) {
            positions[i * 3] = vPositions.get(i).x;
            positions[i * 3 + 1] = vPositions.get(i).y;
            positions[i * 3 + 2] = vPositions.get(i).z;

            normals[i * 3] = vNormals.get(i).x;
            normals[i * 3 + 1] = vNormals.get(i).y;
            normals[i * 3 + 2] = vNormals.get(i).z;

            texCoords[i * 2] = vTexCoords.get(i).x;
            texCoords[i * 2 + 1] = vTexCoords.get(i).y;
        }

        return List.of(
                positions,
                normals,
                texCoords
        );
    }

    private static Vector3f calculateNormal(int x, int z, BiomeType biomeType, PerlinNoise perlinNoise) {
        Vector3f left = (x > 0) ? new Vector3f(x - 1, getYPositionForBiomeType(x - 1, z, perlinNoise, biomeType), z) : new Vector3f(x, getYPositionForBiomeType(x, z, perlinNoise, biomeType), z);
        Vector3f right = (x < TERRAIN_SIZE - 1) ? new Vector3f(x + 1, getYPositionForBiomeType(x + 1, z, perlinNoise, biomeType), z) : new Vector3f(x, getYPositionForBiomeType(x, z, perlinNoise, biomeType), z);
        Vector3f down = (z > 0) ? new Vector3f(x, getYPositionForBiomeType(x, z - 1, perlinNoise, biomeType), z - 1) : new Vector3f(x, getYPositionForBiomeType(x, z, perlinNoise, biomeType), z);
        Vector3f up = (z < TERRAIN_SIZE - 1) ? new Vector3f(x, getYPositionForBiomeType(x, z + 1, perlinNoise, biomeType), z + 1) : new Vector3f(x, getYPositionForBiomeType(x, z, perlinNoise, biomeType), z);

        Vector3f horizontal = new Vector3f(right.x - left.x, right.y - left.y, right.z - left.z);
        Vector3f vertical = new Vector3f(up.x - down.x, up.y - down.y, up.z - down.z);

        Vector3f normal = new Vector3f();
        vertical.cross(horizontal, normal);

        normal.normalize();

        return normal;
    }

    private static int[] generateIndices() {
        List<Integer> indicesList = new ArrayList<>();

        // Generate indices for each quad (square) on the grid
        for (int x = 0; x < TERRAIN_SIZE - 1; x++) {
            for (int y = 0; y < TERRAIN_SIZE - 1; y++) {
                // Indices for the two triangles of each quad
                int topLeft = x * TERRAIN_SIZE + y;
                int topRight = (x + 1) * TERRAIN_SIZE + y;
                int bottomLeft = x * TERRAIN_SIZE + (y + 1);
                int bottomRight = (x + 1) * TERRAIN_SIZE + (y + 1);

                // Triangle 1 (top-left, bottom-left, bottom-right)
                indicesList.add(topLeft);
                indicesList.add(bottomLeft);
                indicesList.add(bottomRight);

                // Triangle 2 (top-left, bottom-right, top-right)
                indicesList.add(topLeft);
                indicesList.add(bottomRight);
                indicesList.add(topRight);
            }
        }

        // Convert to int[] array
        int[] indices = new int[indicesList.size()];
        for (int i = 0; i < indicesList.size(); i++) {
            indices[i] = indicesList.get(i);
        }

        return indices;
    }

    private static List<float[]> generateTangentsAndBitangents(float[] positions, float[] texCoords, int[] indices) {
        int vertexCount = positions.length / 3;  // Each vertex has 3 components (x, y, z)
        Vector3f[] tangents = new Vector3f[vertexCount];
        Vector3f[] bitangents = new Vector3f[vertexCount];

        // Initialize tangent and bitangent vectors for each vertex
        for (int i = 0; i < vertexCount; i++) {
            tangents[i] = new Vector3f(0, 0, 0);
            bitangents[i] = new Vector3f(0, 0, 0);
        }

        // Loop over each triangle using the indices
        for (int i = 0; i < indices.length; i += 3) {
            int i1 = indices[i];
            int i2 = indices[i + 1];
            int i3 = indices[i + 2];

            // Get positions of the triangle's vertices
            Vector3f v0 = new Vector3f(positions[i1 * 3], positions[i1 * 3 + 1], positions[i1 * 3 + 2]);
            Vector3f v1 = new Vector3f(positions[i2 * 3], positions[i2 * 3 + 1], positions[i2 * 3 + 2]);
            Vector3f v2 = new Vector3f(positions[i3 * 3], positions[i3 * 3 + 1], positions[i3 * 3 + 2]);

            // Get texture coordinates of the triangle's vertices
            Vector2f uv0 = new Vector2f(texCoords[i1 * 2], texCoords[i1 * 2 + 1]);
            Vector2f uv1 = new Vector2f(texCoords[i2 * 2], texCoords[i2 * 2 + 1]);
            Vector2f uv2 = new Vector2f(texCoords[i3 * 2], texCoords[i3 * 2 + 1]);

            // Compute the edges of the triangle
            Vector3f edge1 = new Vector3f(v1).sub(v0);
            Vector3f edge2 = new Vector3f(v2).sub(v0);

            // Compute the differences in texture coordinates
            float deltaU1 = uv1.x - uv0.x;
            float deltaV1 = uv1.y - uv0.y;
            float deltaU2 = uv2.x - uv0.x;
            float deltaV2 = uv2.y - uv0.y;

            // Compute the tangent and bitangent
            float f = 1.0f / (deltaU1 * deltaV2 - deltaU2 * deltaV1);

            Vector3f tangent = new Vector3f(
                    f * (deltaV2 * edge1.x - deltaV1 * edge2.x),
                    f * (deltaV2 * edge1.y - deltaV1 * edge2.y),
                    f * (deltaV2 * edge1.z - deltaV1 * edge2.z)
            );
            Vector3f bitangent = new Vector3f(
                    f * (-deltaU2 * edge1.x + deltaU1 * edge2.x),
                    f * (-deltaU2 * edge1.y + deltaU1 * edge2.y),
                    f * (-deltaU2 * edge1.z + deltaU1 * edge2.z)
            );

            // Accumulate tangents and bitangents for each vertex of the triangle
            tangents[i1].add(tangent);
            tangents[i2].add(tangent);
            tangents[i3].add(tangent);

            bitangents[i1].add(bitangent);
            bitangents[i2].add(bitangent);
            bitangents[i3].add(bitangent);
        }

        // Normalize tangents and bitangents for each vertex
        float[] tangentArray = new float[vertexCount * 3];
        float[] bitangentArray = new float[vertexCount * 3];
        for (int i = 0; i < vertexCount; i++) {
            tangents[i].normalize();
            bitangents[i].normalize();

            tangentArray[i * 3] = tangents[i].x;
            tangentArray[i * 3 + 1] = tangents[i].y;
            tangentArray[i * 3 + 2] = tangents[i].z;

            bitangentArray[i * 3] = bitangents[i].x;
            bitangentArray[i * 3 + 1] = bitangents[i].y;
            bitangentArray[i * 3 + 2] = bitangents[i].z;
        }

        return List.of(tangentArray, bitangentArray);  // Return either tangents or bitangents based on your needs
    }

    static class HeightGetters {
        static float getYPositionForBiomeType(int x, int z, PerlinNoise noise, BiomeType biomeType) {
            switch (biomeType) {
                case BEACH -> {
                    return getYPositionForBeach(x, z, noise);
                }

                default -> {
                    return 0;
                }
            }
        }

        private static float getYPositionForBeach(int x, int z, PerlinNoise noise) {
            return noise.getComposedNoiseAt(x, z) * BEACH_NOISE_MULTIPLIER;
        }
    }
}
