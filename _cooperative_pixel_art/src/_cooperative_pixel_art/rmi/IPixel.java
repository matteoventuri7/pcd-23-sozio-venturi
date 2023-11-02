package _cooperative_pixel_art.rmi;

import java.rmi.Remote;
import java.util.UUID;

public interface IPixel extends ISender {
    UUID getSenderId();
    int getX();
    int getY();
    int getColor();
}
