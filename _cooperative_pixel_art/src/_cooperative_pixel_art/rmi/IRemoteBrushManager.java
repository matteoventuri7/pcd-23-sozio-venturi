package _cooperative_pixel_art.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRemoteBrushManager extends Remote {
    final String remoteName = "CooperativePixelArtRmi";
    void updatePosition(IPosition position) throws RemoteException;

    void updatePixel(IPixel pixel) throws RemoteException;
    void addBrush(IRemoteBrush brush) throws RemoteException;
    void removeBrush(IRemoteBrush brush) throws RemoteException;
}
