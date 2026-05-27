package com.Towers;

import com.tdx.Bloon;
import com.tdx.Projectile;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.util.List;

public abstract class Tower {
    public enum TowerSurface {
        GROUND, CLIFF
    }

    public final int cost;
    public final String name;
    public final double x;
    public final double y;
    public double range;
    public final TowerSurface surface;
    public int level = 1;
    public final int maxLevel;
    protected int upgradeCost;
    protected double cooldownSeconds = 0;
    protected final Circle view;
    protected final double baseRadius = 18;
    protected final double scale;

    public Tower(String name, int cost, double x, double y, double range, Color color, TowerSurface surface,
            int maxLevel, int upgradeCost, double scale) {
        this.name = name;
        this.cost = cost;
        this.x = x;
        this.y = y;
        this.range = range;
        this.surface = surface;
        this.maxLevel = maxLevel;
        this.upgradeCost = upgradeCost;
        this.scale = scale;
        this.view = new Circle(baseRadius * scale, color);
        this.view.setCenterX(x);
        this.view.setCenterY(y);
        this.view.setStroke(Color.web("#333333"));
        this.view.setStrokeWidth(2);
    }

    public Circle getView() {
        return view;
    }

    public boolean contains(double px, double py) {
        double dx = px - x;
        double dy = py - y;
        return Math.hypot(dx, dy) <= view.getRadius();
    }

    public boolean canUpgrade() {
        return level < maxLevel;
    }

    public int getUpgradeCost() {
        return upgradeCost;
    }

    public boolean upgrade() {
        if (!canUpgrade()) {
            return false;
        }
        level++;
        upgradeCost *= 2;
        range += 15;
        onUpgrade();
        refreshView();
        return true;
    }

    protected void refreshView() {
        view.setRadius((baseRadius + (level - 1) * 3) * scale);
        view.setStrokeWidth((1 + level * 0.5) * scale);
        // Update tower color brightness to show upgrade
        Color current = (Color) view.getFill();
        double brightness = 0.2 + (level - 1) * 0.15;
        view.setFill(current.interpolate(Color.WHITE, brightness));
    }

    protected void onUpgrade() {
    }

    public abstract void update(double elapsedSeconds, List<Bloon> bloons, List<Projectile> projectiles);

    protected Bloon findTarget(List<Bloon> bloons) {
        Bloon closest = null;
        double bestDistance = Double.MAX_VALUE;
        double scaledRange = range * scale;

        for (Bloon bloon : bloons) {
            if (!bloon.isAlive() || bloon.hasReachedEnd()) {
                continue;
            }
            double dx = bloon.getX() - x;
            double dy = bloon.getY() - y;
            double dist = Math.hypot(dx, dy);
            if (dist <= scaledRange && dist < bestDistance) {
                bestDistance = dist;
                closest = bloon;
            }
        }

        return closest;
    }
}
