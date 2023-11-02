package _cooperative_pixel_art.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRemoteBrushManager extends Remote {
    String remoteName = "CooperativePixelArtRmi";
    void updatePositionRemote(IRemoteBrush brush) throws RemoteException;
    void updatePixelRemote(IPixel pixel) throws RemoteException;
    IHistory addBrushRemote(IRemoteBrush brush) throws RemoteException;
    void removeBrushRemote(IRemoteBrush brush) throws RemoteException;
}
