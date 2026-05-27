package com.Towers;

import javafx.scene.paint.Color;

public abstract class GroundTower extends Tower {
    protected GroundTower(String name, int cost, double x, double y, double range, Color color, int maxLevel,
            int upgradeCost, double scale) {
        super(name, cost, x, y, range, color, TowerSurface.GROUND, maxLevel, upgradeCost, scale);
    }
}
