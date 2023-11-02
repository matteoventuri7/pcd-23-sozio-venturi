package _cooperative_pixel_art.core;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.UUID;

public class Brush implements Serializable, IBrush {
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

    protected Brush(final UUID id, final String name, final int x, final int y, final int color) {
        this.id= id;
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

        if (!(o instanceof Brush)) return false;

        Brush brush = (Brush) o;

        return new EqualsBuilder().append(id, brush.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).toHashCode();
    }

    public void update(IBrush brush) {
        this.color = brush.getColor();
        this.x = brush.getX();
        this.y = brush.getY();
    }
}
