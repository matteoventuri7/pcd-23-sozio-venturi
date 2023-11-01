package _cooperative_pixel_art.core;

import java.util.UUID;

public class RemoveBrushMessage extends BrushMessage {
    public RemoveBrushMessage(UUID senderId, Brush brush) {
        super(senderId, brush);
    }
}
