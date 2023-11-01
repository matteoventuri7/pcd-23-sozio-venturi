package _cooperative_pixel_art.core;

import java.io.Serializable;
import java.util.UUID;

public abstract class BaseMessage implements Serializable {
    private final UUID senderId;

    public UUID getSenderId() {
        return senderId;
    }

    public BaseMessage(UUID senderId) {
        this.senderId = senderId;
    }
}
