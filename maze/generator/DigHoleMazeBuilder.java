package maze.generator;

import maze.MazeData;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DigHoleMazeBuilder extends AbstractMazeBuilder {
    private final Random rand;

    public DigHoleMazeBuilder(int width, int height, Random random) {
        super(width, height);
        rand = random;
    }

    @Override
    protected MazeData.Tile getFillTile() {
        return MazeData.Tile.WALL;
    }

    @Override
    public void buildMaze() {
        digHole(1, 1);
    }

    public void digHole(int x, int y) {
        int[] dx = {0, 1, 0, -1};
        int[] dy = {-1, 0, 1, 0};
        List<Integer> list = Arrays.asList(new Integer[]{0, 1, 2, 3});
        Collections.shuffle(list, rand);

        setData(x, y, MazeData.Tile.PATH);
        for(int d : list) {
            int dx2 = x+2*dx[d];
            int dy2 = y+2*dy[d];
            if(isExist(dx2, dy2, 1) && getData(dx2, dy2) == MazeData.Tile.WALL) {
                setData(x+dx[d], y+dy[d], MazeData.Tile.PATH);
                digHole(dx2, dy2);
            }
        }
    }
}
