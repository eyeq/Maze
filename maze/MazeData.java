package maze;

public class MazeData {
    private int width;
    private int height;
    private Tile[][] tiles;

    public MazeData(int width, int height, Tile... tiles) {
        setMaze(width, height, tiles);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isExist(int x, int y) {
        return -1 < x && x < getWidth() && -1 < y && y < getHeight();
    }

    public Tile getTile(int x, int y) {
        return tiles[x][y];
    }

    public boolean isMazePath(int x, int y) {
        return isExist(x, y) && getTile(x, y) == Tile.PATH;
    }

    protected void setWidth(int width) {
        this.width = width;
    }

    protected void setHeight(int height) {
        this.height = height;
    }

    protected void setMaze(int width, int height, Tile... tiles) {
        setWidth(width);
        setHeight(height);
        this.tiles = new Tile[getWidth()][getHeight()];
        for(int i = 0; i < getWidth(); i++) {
            for(int j = 0; j < getHeight(); j++) {
                int index = i*getHeight() + j;
                this.tiles[i][j] = (index < tiles.length) ? tiles[index] : Tile.PATH;
            }
        }
    }

    protected void init() {
        setMaze(getWidth(), getHeight());
    }

    public enum Tile {
        PATH,
        WALL,
        ;
    }
}
