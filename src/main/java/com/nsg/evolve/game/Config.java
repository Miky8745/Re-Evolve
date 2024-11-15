package com.nsg.evolve.game;

public class Config {
    public static final float MOUSE_SENSITIVITY = 0.1f;

    public static class Terrain {
        public static final int BEACH_NOISE_MULTIPLIER = 50; //15;
    }

    public static class Character {
        public static final float MOVEMENT_SPEED = 0.001f;

        public static final float CHARACTER_HEIGHT = 1.8f;

        public static final float JUMP_STRENGTH = 7;
    }

    @Test
    public static final int TERRAIN_SIZE = 50;
}
