package _cooperative_pixel_art.rmi;

import _cooperative_pixel_art.core.*;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

abstract class RmiBrushManager extends BaseBrushManager {

    private ExecutorService executorService;

    protected RmiBrushManager(IBrush localBrush) {
        super(localBrush);
        executorService = Executors.newVirtualThreadPerTaskExecutor();
    }

    protected IBrush GetLocalCopyBrush(IBrush brush) {
        var optBrush = brushes.stream()
                .filter(b -> b.equals(brush))
                .findFirst();
        var brushLocalCopy = optBrush.get();
        return brushLocalCopy;
    }

    protected void EnqueueAction(Runnable command){
        executorService.execute(command);
    }
}

class RmiServerBrushManager extends RmiBrushManager implements IRemoteBrushManager {
    private final String localHost;
    private final String serviceName;
    Map<UUID, IRemoteBrushManager> peers;
    List<BaseMessage> historyEvents;

    public RmiServerBrushManager(Brush localBrush, String localhost, String serviceName) {
        super(localBrush);
        this.serviceName = serviceName;
        this.localHost = localhost;

        historyEvents = new ArrayList<>();
        peers = new ConcurrentHashMap<>();
    }

    private List<IRemoteBrushManager> getRemotePeersExcept(ISender senderObject) {
        return peers.entrySet()
                .stream()
                .filter(e -> !e.getKey().equals(senderObject.getSenderId()))
                .map(Map.Entry::getValue).toList();
    }

    @Override
    public void run() {
        try {
            var localRegistry = LocateRegistry.getRegistry();
            UnicastRemoteObject.exportObject(this, 0);
            localRegistry.rebind(serviceName, this);

            addBrush(localBrush);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void updatePosition(int x, int y) {
        super.updatePosition(x, y);
        try {
            updatePositionRemote(new RemoteBrush(localBrush, localHost));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updatePixel(int x, int y, int color) {
        try {
            updatePixelRemote(new Pixel(x, y, color, localBrush.getId()));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addBrush(IBrush brush) {
        super.addBrush(brush);

        try {
            addBrushRemote(new RemoteBrush(brush, localHost));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeBrush(IBrush brush) {
        try {
            removeBrushRemote(new RemoteBrush(brush, localHost));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws Exception {

    }

    // REMOTE METHODS CALLED BY PEERS

    @Override
    public void updatePositionRemote(IRemoteBrush brush) throws RemoteException {
        if (!brush.equals(localBrush)) {
            GetLocalCopyBrush(brush).update(brush);
            view.refresh();
        }

        for (IRemoteBrushManager remotePeer : getRemotePeersExcept(brush)) {
            EnqueueAction(() -> {
                try {
                    System.out.println("Sending position " + brush.getX() + "," + brush.getY());
                    remotePeer.updatePositionRemote(brush);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            });
        }
    }


    @Override
    public void updatePixelRemote(IPixel pixel) throws RemoteException {
        super.updatePixel(pixel.getX(), pixel.getY(), pixel.getColor());

        for (IRemoteBrushManager remotePeer : getRemotePeersExcept(pixel)) {
            EnqueueAction(() -> {
                try {
                    remotePeer.updatePixelRemote(pixel);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            });
        }
    }


    @Override
    public IHistory addBrushRemote(IRemoteBrush brush) throws RemoteException {
        if (!brush.equals(localBrush))
            super.addBrush(brush);

        for (IRemoteBrushManager remotePeer : getRemotePeersExcept(brush)) {
            EnqueueAction(() -> {
                try {
                    remotePeer.addBrushRemote(brush);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            });
        }

        if (!brush.equals(localBrush)) {
            // add to peers
            try {
                peers.put(brush.getId(), (IRemoteBrushManager) LocateRegistry.getRegistry(brush.getHost()).lookup(brush.getId().toString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return new History(brushes.stream().toList(), grid);
    }


    @Override
    public void removeBrushRemote(IRemoteBrush brush) throws RemoteException {
        super.removeBrush(brush);

        peers.remove(brush.getId());
        for (IRemoteBrushManager remotePeer : getRemotePeersExcept(brush)) {
            EnqueueAction(() -> {
                try {
                    remotePeer.removeBrushRemote(brush);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            });
        }
    }


}

class RmiClientBrushManager extends RmiBrushManager implements IRemoteBrushManager {
    final String remoteHost, localHost;
    private final String remoteServiceName;
    private IRemoteBrushManager clientPrincipalStub;

    public RmiClientBrushManager(Brush localBrush, String remoteHost, String remoteServiceName, String localHost) {
        super(localBrush);
        this.remoteServiceName = remoteServiceName;
        this.remoteHost = remoteHost;
        this.localHost = localHost;
    }

    @Override
    public void run() {
        try {
            // CLIENT
            var remoteRegistry = LocateRegistry.getRegistry(remoteHost);
            clientPrincipalStub = (IRemoteBrushManager) remoteRegistry.lookup(remoteServiceName);

            // SERVER
            var localRegistry = LocateRegistry.getRegistry();
            UnicastRemoteObject.exportObject(this, 0);
            localRegistry.rebind(localBrush.getId().toString(), this);

            addBrush(localBrush);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws Exception {

    }

    @Override
    public void addBrush(IBrush brush) {
        super.addBrush(brush);

        EnqueueAction(() -> {
            try {
                var history = clientPrincipalStub.addBrushRemote(new RemoteBrush(brush, localHost));

                for (int x = 0; x < grid.getNumRows(); x++)
                    for (int y = 0; y < grid.getNumColumns(); y++)
                        grid.set(x, y, history.getGrid().get(x, y));

                this.brushes.addAll(history.getBrushes());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void removeBrush(IBrush brush) {
        super.removeBrush(brush);

        EnqueueAction(() -> {
            try {
                clientPrincipalStub.removeBrushRemote(new RemoteBrush(brush, localHost));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void updatePosition(int x, int y) {
        super.updatePosition(x, y);

        EnqueueAction(() -> {
            try {
                clientPrincipalStub.updatePositionRemote(new RemoteBrush(localBrush, localHost));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void updatePixel(int x, int y, int color) {
        super.updatePixel(x, y, color);

        EnqueueAction(() -> {
            try {
                clientPrincipalStub.updatePixelRemote(new Pixel(x, y, color, localBrush.getId()));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    // REMOTE METHODS CALLED BY PRINCIPAL SERVER

    @Override
    public void updatePositionRemote(IRemoteBrush brush) throws RemoteException {
        GetLocalCopyBrush(brush).update(brush);
        System.out.println("Received position " + brush.getX() + "," + brush.getY());
        view.refresh();
    }

    @Override
    public void updatePixelRemote(IPixel pixel) throws RemoteException {
        super.updatePixel(pixel.getX(), pixel.getY(), pixel.getColor());
    }

    @Override
    public IHistory addBrushRemote(IRemoteBrush brush) throws RemoteException {
        super.addBrush(brush);
        return null;
    }

    @Override
    public void removeBrushRemote(IRemoteBrush brush) throws RemoteException {
        super.removeBrush(brush);
    }
}
