package maze.javafx;

import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;

public class HandScrollListener {
    private double oldClickedX;
    private double oldClickedY;
    private double scrollSpeed;

    public HandScrollListener(double scrollSpeed) {
        setScrollSpeed(scrollSpeed);
    }

    public double getScrollSpeed() {
        return scrollSpeed;
    }

    public void setScrollSpeed(double scrollSpeed) {
        this.scrollSpeed = scrollSpeed;
    }

    public void mousePressed(MouseEvent e) {
        oldClickedX = e.getScreenX();
        oldClickedY = e.getScreenY();
    }

    public void mouseDragged(MouseEvent e, ScrollPane scrollPane) {
        double newClickedX = e.getScreenX();
        double newClickedY = e.getScreenY();
        double dx = oldClickedX - newClickedX;
        double dy = oldClickedY - newClickedY;
        oldClickedX = newClickedX;
        oldClickedY = newClickedY;

        scrollPane.setHvalue(scrollPane.getHvalue() + dx*scrollSpeed/1000);
        scrollPane.setVvalue(scrollPane.getVvalue() + dy*scrollSpeed/1000);
    }
}
