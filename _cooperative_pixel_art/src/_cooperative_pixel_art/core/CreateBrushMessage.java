package _cooperative_pixel_art.core;

import java.util.UUID;

public class CreateBrushMessage extends BrushMessage {
    public CreateBrushMessage(UUID senderId, Brush brush) {
        super(senderId, brush);
    }
}
