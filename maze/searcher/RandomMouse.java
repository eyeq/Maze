package maze.searcher;

import maze.MazeData;

import java.awt.*;
import java.util.*;

public class RandomMouse implements IMoveMethod {
    private Random rand;
    private Direction preDirection;

    public RandomMouse(Random random) {
        rand = random;
    }

    @Override
    public Direction getMoveDirection(Point current, Point entrance, Point exsist, MazeData mazeData, java.util.List<Direction> canMoves) {
        Direction direction = canMoves.get(rand.nextInt(canMoves.size()));
        if(preDirection != null) {
            canMoves.removeAll(Arrays.asList(preDirection.reverses()));
            if(!canMoves.isEmpty()) {
                direction = canMoves.get(rand.nextInt(canMoves.size()));
            }
        }
        preDirection = direction;
        return direction;
    }
}
