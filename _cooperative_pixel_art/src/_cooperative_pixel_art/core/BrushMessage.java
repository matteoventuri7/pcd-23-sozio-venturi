package _cooperative_pixel_art.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class BrushMessage extends BaseMessage {

    public Brush getBrush() {
        return brush;
    }

    private final Brush brush;

    public BrushMessage(UUID senderId, Brush brush){
        super(senderId);
        this.brush = brush;
    }
}

