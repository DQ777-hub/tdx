package com.tdx;

import javafx.geometry.Point2D;
import com.tdx.Bloon;
import com.tdx.Bloon.BloonType;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores game settings and wave data.
 * Game settingsw and wave data. 
 */
public class GameConfig {

    /** Game path that bloons follow, the journey from start to finish. */
    public static final List<Point2D> PATH = List.of(
            new Point2D(120, 990),
            new Point2D(432, 990),
            new Point2D(432, 396),
            new Point2D(1008, 396),
            new Point2D(1008, 756),
            new Point2D(1584, 756),
            new Point2D(1584, 216),
            new Point2D(1872, 216));

    /** Base game width for scaling calculations. */
    public static final double BASE_WIDTH = 1920.0;

    /** Base game height for scaling calculations. */
    public static final double BASE_HEIGHT = 1080.0;

    /** Wave definitions - List of (count, type) for each wave. */
    public static final List<List<Object[]>> WAVES;

    static {
        WAVES = new ArrayList<>();

        List<Object[]> w1 = new ArrayList<>();
        w1.add(new Object[] { 12, BloonType.RED });
        WAVES.add(w1);

        List<Object[]> w2 = new ArrayList<>();
        w2.add(new Object[] { 6, BloonType.RED });
        w2.add(new Object[] { 7, BloonType.GREEN });
        WAVES.add(w2);

        List<Object[]> w3 = new ArrayList<>();
        w3.add(new Object[] { 8, BloonType.GREEN });
        WAVES.add(w3);

        List<Object[]> w4 = new ArrayList<>();
        w4.add(new Object[] { 25, BloonType.RED });
        w4.add(new Object[] { 12, BloonType.GREEN });
        w4.add(new Object[] { 8, BloonType.GREEN });
        WAVES.add(w4);

        List<Object[]> w5 = new ArrayList<>();
        w5.add(new Object[] { 13, BloonType.YELLOW });
        WAVES.add(w5);

        List<Object[]> w6 = new ArrayList<>();
        w6.add(new Object[] { 18, BloonType.RED });
        w6.add(new Object[] { 10, BloonType.YELLOW });
        w6.add(new Object[] { 7, BloonType.GREEN });
        WAVES.add(w6);

        List<Object[]> w7 = new ArrayList<>();
        w7.add(new Object[] { 7, BloonType.RUBBER });
        w7.add(new Object[] { 15, BloonType.RED });
        w7.add(new Object[] { 8, BloonType.YELLOW });
        WAVES.add(w7);

        List<Object[]> w8 = new ArrayList<>();
        w8.add(new Object[] { 4, BloonType.CERAMIC });
        w8.add(new Object[] { 12, BloonType.YELLOW });
        w8.add(new Object[] { 10, BloonType.GREEN });
        WAVES.add(w8);

        List<Object[]> w9 = new ArrayList<>();
        w9.add(new Object[] { 30, BloonType.RED });
        w9.add(new Object[] { 4, BloonType.CERAMIC });
        w9.add(new Object[] { 6, BloonType.RUBBER });
        WAVES.add(w9);

        List<Object[]> w10 = new ArrayList<>();
        w10.add(new Object[] { 18, BloonType.YELLOW });
        w10.add(new Object[] { 8, BloonType.RUBBER });
        w10.add(new Object[] { 6, BloonType.CERAMIC });
        WAVES.add(w10);

        List<Object[]> w11 = new ArrayList<>();
        w11.add(new Object[] { 7, BloonType.CERAMIC });
        w11.add(new Object[] { 13, BloonType.YELLOW });
        w11.add(new Object[] { 25, BloonType.RED });
        WAVES.add(w11);

        List<Object[]> w12 = new ArrayList<>();
        w12.add(new Object[] { 1, BloonType.BOSS });
        w12.add(new Object[] { 40, BloonType.RED });
        w12.add(new Object[] { 20, BloonType.YELLOW });
        w12.add(new Object[] { 15, BloonType.CERAMIC });
        w12.add(new Object[] { 8, BloonType.RUBBER });
        WAVES.add(w12);
    }

    /** Prevent instantiation. */
    private GameConfig() {
    }
}
