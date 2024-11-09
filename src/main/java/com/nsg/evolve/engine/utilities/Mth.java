package com.nsg.evolve.engine.utilities;

import org.joml.Vector3f;

/**
 * Class for Maths
 */
public class Mth {

    /**
     * Simple interpolation x(t) = (x(1)-x(0)) * t + x(0)
     * @param start absolute value of x(0)
     * @param end absolute value of x(1)
     * @param point t
     * @return x(t)
     */

    public static float simpleInterpolation(float start, float end, float point) {
        float line = end - start;

        float baseValue = line * point;

        return baseValue + start;
    }

    /**
     * Function for absolute value of number x
     * @return Absolute value of x
     */
    public static float abs(float x) {
        return x >= 0 ? x : x*-1;
    }

    public static Vector3f multiply(Vector3f v, float f) {
        return new Vector3f(v.x * f, v.y * f, v.z * f);
    }

    public static Vector3f add(Vector3f v, Vector3f v2) {
        return new Vector3f(v.x + v2.x, v.y + v2.y, v.z + v2.z);
    }
}
