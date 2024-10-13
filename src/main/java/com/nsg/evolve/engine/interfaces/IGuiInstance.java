package com.nsg.evolve.engine.interfaces;

import com.nsg.evolve.engine.Window;
import com.nsg.evolve.engine.scene.Scene;

public interface IGuiInstance {
    void drawGui();

    boolean handleGuiInput(Scene scene, Window window);
}