package maze.searcher;

import maze.MazeData;

import java.awt.*;
import java.util.List;

public class WallFollowerAlgorithm implements IMoveAlgorithm {
    private Direction preDirection = Direction.NORTH;

    @Override
    public Direction getMoveDirection(Point current, Point entrance, Point destination, MazeData mazeData, List<Direction> canMoves) {
        Direction[] directions = {Direction.NORTH, Direction.NORTH_WEST, Direction.WEST, Direction.SOUTH_WEST,
                Direction.SOUTH, Direction.SOUTH_EAST, Direction.EAST, Direction.NORTH_EAST,
                null, null, null, null, null, null, null, null};
        int length = 8;
        System.arraycopy(directions, 0, directions, length, length);
        int start = 0;
        while(directions[start+2] != preDirection) {
            start++;
        }
        for(int i = 0; i < length; i++) {
            Direction next = directions[start+i];
            if(canMoves.contains(next)) {
                preDirection = next;
                return next;
            }
        }
        return preDirection;
    }
}
