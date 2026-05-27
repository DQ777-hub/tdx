package com.Towers;

import com.tdx.Bloon;
import com.tdx.Projectile;
import javafx.scene.paint.Color;
import java.util.List;

public class BasicCliffTower extends CliffTower {
    private final double baseFireRate = 1.0;
    private final int baseDamage = 2;

    public BasicCliffTower(double x, double y, double scale) {
        super("Cliff Archer", 180, x, y, 140, Color.web("#5ac8fa"), 3, 150, scale);
    }

    @Override
    public void update(double elapsedSeconds, List<Bloon> bloons, List<Projectile> projectiles) {
        cooldownSeconds -= elapsedSeconds;
        if (cooldownSeconds > 0) {
            return;
        }

        Bloon target = findTarget(bloons);
        if (target != null) {
            Projectile shot = new Projectile(x, y, target, getDamage());
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

    @Override
    protected void onUpgrade() {
        // cliff archer increases range slightly each level as well
        range += 10;
    }
}
