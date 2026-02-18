package com.cedarxuesong.translate_allinone.gui.configui.model;

public final class UiRect {
    public final int x;
    public final int y;
    public final int width;
    public final int height;

    public UiRect(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int right() {
        return x + width;
    }

    public int bottom() {
        return y + height;
    }

    public boolean contains(double px, double py) {
        return px >= x && px <= right() && py >= y && py <= bottom();
    }
}
