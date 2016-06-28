package maze.searcher;

import javafx.event.EventHandler;
import maze.MazeData;
import maze.searcher.event.MoveEvent;

import java.awt.*;
import java.util.*;
import java.util.List;

public class MazeExplorer {
    private EventHandler<? super MoveEvent> moveEventHandler;
    private IMoveMethod moveMethod;

    private Point start;
    private Point current;
    private Point destination;
    private MazeData mazeData;

    public MazeExplorer(IMoveMethod moveMethod, Point current, Point destination, MazeData mazeData) {
        setMoveMethod(moveMethod);
        this.current = current;
        this.start = current;
        this.destination = destination;
        this.mazeData = mazeData;
    }

    public boolean move() {
        IMoveMethod.Direction direction = moveMethod.getMoveDirection(new Point(current), new Point(start), new Point(destination), mazeData, getCanMoves());
        if(!canMove(direction)) {
            return false;
        }
        int nextX = current.x + direction.getDx();
        int nextY = current.y + direction.getDy();
        current.move(nextX, nextY);
        if(getOnSearcherMoveed() != null) {
            MoveEvent event = new MoveEvent(this, null, direction);
            getOnSearcherMoveed().handle(event);
        }
        return true;
    }

    public void setMoveMethod(IMoveMethod moveMethod) {
        this.moveMethod = moveMethod;
    }

    public int getCurrentX() {
        return current.x;
    }

    public int getCurrentY() {
        return current.y;
    }

    public boolean canMove(IMoveMethod.Direction direction) {
        int dx = direction.getDx();
        int dy = direction.getDy();
        int x = current.x;
        int y = current.y;
        return mazeData.isMazePath(x+dx, y+dy) && (mazeData.isMazePath(x+dx, y) || mazeData.isMazePath(x, y+dy));
    }

    public java.util.List<IMoveMethod.Direction> getCanMoves() {
        List directions = new ArrayList<IMoveMethod.Direction>();
        for(IMoveMethod.Direction direction : IMoveMethod.Direction.values()) {
            if(canMove(direction)) {
                directions.add(direction);
            }
        }
        return directions;
    }

    public final void setOnSearcherMoveed(EventHandler<? super MoveEvent> value) {
        moveEventHandler = value;
    }

    public final EventHandler<? super MoveEvent> getOnSearcherMoveed() {
        return moveEventHandler;
    }
}
