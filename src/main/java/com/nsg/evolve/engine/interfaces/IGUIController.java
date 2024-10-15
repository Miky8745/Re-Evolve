package com.nsg.evolve.engine.interfaces;

import java.util.List;

@FunctionalInterface
public interface IGUIController {
    List<IGUIElement> getActiveElements();
}
