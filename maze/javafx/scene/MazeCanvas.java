package maze.javafx.scene;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import maze.MazeData;

public class MazeCanvas extends Canvas {
    private int tileSize = 10;
    private int wallWeight = 10;

    public void draw(MazeData maze) {
        GraphicsContext graphics = getGraphicsContext2D();
        for(int i = 0; i < maze.getWidth(); i++) {
            for(int j = 0; j < maze.getHeight(); j++) {
                draw(graphics, i, j, maze.getTile(i, j));
            }
        }
    }

    public void draw(GraphicsContext graphics, int x, int y, MazeData.Tile tile) {
        drawTile(graphics, x, y, (tile == MazeData.Tile.PATH) ? Color.WHITE : Color.BLACK);
    }

    public void drawTile(GraphicsContext graphics, int x, int y, Paint paint) {
        drawTile(graphics, x, y, paint, null, paint);
    }

    public void drawTile(GraphicsContext graphics, int x, int y, Paint paint, String text, Paint textPaint) {
        int x0 = x * (tileSize + wallWeight) / 2;
        int y0 = y * (tileSize + wallWeight) / 2;
        int width = tileSize;
        int height = tileSize;
        if(x % 2 == 0) {
            width = wallWeight;
        } else {
            x0 += (wallWeight - tileSize) / 2;
        }
        if(y % 2 == 0) {
            height = wallWeight;
        } else {
            y0 += (wallWeight - tileSize) / 2;
        }
        graphics.setFill(paint);
        graphics.fillRect(x0, y0, width, height);
        if(text != null) {
            graphics.setFill(textPaint);
            graphics.fillText(text, x0, y0 + tileSize);
        }
    }

    public void resize(MazeData maze) {
        int width = maze.getWidth();
        int height = maze.getHeight();
        this.setWidth(width * (tileSize + wallWeight) / 2 + (wallWeight - tileSize) / 2);
        this.setHeight(height * (tileSize + wallWeight) / 2 + (wallWeight - tileSize) / 2);
    }

    public void setTileSize(int tileSize) {
        this.tileSize = tileSize;
    }

    public void setWallWeight(int wallWeight) {
        this.wallWeight = wallWeight;
    }
}
