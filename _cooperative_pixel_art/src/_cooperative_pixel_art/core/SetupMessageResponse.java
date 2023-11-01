package _cooperative_pixel_art.core;

import java.util.List;
import java.util.UUID;

public class SetupMessageResponse extends BaseMessage {
    private List<BaseMessage> historyEvents;

    public SetupMessageResponse(UUID senderId, List<BaseMessage> historyEvents) {
        super(senderId);
        this.historyEvents = historyEvents;
    }

    public List<BaseMessage> getHistoryEvents() {
        return historyEvents;
    }
}
