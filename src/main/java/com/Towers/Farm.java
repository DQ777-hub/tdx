package com.Towers;

import com.tdx.Bloon;
import com.tdx.Projectile;
import javafx.scene.paint.Color;
import java.util.List;

public class Farm extends GroundTower {
    private final double baseIncomePerSecond = 1.5; // Money per second
    private double incomeTimer = 0;
    private int generatedIncome = 0;

    public Farm(double x, double y, double scale) {
        super("Farm", 1200, x, y, 0, Color.web("#4caf50"), 3, 1000, scale);
        // Farm has 0 range - it doesn't attack
    }

    @Override
    public void update(double elapsedSeconds, List<Bloon> bloons, List<Projectile> projectiles) {
        // Farm generates income over time
        incomeTimer += elapsedSeconds;

        double incomePerSecond = getIncomePerSecond();
        while (incomeTimer >= 1.0) {
            generatedIncome += (int) incomePerSecond;
            incomeTimer -= 1.0;
        }
    }

    public int collectIncome() {
        int income = generatedIncome;
        generatedIncome = 0;
        return income;
    }

    private double getIncomePerSecond() {
        return baseIncomePerSecond + (level - 1) * 0.5;
    }

    public double getDisplayIncome() {
        return getIncomePerSecond();
    }

    @Override
    protected void onUpgrade() {
        // Farm gets better income with upgrades
    }
}
