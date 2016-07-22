package maze.searcher;

import maze.MazeData;

import java.awt.*;
import java.util.List;

public interface IMoveAlgorithm {
    Direction getMoveDirection(Point current, Point entrance, Point destination, MazeData mazeData, List<Direction> canMoves);

    enum Direction {
        EAST(1, 0),
        NORTH_EAST(1, 1),
        NORTH(0, 1),
        NORTH_WEST(-1, 1),
        WEST(-1, 0),
        SOUTH_WEST(-1, -1),
        SOUTH(0, -1),
        SOUTH_EAST(1, -1),
        ;

        private final int dx;
        private final int dy;

        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }

        public int getDx() {
            return dx;
        }

        public int getDy() {
            return dy;
        }

        public Direction reverse() {
            Direction[] directions = Direction.values();
            int n = (ordinal() + 4) % directions.length;
            return directions[n];
        }

        public Direction[] reverses() {
            Direction[] directions = Direction.values();
            Direction reverse = reverse();
            int length = directions.length;
            return new Direction[] {
                    directions[(reverse.ordinal() - 1 + length) % length],
                    reverse,
                    directions[(reverse.ordinal() + 1) % length]
            };
        }
    }
}
