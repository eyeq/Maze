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
        data[destination.x][destination.y] = 1;
        for(int i = 1; ; i++) {
            if(init(mazeData.getWidth(), mazeData.getHeight(), data, i)) {
                break;
            }
        }

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

    private boolean init(int width, int height, int[][] data, int d) {
        boolean isFinish = true;
        for(int x = 0; x < data.length; x++) {
            for(int y = 0; y < data[x].length; y++) {
                if(data[x][y] == d) {
                    for(Direction direction : Direction.values()) {
                        int x1 = x + direction.getDx();
                        int y1 = y + direction.getDy();
                        if(0 <= x1 && x1 < width && 0 <= y1 && y1 < height) {
                            if(data[x1][y1] == 0 || data[x1][y1] > d + 1) {
                                isFinish = false;
                                data[x1][y1] = d + 1;
                            }
                        }
                    }
                }
            }
        }
        return isFinish;
    }
}
