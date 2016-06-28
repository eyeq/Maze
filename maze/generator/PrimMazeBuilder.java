package maze.generator;

import javafx.util.Pair;
import maze.MazeData;

import java.awt.*;
import java.util.*;
import java.util.List;

public class PrimMazeBuilder extends AbstractMazeBuilder {
    private final Random rand;

    public PrimMazeBuilder(int width, int height, Random random) {
        super(width, height);
        rand = random;
    }

    @Override
    protected MazeData.Tile getFillTile() {
        return MazeData.Tile.WALL;
    }

    @Override
    public void buildMaze() {
        List<Pair<Point, int[]>> edges = new ArrayList<>();
        addEdge(edges, 1, 1);
        prim(edges);
    }

    private void addEdge(List<Pair<Point, int[]>> edges, int x, int y) {
        int[] dx = {0, 1, 0, -1};
        int[] dy = {-1, 0, 1, 0};
        for(int i = 0; i < 4; i++) {
            int x2 = x + dx[i];
            int y2 = y + dy[i];
            if(isExist(x2, y2, 1) && getData(x2, y2) == MazeData.Tile.WALL) {
                edges.add(new Pair<>(new Point(x, y), new int[]{dx[i], dy[i]}));
            }
        }
    }

    private void prim(List<Pair<Point, int[]>> edges) {
        while(!edges.isEmpty()) {
            Pair<Point, int[]> edge = edges.remove(rand.nextInt(edges.size()));
            Point point = edge.getKey();
            int[] d = edge.getValue();
            int x = point.x + 2 * d[0];
            int y = point.y + 2 * d[1];
            if(getData(x, y) == MazeData.Tile.PATH) {
                continue;
            }
            setData(point.x, point.y, MazeData.Tile.PATH);
            setData(point.x + d[0], point.y + d[1], MazeData.Tile.PATH);
            setData(x, y, MazeData.Tile.PATH);
            addEdge(edges, x, y);
        }
    }
}
