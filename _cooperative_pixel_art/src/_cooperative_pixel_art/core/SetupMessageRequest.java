package _cooperative_pixel_art.core;

import java.util.UUID;

public class SetupMessageRequest extends BaseMessage {
    private final String queueName;

    public SetupMessageRequest(UUID senderId, String queueName) {
        super(senderId);
        this.queueName = queueName;
    }

    public String getQueueName() {
        return queueName;
    }
}
