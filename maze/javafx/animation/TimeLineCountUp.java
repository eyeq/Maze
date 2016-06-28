package maze.javafx.animation;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class TimeLineCountUp {
    private final List<EventHandler> eventHandlerList = new ArrayList<>();
    private final Timeline timeline;
    private Duration time = Duration.ZERO;

    public TimeLineCountUp(Duration time) {
        timeline = new Timeline(new KeyFrame(time, (e) -> this.count(((KeyFrame) e.getSource()).getTime())));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    private void count(Duration duration) {
        setTime(time.add(duration));
        for(EventHandler handler : eventHandlerList) {
            handler.handle(new Event(this, null, null));
        }
    }

    public boolean addEventHandler(EventHandler handler) {
        return eventHandlerList.add(handler);
    }

    public boolean removeEventHandler(EventHandler handler) {
        return eventHandlerList.remove(handler);
    }

    public boolean isRunning() {
        return timeline.getStatus() == Animation.Status.RUNNING;
    }

    public void play() {
        timeline.play();
    }

    public void stop() {
        timeline.stop();
    }

    public Duration getTime() {
        return time;
    }

    public void setTime(Duration duration) {
        time = duration;
    }
}
