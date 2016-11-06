package maze.searcher.event;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;
import maze.searcher.IMoveAlgorithm;

public class MoveEvent extends Event {
    public static final EventType<MoveEvent> MOVE = new EventType<>(Event.ANY, "MOVE");

    private final IMoveAlgorithm.Direction direction;

    public MoveEvent(Object source, EventTarget target, IMoveAlgorithm.Direction direction) {
        super(source, target, MOVE);
        this.direction = direction;
    }

    public IMoveAlgorithm.Direction getDirection() {
        return direction;
    }

    @Override
    public MoveEvent copyFor(Object newSource, EventTarget newTarget) {
        return (MoveEvent) super.copyFor(newSource, newTarget);
    }

    @Override
    public EventType<? extends MoveEvent> getEventType() {
        return (EventType<? extends MoveEvent>) super.getEventType();
    }
}
