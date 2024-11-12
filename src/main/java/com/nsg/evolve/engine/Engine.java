package com.nsg.evolve.engine;

import com.nsg.evolve.engine.interfaces.IAppLogic;
import com.nsg.evolve.engine.render.Render;
import com.nsg.evolve.engine.scene.Scene;
import com.nsg.evolve.game.terraingen.BiomeType;

import static com.nsg.evolve.engine.Time.MILLISECOND;

/**
 * The ENGINE
 */
public class Engine {

    public static final int TARGET_UPS = 50;
    private final IAppLogic appLogic;
    private final Window window;
    private Render render;
    private boolean running;
    private Scene scene;
    private int targetFps;
    private int targetUps;

    public Engine(String windowTitle, Window.WindowOptions opts, IAppLogic appLogic, BiomeType biomeType) {
        window = new Window(windowTitle, opts, () -> {
            resize();
            return null;
        });
        targetFps = opts.fps;
        targetUps = opts.ups;
        this.appLogic = appLogic;
        render = new Render(window);
        scene = new Scene(window.getWidth(), window.getHeight(), biomeType);
        appLogic.init(window, scene, render);
        running = true;
    }

    private void cleanup() {
        appLogic.cleanup();
        render.cleanup();
        window.cleanup();
    }

    private void resize() {
        int width = window.getWidth();
        int height = window.getHeight();
        scene.resize(width, height);
        render.resize(width, height);
    }

    private void run() {
        long initialTime = System.currentTimeMillis();
        float timeU = (float) MILLISECOND / targetUps;
        float timeR = targetFps > 0 ? (float) MILLISECOND / targetFps : 0;
        float deltaUpdate = 0;
        float deltaFps = 0;

        long updateTime = initialTime;

        while (running && !window.windowShouldClose()) {
            window.pollEvents();

            long now = System.currentTimeMillis();
            deltaUpdate += (now - initialTime) / timeU;
            deltaFps += (now - initialTime) / timeR;

            if (targetFps <= 0 || deltaFps >= 1) {
                window.getMouseInput().input(window.getWindowHandle());
                appLogic.input(window, scene);
            }

            if (deltaUpdate >= 1) {
                Time.deltaTimeMillis = now - updateTime;
                Time.deltaTime = Time.deltaTimeMillis * Time.MILLISECOND_FRACTION;
                appLogic.update(window, scene);
                updateTime = now;
                deltaUpdate--;
            }

            if (targetFps <= 0 || deltaFps >= 1) {
                render.render(window, scene);
                deltaFps--;
                window.update();
            }
            initialTime = now;
        }

        cleanup();
    }

    public void start() {
        running = true;
        run();
    }

    public void stop() {
        running = false;
    }
}