package maze.generator;

import maze.MazeData;

import java.util.Random;

public class DefeatStickMazeBuilder extends AbstractMazeBuilder {
    private final Random rand;
    public DefeatStickMazeBuilder(int width, int height, Random random) {
        super(width, height);
        rand = random;
    }

    @Override
    protected MazeData.Tile getFillTile() {
        return MazeData.Tile.PATH;
    }

    @Override
    public void buildMaze() {
        defeatStick();
    }

    protected void defeatStick() {
        int[] dx = {0, 1, 0, -1};
        int[] dy = {-1, 0, 1, 0};
        for(int i = 2; i < getWidth()-2; i+=2) {
            for(int j = 2; j < getHeight()-2; j+=2) {
                int d;
                if(i == 2) {
                    d = rand.nextInt(4);
                } else {
                    do {
                        d = rand.nextInt(3);
                    } while(getData(i+dx[d], j+dy[d]) == MazeData.Tile.WALL);
                }
                setData(i, j, MazeData.Tile.WALL);
                setData(i+dx[d], j+dy[d], MazeData.Tile.WALL);
            }
        }
    }
}
