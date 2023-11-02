package _cooperative_pixel_art.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class BrushMessage extends BaseMessage {

    public IBrush getBrush() {
        return brush;
    }

    private final IBrush brush;

    public BrushMessage(UUID senderId, IBrush brush){
        super(senderId);
        this.brush = brush;
    }
}

