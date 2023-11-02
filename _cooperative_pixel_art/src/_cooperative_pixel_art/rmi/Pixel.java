package _cooperative_pixel_art.rmi;

import java.io.Serializable;

public class Pixel implements IPixel, Serializable {
    private final int x;
    private final int y;
    private final int color;

    public Pixel(int x, int y, int color) {
        this.x=x;
        this.y=y;
        this.color=color;
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public int getColor() {
        return this.color;
    }
}
