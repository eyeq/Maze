package maze.searcher;

import maze.MazeData;

import java.awt.*;
import java.util.*;
import java.util.List;

public class HillClimbingAlgorithm implements IMoveAlgorithm {
    private Random rand;

    public HillClimbingAlgorithm(Random random) {
        rand = random;
    }

    @Override
    public Direction getMoveDirection(Point current, Point entrance, Point destination, MazeData mazeData, List<Direction> canMoves) {
        Collections.shuffle(canMoves, rand);
        for(Direction direction : canMoves) {
            if((destination.getX() - current.getX()) * direction.getDx() + (destination.getY() - current.getY()) * direction.getDy() > 0) {
                return direction;
            }
        }
        return null;
    }
}
