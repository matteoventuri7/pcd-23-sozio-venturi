package _cooperative_pixel_art.rabbitqm;

import _cooperative_pixel_art.core.*;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.List;
import java.util.concurrent.Semaphore;

public class BrushManagerRabbitMQ extends BaseBrushManager {
    private Connection connection;
    private Channel channel;
    private final String exchangeName;
    private String queueName;
    private final String host;
    private boolean iAmBrokerNode;
    private List<BaseMessage> historyEvents;
    private Semaphore isReadySemaphore = new Semaphore(1, true);

    public BrushManagerRabbitMQ(Brush localBrush, String host, String exchangeName, boolean isBrokerNode) {
        super(localBrush);
        this.host=host;
        this.iAmBrokerNode = isBrokerNode;
        this.exchangeName=exchangeName;

        if(iAmBrokerNode){
            configureBrokerNode();
        }
    }

    private void configureBrokerNode() {
        historyEvents = new ArrayList<>();
    }

    private void HandleEvent(Object o){
        switch (o) {
            case UpdatePixelMessage upMsg -> { if(grid != null) grid.set(upMsg.getX(), upMsg.getY(), upMsg.getColor()); }
            case CreateBrushMessage cMsg -> brushes.add(cMsg.getBrush());
            case RemoveBrushMessage rMsg -> {
                brushes.remove(rMsg.getBrush());
                if(brushes.size() == 1 && !iAmBrokerNode){
                    iAmBrokerNode = true;
                    configureBrokerNode();
                    // fill history from grid
                    for (int x = 0; x < grid.getNumRows(); x++)
                        for(int y = 0; y < grid.getNumColumns(); y++)
                            historyEvents.add(new UpdatePixelMessage(localBrush.getId(), x, y, grid.get(x,y)));
                }
            }
            case UpdateBrushMessage upMsg -> {
                var brushMsg = upMsg.getBrush();
                var optBrush = brushes.stream()
                        .filter(b -> b.equals(brushMsg))
                        .findFirst();
                if (optBrush.isEmpty()) {
                    brushes.add(brushMsg);
                    optBrush = Optional.of(brushMsg);
                }
                var brush = optBrush.get();
                brush.update(upMsg.getBrush());
            }
            case SetupMessageRequest smr when iAmBrokerNode -> {
                try {
                    System.out.println("Sending history with " + this.historyEvents.size() + "events");
                    var message = SerializationUtils.serialize(new SetupMessageResponse(localBrush.getId(), this.historyEvents));
                    channel.basicPublish("", smr.getQueueName(), null, message);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            case null, default -> {
                // message unknown
                System.out.println("Unknown message");
                return;
            }
        }
    }

    private void SetupHistoryGrid() throws IOException, InterruptedException {
        final String queueName = localBrush.getId().toString();
        channel.queueDeclare(queueName, false, false, false, null);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            Object o = SerializationUtils.deserialize(delivery.getBody());
            if(o instanceof SetupMessageResponse){

                SetupMessageResponse sm = (SetupMessageResponse)o;
                for (var ev: sm.getHistoryEvents()) {
                    HandleEvent(ev);
                }

                isReadySemaphore.release();
                channel.queueDelete(queueName);
            }
        };

        isReadySemaphore.acquire();

        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });

        sendMessage(new SetupMessageRequest(localBrush.getId(), queueName));
    }

    private void sendMessage(Serializable message) {
        try{
            channel.basicPublish(exchangeName, "", null, SerializationUtils.serialize(message));
        }catch (Exception ex){
            System.out.println("Send event exception");
            ex.printStackTrace();
        }
    }

    @Override
    public void addBrush(final IBrush brush) {
        super.addBrush(brush);
        sendMessage(new CreateBrushMessage(localBrush.getId(), brush));
    }
    @Override
    public void removeBrush(final IBrush brush) {
        super.removeBrush(brush);
        sendMessage(new RemoveBrushMessage(localBrush.getId(), brush));
    }

    @Override
    public void close() throws Exception {
        removeBrush(localBrush);
        channel.queueDelete(queueName);
        channel.close();
        connection.close();
    }

    @Override
    public void updatePosition(int x, int y) {
        super.updatePosition(x,y);
        sendMessage(new UpdateBrushMessage(localBrush.getId(), localBrush));
    }

    @Override
    public void updatePixel(int x, int y, int color) {
        super.updatePixel(x, y, color);
        sendMessage(new UpdatePixelMessage(localBrush.getId(), x,y,color));
    }

    @Override
    public void run() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(this.host);

            connection = factory.newConnection();
            channel = connection.createChannel();

            // "fanout" exchange send a copy of the message to all queues
            channel.exchangeDeclare(exchangeName, "fanout");

            // setup receive
            this.queueName = channel.queueDeclare().getQueue();
            System.out.println("Queue: " + queueName);
            channel.queueBind(queueName, exchangeName, "");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                Object o = SerializationUtils.deserialize(delivery.getBody());
                if (!(o instanceof BaseMessage)) {
                    System.out.println("Received wrong type event");
                    return;
                }

                var sendId = ((BaseMessage) o).getSenderId();
                var localBrushId = localBrush.getId();

                System.out.println("Received event from " + sendId);

                if (iAmBrokerNode &&
                        (o instanceof CreateBrushMessage || o instanceof RemoveBrushMessage || o instanceof UpdatePixelMessage)) {
                    historyEvents.add((BaseMessage) o);
                }

                if (sendId.equals(localBrush.getId())) {
                    // this is my message. SKIP
                    System.out.println("Local brush id: " + localBrushId);
                    System.out.println("Event skipped");
                    return;
                }

                HandleEvent(o);

                if(view != null)
                    view.refresh();
            };

            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
                System.out.println("------Cancel callback " + consumerTag);
            });

            if (!iAmBrokerNode) {
                SetupHistoryGrid();
            }

            isReadySemaphore.acquire();
            isReadySemaphore.release();

            addBrush(localBrush);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
