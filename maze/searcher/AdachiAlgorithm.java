package maze.searcher;

import maze.MazeData;

import java.awt.*;
import java.util.*;
import java.util.List;

public class AdachiAlgorithm implements IMoveAlgorithm {
    private Random rand;
    private int[][] data;

    public AdachiAlgorithm(Random random) {
        rand = random;
    }

    @Override
    public Direction getMoveDirection(Point current, Point entrance, Point destination, MazeData mazeData, List<Direction> canMoves) {
        if(data == null) {
            data = new int[mazeData.getWidth()][mazeData.getHeight()];
        }
        for(Direction direction : Direction.values()) {
            int x = current.x + direction.getDx();
            int y = current.y + direction.getDy();
            if(mazeData.isExist(x, y) && mazeData.getTile(x, y) == MazeData.Tile.WALL) {
                data[x][y] = Integer.MIN_VALUE;
            }
        }
        for(int x = 0; x < data.length; x++) {
            for(int y = 0; y < data[x].length; y++) {
                if(data[x][y] != Integer.MIN_VALUE) {
                    data[x][y] = 0;
                }
            }
        }
        init(mazeData.getWidth(), mazeData.getHeight(), data, destination.x, destination.y, 1);
        Collections.shuffle(canMoves, rand);
        int min = data[current.x][current.y];
        Direction direction = null;
        for(Direction move : canMoves) {
            int d = data[current.x + move.getDx()][current.y + move.getDy()];
            if(d <= min) {
                min = d;
                direction = move;
            }
        }
        return direction;
    }

    private void init(int width, int height, int[][] data, int x, int y, int d) {
        if(0 <= x && x < width && 0 <= y && y < height) {
            if(data[x][y] == 0 || data[x][y] > d) {
                data[x][y] = d;
                for(Direction direction : Direction.values()) {
                    init(width, height, data, x + direction.getDx(), y + direction.getDy(), d + 1);
                }
            }
        }
    }
}
