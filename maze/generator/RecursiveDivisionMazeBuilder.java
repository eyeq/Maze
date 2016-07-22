package maze.generator;

import maze.MazeData;

import java.util.Random;

public class RecursiveDivisionMazeBuilder extends AbstractMazeBuilder {
    private final Random rand;

    public RecursiveDivisionMazeBuilder(int width, int height, Random random) {
        super(width, height);
        rand = random;
    }

    @Override
    protected MazeData.Tile getFillTile() {
        return MazeData.Tile.PATH;
    }

    @Override
    public void buildMaze() {
        divide(1, 1, getWidth() - 2, getHeight() - 2);
    }

    private void divide(int minX, int minY, int maxX, int maxY) {
        int dx = maxX - minX;
        int dy = maxY - minY;
        if(dx < 1 || dy < 1) {
            return;
        }
        boolean isHorizontal = dx == dy ? rand.nextBoolean() : dx > dy;
        if(isHorizontal) {
            int x = minX + 1;
            if(dx > 3) {
                x += rand.nextInt(dx / 2) * 2;
            }
            int ya = minY;
            if(dy > 3) {
                ya += rand.nextInt(dy / 2) * 2;
            }
            for(int y = minY; y < maxY + 1; y++) {
                if(y == ya) {
                    continue;
                }
                setData(x, y, MazeData.Tile.WALL);
            }
            divide(minX, minY, x - 1, maxY);
            divide(x + 1, minY, maxX, maxY);
        } else {
            int y = minY + 1;
            if(dy > 3) {
                y += rand.nextInt(dy / 2) * 2;
            }
            int xa =  minX;
            if(dx > 3) {
                xa += rand.nextInt(dx / 2) * 2;
            }
            for(int x = minX; x < maxX + 1; x++) {
                if(x == xa) {
                    continue;
                }
                setData(x, y, MazeData.Tile.WALL);
            }
            divide(minX, minY, maxX, y - 1);
            divide(minX, y + 1, maxX, maxY);
        }
    }
}
