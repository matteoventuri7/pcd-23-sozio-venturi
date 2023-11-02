package _cooperative_pixel_art.rmi;

import java.io.Serializable;
import java.util.UUID;

public class Pixel implements IPixel, ISender, Serializable {
    private final int x;
    private final int y;
    private final int color;
    private final UUID senderId;

    public Pixel(int x, int y, int color, UUID senderId) {
        this.x=x;
        this.y=y;
        this.color=color;
        this.senderId =senderId;
    }

    @Override
    public UUID getSenderId() {
        return senderId;
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
