package com.nsg.evolve.engine.noise;

import org.joml.Vector2f;

import static org.joml.Math.*;

// From examples from ZIPPED
public class PerlinNoise {
    private static long seedHalf = 8020463840L;

    private float[][] pixelValues;
    private final int gridSize;
    private final long seed;

    private PerlinNoise(int sizeX, int sizeY, int gridSize) {
        pixelValues = new float[sizeX][sizeY];
        this.gridSize = gridSize;
        seed = newSeed();
    }

    private PerlinNoise(int sizeX, int sizeY, int gridSize, long seed) {
        pixelValues = new float[sizeX][sizeY];
        this.gridSize = gridSize;
        this.seed = seed;
    }

    // From joml Random
    public static long newSeed() {
        long oldSeedHalf = seedHalf;
        long newSeedHalf = oldSeedHalf * 3512401965023503517L;
        seedHalf = newSeedHalf;

        return newSeedHalf;
    }

    public static PerlinNoise generateNoise(int sizeX, int sizeY, int gridSize, int octaves) {
        PerlinNoise noise = new PerlinNoise(sizeX, sizeY, gridSize);

        generateNoiseForInstance(noise, octaves);

        return noise;
    }

    public static PerlinNoise generateNoise(int sizeX, int sizeY, int gridSize, int octaves, long seed) {
        PerlinNoise noise = new PerlinNoise(sizeX, sizeY, gridSize, seed);

        generateNoiseForInstance(noise, octaves);

        return noise;
    }

    private static void generateNoiseForInstance(PerlinNoise noise, int octaves) {
        for (int i = 0; i < noise.pixelValues.length; i++) {
            for (int j = 0; j < noise.pixelValues[0].length; j++) {

                float value = 0;

                float frequency = 1;
                float amplitude = 1;

                for (int k = 0; k < octaves; k++) {
                    value += noise.perlinSampleAt(i * frequency / noise.gridSize, j * frequency / noise.gridSize) * amplitude;

                    frequency *=2;
                    amplitude /= 2;
                }

                noise.pixelValues[i][j] = value;
            }
        }
    }

    public float getComposedNoiseAt(int x, int y) {
        return pixelValues[x][y];
    }

    private float perlinSampleAt(float x, float y) {
        int x0 = (int) x;
        int y0 = (int) y;
        int x1 = x0 + 1;
        int y1 = y0 + 1;

        float sx = x - (float) x0;
        float sy = y - (float) y0;

        float n0 = dotGridGradient(x0, y0, x, y);
        float n1 = dotGridGradient(x1, y0, x, y);
        float ix0 = interpolate(n0, n1, sx);

        n0 = dotGridGradient(x0, y1, x, y);
        n1 = dotGridGradient(x1, y1, x, y);
        float ix1 = interpolate(n0, n1, sx);

        return interpolate(ix0, ix1, sy);
    }

    private float dotGridGradient(int ix, int iy, float x, float y) {
        Vector2f gradient = randomGradient(ix, iy);

        float dx = x - (float) ix;
        float dy = y - (float) iy;

        return dx * gradient.x + dy * gradient.y;
    }

    private Vector2f randomGradient(int ix, int iy) {
        final int w = 32;
        final int s = w / 2;

        long a = ix + seed;
        long b = iy + seed;

        a *= 3284157443L;
        b ^= (a << s) | (a >>> (w-s));
        b *= 1911520717L;
        a ^= (b << s) | (b >>> (w-s));
        a *= 2048419325L;

        float random = (float) (a * (PI / (double) (~(~0 >>> 1))));

        return new Vector2f(sin(random), cos(random));
    }

    private float interpolate(float a0, float a1, float w) {
        return (a1 - a0) * (3.0f - w * 2.0f) * w * w + a0;
    }

    public int getGridSize() {
        return gridSize;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (float[] x : pixelValues) {
            for (float y : x) {
                builder.append(y).append(", ");
            }
            builder.append("\n");
        }

        return builder.toString();
    }
}
