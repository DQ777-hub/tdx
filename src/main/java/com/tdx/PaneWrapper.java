package com.tdx;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

/**
 * Custom pane with clipping support for the game playfield.
 * Ensures game content stays within bounds
 */
public class PaneWrapper extends Pane {
    private Rectangle clipRect;

    /**
     * Create a new playfield pane with the specified dimensions.
     * 
     * @param width  the initial width
     * @param height the initial height
     */
    public PaneWrapper(double width, double height) {
        setPrefSize(width, height);
        clipRect = new Rectangle(width, height);
        setClip(clipRect);

        // Listen to width/height changes and update clip
        widthProperty().addListener((obs, oldVal, newVal) -> {
            clipRect.setWidth(newVal.doubleValue());
        });
        heightProperty().addListener((obs, oldVal, newVal) -> {
            clipRect.setHeight(newVal.doubleValue());
        });
    }
}
