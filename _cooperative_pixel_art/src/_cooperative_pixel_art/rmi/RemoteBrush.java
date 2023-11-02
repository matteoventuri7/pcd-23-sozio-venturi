package _cooperative_pixel_art.rmi;

import _cooperative_pixel_art.core.Brush;
import _cooperative_pixel_art.core.IBrush;

public class RemoteBrush extends Brush implements IRemoteBrush {
    private final String host;

    public RemoteBrush(IBrush brush, String host) {
        super(brush.getId(), brush.getName(), brush.getX(), brush.getY(), brush.getColor());
        this.host=host;
    }

    @Override
    public String getHost() {
        return host;
    }
}
