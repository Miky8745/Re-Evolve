package com.nsg.evolve.engine.utilities;

import java.io.File;

public record ResourceLocation(String path) {

    private static final String resourcesFolder = "src/main/resources/";

    public ResourceLocation(String path) {
        this.path = resourcesFolder + path;

        if (!new File(this.path).exists()) {
            throw new RuntimeException("File " + path + " is not present.");
        }
    }

    public File getFile() {
        return new File(path);
    }
}
