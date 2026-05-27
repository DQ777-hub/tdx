package com.tdx;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.util.List;

public class Bloon {
    public enum BloonType {
        RED(1, 1.0), GREEN(1, 0.9), YELLOW(2, 1.1), RUBBER(3, 0.8), CERAMIC(4, 0.7), BOSS(8, 0.6);

        public final int baseHealth;
        public final double speedMultiplier;

        BloonType(int baseHealth, double speedMultiplier) {
            this.baseHealth = baseHealth;
            this.speedMultiplier = speedMultiplier;
        }
    }

    private final Circle view;
    private final List<Point2D> path;
    private int pathIndex = 0;
    private final double speed;
    private int health;
    private boolean finished = false;
    private final double scale;
    private static final double baseRadius = 12;
    private final BloonType type;

    public Bloon(List<Point2D> path, double speed, int health, double scale, BloonType type) {
        this.path = path;
        this.type = type;
        this.speed = (speed * scale * type.speedMultiplier); // Scale speed based on type
        this.health = health + type.baseHealth - 1;
        this.scale = scale;

        Point2D start = path.get(0);
        this.view = new Circle(baseRadius * scale, getColorForType());
        this.view.setCenterX(start.getX());
        this.view.setCenterY(start.getY());
        this.view.setStroke(Color.web("#222222"));
        this.view.setStrokeWidth(2 * scale);
    }

    // Old Constructor
    public Bloon(List<Point2D> path, double speed, int health, double scale) {
        this(path, speed, health, scale, BloonType.RED);
    }

    public Circle getView() {
        return view;
    }

    public double getX() {
        return view.getCenterX();
    }

    public double getY() {
        return view.getCenterY();
    }

    public void update(double elapsedSeconds) {
        if (finished || !isAlive() || pathIndex >= path.size() - 1) {
            return;
        }

        Point2D target = path.get(pathIndex + 1);
        Point2D current = new Point2D(getX(), getY());
        Point2D direction = target.subtract(current);
        double distance = direction.magnitude();

        if (distance < speed * elapsedSeconds) {
            view.setCenterX(target.getX());
            view.setCenterY(target.getY());
            pathIndex++;
            if (pathIndex >= path.size() - 1) {
                finished = true;
            }
            return;
        }

        Point2D step = direction.normalize().multiply(speed * elapsedSeconds);
        view.setCenterX(getX() + step.getX());
        view.setCenterY(getY() + step.getY());
    }

    public void applyDamage(int damage) {
        health -= damage;
        // Color stays consistent with bloon type
    }

    public boolean isAlive() {
        return health > 0;
    }

    public boolean hasReachedEnd() {
        return finished;
    }

    private Color getColorForType() {
        switch (type) {
            case RED:
                return Color.web("#ff4d4d");
            case GREEN:
                return Color.web("#66bb6a");
            case YELLOW:
                return Color.web("#ffeb3b");
            case RUBBER:
                return Color.web("#a1887f");
            case CERAMIC:
                return Color.web("#e0e0e0");
            case BOSS:
                return Color.web("#b71c1c");
            default:
                return Color.web("#ff4d4d"); // Default to red
        }
    }

    public BloonType getType() {
        return type;
    }
}
