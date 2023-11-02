package _cooperative_pixel_art.core;

import java.util.UUID;

public class RemoveBrushMessage extends BrushMessage {
    public RemoveBrushMessage(UUID senderId, IBrush brush) {
        super(senderId, brush);
    }
}
