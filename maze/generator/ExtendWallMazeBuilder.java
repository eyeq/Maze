package maze.generator;

import javafx.scene.effect.Light;
import maze.MazeData;

import java.awt.*;
import java.util.*;
import java.util.List;

public class ExtendWallMazeBuilder extends AbstractMazeBuilder {
    private final Random rand;

    public ExtendWallMazeBuilder(int width, int height, Random random) {
        super(width, height);
        rand = random;
    }

    @Override
    protected MazeData.Tile getFillTile() {
        return MazeData.Tile.PATH;
    }

    @Override
    public void buildMaze() {
        List<Point> walls = getStartWalls();
        while(!walls.isEmpty()) {
            Point start = walls.get(rand.nextInt(walls.size()));
            extendWall(start, walls);
        }
    }

    private List<Point> getStartWalls() {
        List<Point> walls = new ArrayList();
        for(int i = 2; i < getWidth()-1; i+=2) {
            walls.add(new Point(i, 0));
            walls.add(new Point(i, getWidth()-1));
        }
        for(int i = 2; i < getHeight()-1; i+=2) {
            walls.add(new Point(0, i));
            walls.add(new Point(getHeight()-1, i));
        }
        return walls;
    }

    private void extendWall(Point start, List<Point> walls) {
        int[] dx = {0, 1, 0, -1};
        int[] dy = {-1, 0, 1, 0};
        List<Integer> list = Arrays.asList(new Integer[]{0, 1, 2, 3});
        Collections.shuffle(list, rand);

        int x = start.x;
        int y = start.y;
        for(int d : list) {
            int dx2 = x+2*dx[d];
            int dy2 = y+2*dy[d];
            if(isExist(dx2, dy2, 2) && getData(dx2, dy2) == MazeData.Tile.PATH) {
                setData(x+dx[d], y+dy[d], MazeData.Tile.WALL);
                setData(dx2, dy2, MazeData.Tile.WALL);
                walls.add(new Point(dx2, dy2));
                return;
            }
        }
        walls.remove(start);
    }
}
