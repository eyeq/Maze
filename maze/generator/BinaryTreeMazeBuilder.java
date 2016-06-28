package maze.generator;

import maze.MazeData;

import java.util.Random;

public class BinaryTreeMazeBuilder extends AbstractMazeBuilder {
    private final Random rand;

    public BinaryTreeMazeBuilder(int width, int height, Random random) {
        super(width, height);
        rand = random;
    }

    @Override
    protected MazeData.Tile getFillTile() {
        return MazeData.Tile.WALL;
    }

    @Override
    public void buildMaze() {
        buildStartPath();
        binaryTree();
    }

    private void buildStartPath() {
        for(int i = 1; i < getWidth()-1; i++) {
            setData(i, 1, MazeData.Tile.PATH);
        }
        for(int j = 2; j < getHeight()-1; j++) {
            setData(1, j, MazeData.Tile.PATH);
        }
    }

    private void binaryTree() {
        for(int i = 3; i < getWidth(); i+=2) {
            for(int j = 3; j < getHeight(); j+=2) {
                if(rand.nextBoolean()) {
                    setData(i-1, j, MazeData.Tile.PATH);
                } else {
                    setData(i, j-1, MazeData.Tile.PATH);
                }
                setData(i, j, MazeData.Tile.PATH);
            }
        }
    }
}
