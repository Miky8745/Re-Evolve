package com.nsg.evolve.engine.interfaces;

import com.nsg.evolve.engine.Window;
import com.nsg.evolve.engine.render.Render;
import com.nsg.evolve.engine.scene.Scene;

public interface IAppLogic {

    void cleanup();

    void init(Window window, Scene scene, Render render);

    void input(Window window, Scene scene);

    void update(Window window, Scene scene);
}