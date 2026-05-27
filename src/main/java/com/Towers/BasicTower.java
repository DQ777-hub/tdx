package com.Towers;

import com.tdx.Bloon;
import com.tdx.Projectile;
import javafx.scene.paint.Color;
import java.util.List;

public class BasicTower extends GroundTower {
    private final double baseFireRate = 1.2;
    private final int baseDamage = 1;
    private boolean piercing = false;

    public BasicTower(double x, double y, double scale) {
        super("Dart Monkey", 150, x, y, 120, Color.web("#f9d71c"), 3, 120, scale);
    }

    @Override
    public void update(double elapsedSeconds, List<Bloon> bloons, List<Projectile> projectiles) {
        cooldownSeconds -= elapsedSeconds;
        if (cooldownSeconds > 0) {
            return;
        }

        Bloon target = findTarget(bloons);
        if (target != null) {
            Projectile shot = new Projectile(x, y, target, getDamage(), piercing);
            projectiles.add(shot);
            cooldownSeconds = 1.0 / getFireRate();
        }
    }

    private int getDamage() {
        return baseDamage + level - 1;
    }

    private double getFireRate() {
        return baseFireRate + (level - 1) * 0.2;
    }

    public int getDisplayDamage() {
        return getDamage();
    }

    public double getDisplayFireRate() {
        return getFireRate();
    }

    public boolean isPiercing() {
        return piercing;
    }

    public void setPiercing(boolean piercing) {
        this.piercing = piercing;
    }

    @Override
    protected void onUpgrade() {
        range += 10;
        // Enable piercing at max level
        if (level >= maxLevel) {
            piercing = true;
        }
    }
}
