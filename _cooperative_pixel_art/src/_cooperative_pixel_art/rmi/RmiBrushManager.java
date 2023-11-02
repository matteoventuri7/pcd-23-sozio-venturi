package _cooperative_pixel_art.rmi;

import _cooperative_pixel_art.core.*;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RmiBrushManager extends BaseBrushManager {
    boolean iAmPrincipal;
    final String remoteHost, localHost;
    List<BaseMessage> historyEvents;
    ExecutorService executorService;
    Registry localRegistry, remoteRegistry;
    Map<UUID, IRemoteBrushManager> peers;
    IRemoteBrushManager principalBrushManager, principalStub, clientPrincipalStub, localServerStub, localBrushManager;

    public RmiBrushManager(Brush localBrush, String remoteHost, String localHost, boolean iAmPrincipal) {
        super(localBrush);
        this.remoteHost = remoteHost;
        this.localHost = localHost;
        this.iAmPrincipal = iAmPrincipal;

        if (iAmPrincipal) {
            configureServer();
        }
    }

    private void configureServer() {
        executorService = Executors.newVirtualThreadPerTaskExecutor();
        historyEvents = new ArrayList<>();
        peers = new ConcurrentHashMap<>();
    }

    @Override
    public void run() {
        try {
            localRegistry = LocateRegistry.getRegistry();

            if (iAmPrincipal) {
                // PRINCIPAL
                principalBrushManager = new RemoteBrushManager(this);
                principalStub = (IRemoteBrushManager) UnicastRemoteObject.exportObject(principalBrushManager, 0);
                localRegistry.rebind(IRemoteBrushManager.remoteName, principalStub);
            } else {
                // CLIENT
                remoteRegistry = LocateRegistry.getRegistry(remoteHost);
                clientPrincipalStub = (IRemoteBrushManager) remoteRegistry.lookup(IRemoteBrushManager.remoteName);
            }

            // SERVER
            localBrushManager = new RemoteBrushManager(this);
            localServerStub = (IRemoteBrushManager) UnicastRemoteObject.exportObject(localBrushManager, 0);
            localRegistry.rebind(localBrush.getId().toString(), localServerStub);

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

        if (iAmPrincipal) {
            try {
                principalBrushManager.addBrush(new RemoteBrush(brush, localHost));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            try {
                clientPrincipalStub.addBrush(new RemoteBrush(brush, localHost));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void removeBrush(IBrush brush) {
        super.removeBrush(brush);
        if(iAmPrincipal){
            try {
                principalBrushManager.removeBrush(new RemoteBrush(brush, localHost));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else{
            try {
                clientPrincipalStub.removeBrush(new RemoteBrush(brush, localHost));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void updatePosition(int x, int y) {
        super.updatePosition(x, y);
        view.refresh();

        if(iAmPrincipal){
            try {
                principalBrushManager.updatePosition(new Position(x, y));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else {
            try {
                clientPrincipalStub.updatePosition(new Position(x, y));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void updatePixel(int x, int y, int color) {
        super.updatePixel(x, y, color);
        view.refresh();

        if(iAmPrincipal){
            try {
                principalBrushManager.updatePixel(new Pixel(x, y, color));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else{
            try {
                clientPrincipalStub.updatePixel(new Pixel(x, y, color));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
