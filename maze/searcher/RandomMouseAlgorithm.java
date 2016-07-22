package maze.searcher;

import maze.MazeData;

import java.awt.*;
import java.util.*;
import java.util.List;

public class RandomMouseAlgorithm implements IMoveAlgorithm {
    private Random rand;
    private Direction preDirection;

    public RandomMouseAlgorithm(Random random) {
        rand = random;
    }

    @Override
    public Direction getMoveDirection(Point current, Point entrance, Point destination, MazeData mazeData, List<Direction> canMoves) {
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
