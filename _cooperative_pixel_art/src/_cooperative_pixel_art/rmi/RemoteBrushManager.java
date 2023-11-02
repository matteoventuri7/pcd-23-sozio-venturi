package _cooperative_pixel_art.rmi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class RemoteBrushManager implements IRemoteBrushManager {
    private final RmiBrushManager brushManager;

    public RemoteBrushManager(RmiBrushManager brushManager) {
        this.brushManager = brushManager;
    }

    @Override
    public void updatePosition(IPosition position) {
        //brushManager.updatePosition(position.getX(), position.getY());

        if (!brushManager.iAmPrincipal) {
            try {
                brushManager.principalStub.updatePosition(position);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            for (IRemoteBrushManager remotePeer : brushManager.peers.values()) {
                brushManager.executorService.execute(()->{
                    try {
                        remotePeer.updatePosition(position);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    @Override
    public void updatePixel(IPixel pixel) {
        //brushManager.updatePixel(pixel.getX(), pixel.getY(), pixel.getColor());

        if (!brushManager.iAmPrincipal) {
            try {
                brushManager.principalStub.updatePixel(pixel);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            for (IRemoteBrushManager remotePeer : brushManager.peers.values()) {
                brushManager.executorService.execute(()->{
                    try {
                        remotePeer.updatePixel(pixel);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    @Override
    public void addBrush(IRemoteBrush brush) {
        //brushManager.addBrush(brush);

        if (!brushManager.iAmPrincipal) {
            try {
                brushManager.principalStub.addBrush(brush);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            for (IRemoteBrushManager remotePeer : brushManager.peers.values()) {
                brushManager.executorService.execute(()->{
                    try {
                        remotePeer.addBrush(brush);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                });
            }
            // add to peers
            try {
                brushManager.peers.put(brush.getId(), (IRemoteBrushManager) LocateRegistry.getRegistry(brush.getHost()).lookup(brush.getId().toString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void removeBrush(IRemoteBrush brush) {
        brushManager.removeBrush(brush);

        if (!brushManager.iAmPrincipal) {
            try {
                brushManager.principalStub.removeBrush(brush);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            brushManager.peers.remove(brush.getId());
            for (IRemoteBrushManager remotePeer : brushManager.peers.values()) {
                brushManager.executorService.execute(()->{
                    try {
                        remotePeer.removeBrush(brush);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }
}
