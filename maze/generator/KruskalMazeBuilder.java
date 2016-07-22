package maze.generator;

import javafx.util.Pair;
import maze.MazeData;

import java.awt.*;
import java.util.*;
import java.util.List;

public class KruskalMazeBuilder extends AbstractMazeBuilder {
    private final Random rand;

    public KruskalMazeBuilder(int width, int height, Random random) {
        super(width, height);
        rand = random;
    }

    @Override
    protected MazeData.Tile getFillTile() {
        return MazeData.Tile.WALL;
    }

    @Override
    public void buildMaze() {
        List<Pair<Point, int[]>> edges = getEdges();
        Collections.shuffle(edges, rand);
        unionFind(edges);
    }

    private List<Pair<Point, int[]>> getEdges() {
        List<Pair<Point, int[]>> edges = new ArrayList<>();
        for(int i = 1; i < getWidth() - 1; i += 2) {
            for(int j = 1; j < getHeight() - 1; j += 2) {
                edges.add(new Pair<>(new Point(i, j), new int[]{1, 0}));
                edges.add(new Pair<>(new Point(i, j), new int[]{0, 1}));
            }
        }
        return edges;
    }

    private void unionFind(List<Pair<Point, int[]>> edges) {
        UnionFind unionFind = new UnionFind((getWidth() + 1) * getHeight());
        for(Pair<Point, int[]> edge : edges) {
            Point point = edge.getKey();
            int[] d = edge.getValue();
            int x = point.x + point.y * getWidth();
            int y = (point.x + 2 * d[0]) + (point.y + 2 * d[1]) * getWidth();
            if(isExist(point.x + 2 * d[0], point.y + 2 * d[1]) && unionFind.union(x, y)) {
                setData(point.x, point.y, MazeData.Tile.PATH);
                setData(point.x + d[0], point.y + d[1], MazeData.Tile.PATH);
                setData(point.x + 2 * d[0], point.y + 2 * d[1], MazeData.Tile.PATH);
            }
        }
    }

    private class UnionFind {
        private final int[] parent;
        private final int[] rank;

        UnionFind(int size) {
            parent = new int[size];
            rank = new int[size];
            Arrays.fill(parent, -1);
            Arrays.fill(rank, 0);
        }

        private int root(int value) {
            if(parent[value] < 0) {
                return value;
            }
            parent[value] = root(parent[value]);
            return parent[value];
        }

        public boolean equals(int x, int y) {
            return root(x) == root(y);
        }

        public boolean union(int x, int y) {
            x = root(x);
            y = root(y);
            if(x == y) {
                return false;
            }
            if(rank[x] < rank[y]) {
                parent[x] = y;
            } else {
                parent[y] = x;
                if(rank[x] == rank[y]) {
                    rank[x]++;
                }
            }
            return true;
        }
    }
}
