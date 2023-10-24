package _cooperative_pixel_art.core;

import java.io.Serializable;
import java.util.UUID;

abstract class BaseMessage implements Serializable{
    private final UUID senderId;

    public UUID getSenderId() {
        return senderId;
    }

    public BaseMessage(UUID senderId){
        this.senderId=senderId;
    }
}

abstract class BrushMessage extends BaseMessage {
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

    public BrushMessage(UUID senderId, String command, BrushManager.Brush brush){
        super(senderId);
        this.command=command;
        this.brush = brush;
    }
}

class CreateBrushMessage extends BrushMessage{
    public CreateBrushMessage(UUID senderId, BrushManager.Brush brush) {
        super(senderId,COMMAND_CREATE, brush);
    }
}

class RemoveBrushMessage extends BrushMessage{
    public RemoveBrushMessage(UUID senderId, BrushManager.Brush brush) {
        super(senderId,COMMAND_REMOVE, brush);
    }
}

class UpdateBrushMessage extends BrushMessage{
    public UpdateBrushMessage(UUID senderId, BrushManager.Brush brush) {
        super(senderId,COMMAND_UPDATE, brush);
    }
}

class UpdatePixelMessage extends BaseMessage {
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

    public UpdatePixelMessage(UUID senderId, int x, int y, int color){
        super(senderId);
        this.x=x;
        this.y=y;
        this.color=color;
    }
}