package com.nsg.evolve.engine.utilities;

import java.io.InputStream;

/**
 * helper class for resources
 */
public class ResourceLocation {
    private final String path;
    private static final String resourcesFolder = "/"; // Root path in the JAR

    public ResourceLocation(String path) {
        this.path = resourcesFolder + path;

        // Verify if the file exists within the JAR by attempting to load it as a resource
        if (getClass().getResourceAsStream(this.path) == null) {
            throw new RuntimeException("File " + path + " is not present in the JAR.");
        }
    }

    public InputStream getFileStream() {
        InputStream inputStream = getClass().getResourceAsStream(path);
        if (inputStream == null) {
            throw new RuntimeException("Failed to load file " + path + " from JAR.");
        }
        return inputStream;
    }
}
