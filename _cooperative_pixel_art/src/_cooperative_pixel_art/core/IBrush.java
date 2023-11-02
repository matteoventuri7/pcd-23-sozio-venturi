package _cooperative_pixel_art.core;

import java.rmi.Remote;
import java.util.UUID;

public interface IBrush extends Remote {
    void updatePosition(final int x, final int y);
    int getX();
    int getY();
    int getColor();
    String getName();
    UUID getId();
    void update(IBrush brush);
}
