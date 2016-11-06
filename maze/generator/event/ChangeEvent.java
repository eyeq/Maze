package maze.generator.event;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;
import maze.MazeData;

public class ChangeEvent extends Event {
    public static final EventType<ChangeEvent> CHANGE = new EventType<>(Event.ANY, "CHANGE");

    private final int x;
    private final int y;
    private final MazeData.Tile tile;

    public ChangeEvent(Object source, EventTarget target, int x, int y, MazeData.Tile tile) {
        super(source, target, CHANGE);
        this.x = x;
        this.y = y;
        this.tile = tile;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public MazeData.Tile getTile() {
        return tile;
    }

    @Override
    public ChangeEvent copyFor(Object newSource, EventTarget newTarget) {
        return (ChangeEvent) super.copyFor(newSource, newTarget);
    }

    @Override
    public EventType<? extends ChangeEvent> getEventType() {
        return (EventType<? extends ChangeEvent>) super.getEventType();
    }
}
