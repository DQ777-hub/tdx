package com.Towers;

import javafx.scene.paint.Color;

public abstract class CliffTower extends Tower {
    protected CliffTower(String name, int cost, double x, double y, double range, Color color, int maxLevel,
            int upgradeCost, double scale) {
        super(name, cost, x, y, range, color, TowerSurface.CLIFF, maxLevel, upgradeCost, scale);
    }
}
