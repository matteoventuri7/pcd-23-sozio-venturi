package _cooperative_pixel_art.rmi;

import java.io.Serializable;

public class Position implements IPosition, Serializable {
    private final int x;
    private final int y;

    public Position(int x, int y) {
        this.x=x;
        this.y=y;
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }
}
