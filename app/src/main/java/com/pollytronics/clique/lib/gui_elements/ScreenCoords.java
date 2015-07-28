package com.pollytronics.clique.lib.gui_elements;

/**
 * Created by pollywog on 7/28/15.
 */
public class ScreenCoords {
    private float x = 0, y = 0;

    public ScreenCoords(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() { return x; }

    public void setX(float x) { this.x = x; }

    public float getY() { return y; }

    public void setY(float y) { this.y = y; }
}
