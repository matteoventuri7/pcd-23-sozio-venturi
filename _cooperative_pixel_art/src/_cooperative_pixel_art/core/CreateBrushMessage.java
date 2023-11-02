package _cooperative_pixel_art.core;

import java.util.UUID;

public class CreateBrushMessage extends BrushMessage {
    public CreateBrushMessage(UUID senderId, IBrush brush) {
        super(senderId, brush);
    }
}
