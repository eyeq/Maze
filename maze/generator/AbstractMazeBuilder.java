package maze.generator;

import maze.MazeData;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractMazeBuilder implements IMazeBuilder {
    private MazeData.Tile[][] data;
    private int width;
    private int height;

    public AbstractMazeBuilder(int width, int height) {
        this.width = Math.max(5, width);
        this.height = Math.max(5, height);
        data = new MazeData.Tile[getWidth()][getHeight()];
    }

    public AbstractMazeBuilder(MazeData data) {
        this(data.getWidth(), data.getHeight());
        for(int x = 0; x < getWidth(); x++) {
            for(int y = 0; y < getHeight(); y++) {
                setData(x, y, data.getTile(x, y));
            }
        }
    }

    protected abstract MazeData.Tile getFillTile();

    protected int getWidth() {
        return width;
    }

    protected int getHeight() {
        return height;
    }

    private MazeData.Tile[][] getData() {
        return data;
    }

    protected void setData(int x, int y, MazeData.Tile tile) {
        getData()[x][y] = tile;
    }

    public MazeData.Tile getData(int x, int y) {
        return getData()[x][y];
    }

    public boolean isExist(int x, int y) {
        return isExist(x, y, 0);
    }

    public boolean isExist(int x, int y, int margin) {
        return -1 + margin < x && x < getWidth() - margin
                && -1 + margin < y && y < getHeight() - margin;
    }

    @Override
    public void buildFill() {
        for(int i = 0; i < getWidth(); i++) {
            for(int j = 0; j < getHeight(); j++) {
                data[i][j] = getFillTile();
            }
        }
    }

    @Override
    public void buildFrame() {
        if(getFillTile() == MazeData.Tile.WALL) {
            return;
        }
        for(int i = 0; i < getWidth(); i++) {
            setData(i, 0, MazeData.Tile.WALL);
            setData(i, getHeight()-1, MazeData.Tile.WALL);
        }
        for(int i = 0; i < getHeight(); i++) {
            setData(0, i, MazeData.Tile.WALL);
            setData(getWidth()-1, i, MazeData.Tile.WALL);
        }
    }

    @Override
    public MazeData getMazeData() {
        List<MazeData.Tile> list = new LinkedList<>();
        for(MazeData.Tile[] tiles : getData()) {
            Collections.addAll(list, tiles);
        }
        MazeData.Tile[] tiles = new MazeData.Tile[list.size()];
        list.toArray(tiles);
        return new MazeData(getWidth(), getHeight(), tiles);
    }
}
