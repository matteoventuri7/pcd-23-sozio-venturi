package _cooperative_pixel_art.core;

import java.io.Serializable;

abstract class BrushMessage implements Serializable {
    protected final static String COMMAND_CREATE = "CREATE";
    protected final static String COMMAND_REMOVE = "REMOVE";
    protected final static String COMMAND_UPDATE = "UPDATE";
    private final String command;

    public String getCommand() {
        return command;
    }

    public BrushManager.Brush getBrush() {
        return brush;
    }

    private final BrushManager.Brush brush;

    public BrushMessage(String command, BrushManager.Brush brush){
        this.command=command;
        this.brush = brush;
    }
}

class CreateBrushMessage extends BrushMessage{
    public CreateBrushMessage(BrushManager.Brush brush) {
        super(COMMAND_CREATE, brush);
    }
}

class RemoveBrushMessage extends BrushMessage{
    public RemoveBrushMessage(BrushManager.Brush brush) {
        super(COMMAND_REMOVE, brush);
    }
}

class UpdateBrushMessage extends BrushMessage{
    public UpdateBrushMessage(BrushManager.Brush brush) {
        super(COMMAND_UPDATE, brush);
    }
}

class UpdatePixelMessage implements Serializable {
    private final int x;
    private final int y;
    private final int color;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getColor() {
        return color;
    }

    public UpdatePixelMessage(int x, int y, int color){
        this.x=x;
        this.y=y;
        this.color=color;
    }
}