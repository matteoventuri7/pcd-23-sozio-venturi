package _cooperative_pixel_art.rmi;

import java.rmi.Remote;

public interface IPixel {
    int getX();
    int getY();
    int getColor();
}
