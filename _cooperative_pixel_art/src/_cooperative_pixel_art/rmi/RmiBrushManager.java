package _cooperative_pixel_art.rmi;

import _cooperative_pixel_art.core.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RmiBrushManager extends BaseBrushManager {
    private final boolean isServer;
    private List<BaseMessage> historyEvents;
    private ExecutorService executorService;

    public RmiBrushManager(Brush localBrush, boolean isServer) {
        super(localBrush);
        this.isServer = isServer;

        if(isServer){
            configureServer();
        }
    }

    private void configureServer() {
        executorService = Executors.newFixedThreadPool(1);
        historyEvents = new ArrayList<>();
    }

    @Override
    public void run() {

    }

    @Override
    public void close() throws Exception {

    }

    @Override
    protected void addBrush(Brush brush) {
        super.addBrush(brush);
    }

    @Override
    protected void removeBrush(Brush brush) {
        super.removeBrush(brush);
    }

    @Override
    public void updatePosition(int x, int y) {
        super.updatePosition(x, y);
    }

    @Override
    public void updatePixel(int x, int y, int color) {
        super.updatePixel(x, y, color);
    }
}
