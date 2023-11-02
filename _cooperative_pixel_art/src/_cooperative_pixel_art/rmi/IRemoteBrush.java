package _cooperative_pixel_art.rmi;

import _cooperative_pixel_art.core.IBrush;

public interface IRemoteBrush extends IBrush, ISender {
    String getHost();
}
