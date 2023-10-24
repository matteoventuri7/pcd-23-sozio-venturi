package _cooperative_pixel_art.core;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.awt.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class BrushManager implements AutoCloseable {
    private static final int BRUSH_SIZE = 10;
    private static final int STROKE_SIZE = 2;
    private final Connection connection;
    private final Channel channel;
    private final Set<Brush> brushes = new HashSet<>();
    private final String exchangeName;
    private final Brush localBrush;
    private PixelGrid grid;
    private PixelGridView view;

    public BrushManager(Brush localBrush, String host, String exchangeName) throws IOException, TimeoutException {
        this.exchangeName=exchangeName;
        this.localBrush = localBrush;

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);

        connection = factory.newConnection();
        channel = connection.createChannel();

        // "fanout" exchange send a cpy of the message to all queues
        channel.exchangeDeclare(exchangeName, "fanout");

        addBrush(localBrush);

        // setup receive
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, exchangeName, "");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            Object o = SerializationUtils.deserialize(delivery.getBody());
            if(((BaseMessage)o).getSenderId().equals(localBrush.getId())){
                // this is my message. SKIP
                return;
            }
            if(o instanceof UpdatePixelMessage){
                UpdatePixelMessage upMsg = (UpdatePixelMessage)o;
                grid.set(upMsg.getX(), upMsg.getY(), upMsg.getColor());
            } else if(o instanceof CreateBrushMessage){
                CreateBrushMessage cMsg = (CreateBrushMessage)o;
                brushes.add(cMsg.getBrush());
            } else if(o instanceof RemoveBrushMessage){
                RemoveBrushMessage rMsg = (RemoveBrushMessage)o;
                brushes.remove(rMsg.getBrush());
            } else if(o instanceof UpdateBrushMessage){
                UpdateBrushMessage upMsg = (UpdateBrushMessage)o;
                var optBrush = brushes.stream()
                        .filter(b-> b.equals(upMsg.getBrush()))
                        .findFirst();
                if(optBrush.isPresent()){
                    var brush = optBrush.get();
                    brush.color = upMsg.getBrush().color;
                    brush.x = upMsg.getBrush().x;
                    brush.y = upMsg.getBrush().y;
                }
            } else {
                // message unknown
                return;
            }

            view.refresh();
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    }

    void sendMessage(Serializable message) throws IOException {
        channel.basicPublish(exchangeName, "", null, SerializationUtils.serialize(message));
    }

    void draw(final Graphics2D g) {
        brushes.forEach(brush -> {
            g.setColor(new Color(brush.color));
            var circle = new java.awt.geom.Ellipse2D.Double(brush.x - BRUSH_SIZE / 2.0, brush.y - BRUSH_SIZE / 2.0, BRUSH_SIZE, BRUSH_SIZE);
            // draw the polygon
            g.fill(circle);
            g.setStroke(new BasicStroke(STROKE_SIZE));
            g.setColor(Color.BLACK);
            g.draw(circle);
        });
    }

    void addBrush(final Brush brush) throws IOException {
        brushes.add(brush);
        sendMessage(new CreateBrushMessage(localBrush.getId(), brush));
    }

    void removeBrush(final Brush brush) throws IOException {
        brushes.remove(brush);
        sendMessage(new RemoveBrushMessage(localBrush.getId(), brush));
    }

    @Override
    public void close() throws Exception {
        removeBrush(localBrush);
        channel.close();
        connection.close();
    }

    public void updatePosition(int x, int y) throws IOException {
        localBrush.updatePosition(x,y);
        sendMessage(new UpdateBrushMessage(localBrush.getId(), localBrush));
    }

    public void updatePixel(int x, int y, int color) throws IOException {
        sendMessage(new UpdatePixelMessage(localBrush.getId(), x,y,color));
    }

    public void setGrid(PixelGrid grid) {
        this.grid=grid;
    }

    public void setView(PixelGridView view) {
        this.view = view;
    }

    public static class Brush implements Serializable {
        private final String name;
        private int x, y;
        private int color;
        private final UUID id;

        public Brush(final String name, final int x, final int y, final int color) {
            this.id= UUID.randomUUID();
            this.x = x;
            this.y = y;
            this.color = color;
            this.name=name;
        }

        public void updatePosition(final int x, final int y) {
            this.x = x;
            this.y = y;
        }
        // write after this getter and setters
        public int getX(){
            return this.x;
        }
        public int getY(){
            return this.y;
        }
        public int getColor(){
            return this.color;
        }
        public void setColor(int color){
            this.color = color;
        }
        public String getName() {return this.name;}
        public UUID getId(){return id;}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            Brush brush = (Brush) o;

            return new EqualsBuilder().append(id, brush.id).isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37).append(id).toHashCode();
        }
    }
}
