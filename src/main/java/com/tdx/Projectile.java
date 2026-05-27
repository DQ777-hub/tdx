package com.tdx;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.util.ArrayList;
import java.util.List;

public class Projectile {
    private final Node view; // Can be ImageView or Circle
    private final Bloon target;
    private final int damage;
    private final double speed = 320;
    private double x;
    private double y;
    private final boolean piercing;
    private final List<Bloon> hitTargets = new ArrayList<>();

    public Projectile(double startX, double startY, Bloon target, int damage) {
        this(startX, startY, target, damage, false);
    }

    public Projectile(double startX, double startY, Bloon target, int damage, boolean piercing) {
        this.target = target;
        this.damage = damage;
        this.piercing = piercing;
        this.x = startX;
        this.y = startY;

        // Load dart image from resources
        Image dartImage = null;
        try {
            dartImage = new Image(getClass().getResourceAsStream("/Medias/Image/dart.png"));
        } catch (Exception e) {
            // Fallback if image not found
            System.err.println("Warning: dart.png not found, using fallback circle");
        }

        if (dartImage != null && !dartImage.isError()) {
            ImageView imageView = new ImageView(dartImage);
            imageView.setFitWidth(20);
            imageView.setFitHeight(20);
            imageView.setPreserveRatio(true);
            imageView.setX(startX - 10);
            imageView.setY(startY - 10);
            this.view = imageView;
        } else {
            // Create a fallback circle if image fails to load
            Circle circle = new Circle(5, Color.web("#ffffff"));
            circle.setCenterX(startX);
            circle.setCenterY(startY);
            circle.setStroke(Color.web("#ffcc00"));
            circle.setStrokeWidth(1.5);
            this.view = circle;
        }
    }

    public Node getView() {
        return view;
    }

    private void updateRotation(double targetX, double targetY) {
        double dx = targetX - x;
        double dy = targetY - y;
        double angle = Math.atan2(dy, dx);
        double degrees = Math.toDegrees(angle);

        if (view instanceof ImageView) {
            ((ImageView) view).setRotate(degrees);
        }
    }

    public boolean update(double elapsedSeconds) {
        if (!target.isAlive() || target.hasReachedEnd()) {
            return true;
        }

        double targetX = target.getX();
        double targetY = target.getY();

        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.hypot(dx, dy);

        // Update rotation to face target
        updateRotation(targetX, targetY);

        // Collision detection: hit if distance is within 20 pixels (accounts for
        // projectile and bloon size)
        if (distance < 20) {
            if (!hitTargets.contains(target)) {
                hitTargets.add(target);
                target.applyDamage(damage);
            }

            if (!piercing) {
                return true; // Non-piercing projectile is done
            } else {
                return false; // Piercing projectile continues
            }
        }

        double step = Math.min(distance, speed * elapsedSeconds);
        x += dx / distance * step;
        y += dy / distance * step;

        // Update the visual representation
        if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;
            imageView.setX(x - 10);
            imageView.setY(y - 10);
        } else if (view instanceof Circle) {
            Circle circle = (Circle) view;
            circle.setCenterX(x);
            circle.setCenterY(y);
        }

        return false;
    }
}
