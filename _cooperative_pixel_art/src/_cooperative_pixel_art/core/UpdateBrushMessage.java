package _cooperative_pixel_art.core;

import java.util.UUID;

public class UpdateBrushMessage extends BrushMessage {
    public UpdateBrushMessage(UUID senderId, IBrush brush) {
        super(senderId, brush);
    }
}
