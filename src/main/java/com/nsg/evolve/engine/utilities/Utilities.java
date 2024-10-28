package com.nsg.evolve.engine.utilities;

import org.joml.Vector2f;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Utilities class
 */
public class Utilities {

    public static String readFile(InputStream stream) {
        StringBuilder builder = new StringBuilder();
        try {
             InputStreamReader inputStreamReader = new InputStreamReader(stream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line).append("\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return builder.toString();
    }

    public static float[] listFloatToArray(List<Float> list) {
        int size = list != null ? list.size() : 0;
        float[] floatArr = new float[size];
        for (int i = 0; i < size; i++) {
            floatArr[i] = list.get(i);
        }
        return floatArr;
    }

    public static int[] listIntToArray(List<Integer> list) {
        return list.stream().mapToInt((Integer v) -> v).toArray();
    }

    public static Vector2f screenToNDC(Vector2f screenPos, int width, int height) {
        Vector2f outVector = new Vector2f();

        outVector.x = (2*screenPos.x) / width - 1;
        outVector.y = 1 - (2*screenPos.y) / height;

        return outVector;
    }
}