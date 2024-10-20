package com.nsg.evolve.engine.gui;

import com.nsg.evolve.engine.utilities.Utilities;
import org.joml.Vector2f;

public class Button extends QuadGenerator.Quad {

    public Button(Vector2f min, Vector2f max, int layer) {
        super(min, max, layer);
    }

    public boolean click(Vector2f mousePos) {
        mousePos = Utilities.screenToNDC(mousePos, width, height);

        if (isMouseInBoundaryBox(mousePos)) {
            System.out.println("Button clicked");
            return true;
        }

        return false;
    }

    private boolean isMouseInBoundaryBox(Vector2f mousePos) {
        return (mousePos.x > min.x && mousePos.y < min.y) && (mousePos.x < max.x && mousePos.y > max.y);
    }
}
