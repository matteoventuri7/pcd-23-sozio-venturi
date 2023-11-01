package _cooperative_pixel_art.core;

import java.util.UUID;

public class UpdatePixelMessage extends BaseMessage {
    private final int x;
    private final int y;
    private final int color;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getColor() {
        return color;
    }

    public UpdatePixelMessage(UUID senderId, int x, int y, int color) {
        super(senderId);
        this.x = x;
        this.y = y;
        this.color = color;
    }
}
