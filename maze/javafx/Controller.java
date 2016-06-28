package maze.javafx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import maze.MazeData;
import maze.generator.*;
import maze.javafx.animation.TimeLineCountUp;
import maze.searcher.IMoveMethod;
import maze.searcher.MazeExplorer;

import java.awt.Point;
import java.net.URL;
import java.util.*;
import java.util.List;

public class Controller implements Initializable {
    public static final int MAZE_TILE_SIZE = 10;
    private static final int LOG_SNAPSHOT_INTERVAL = 1000;

    private static final Duration SLOW = Duration.millis(100);
    private static final Duration FAST = Duration.millis(10);

    @FXML
    private Spinner<Integer> widthSpinner;
    @FXML
    private Spinner<Integer> heightSpinner;
    @FXML
    private ComboBox<String> generationCombo;
    @FXML
    private ComboBox<String> searchCombo;
    @FXML
    private Slider logSlider;
    @FXML
    private Label logLabel;

    @FXML
    private TextField generationSeed;
    @FXML
    private TextField searchSeed;

    @FXML
    private Label generationUsedTime;
    @FXML
    private Label searchUsedTime;

    @FXML
    private ScrollPane mazePane;
    @FXML
    private Canvas mazeCanvas;

    private final TimeLineCountUp timer;
    private Duration speed;

    private final GenerationController generationController;
    private final SearchController searchController;
    private MazeData maze;
    private Point entrance;
    private Point exist;
    private List<Point> searcherLog;
    private List<Image> searcherLogImage;

    public Controller() {
        timer = new TimeLineCountUp(Duration.ONE);
        timer.addEventHandler(e -> this.time(((TimeLineCountUp) e.getSource()).getTime()));
        speed = SLOW;

        generationController = new GenerationController();
        searchController = new SearchController();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourcebundle) {
        HandScrollListener hansScroll = new HandScrollListener(4);
        mazeCanvas.setOnMouseReleased((e) -> ((Node) e.getSource()).setCursor(Cursor.DEFAULT));
        mazeCanvas.setOnMousePressed((e) -> {
            ((Node) e.getSource()).setCursor(Cursor.CLOSED_HAND);
            hansScroll.mousePressed(e);
        });
        mazeCanvas.setOnMouseDragged((e) -> hansScroll.mouseDragged(e, mazePane));

        widthSpinner.setEditable(true);
        heightSpinner.setEditable(true);
        if(widthSpinner.getValueFactory() == null) {
            widthSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 99, 21, 2));
        }
        if(heightSpinner.getValueFactory() == null) {
            heightSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 99, 21, 2));
        }
        widthSpinner.getEditor().textProperty().addListener((observable, oldValue, newValue) -> changeSpinner(widthSpinner, oldValue, newValue));
        heightSpinner.getEditor().textProperty().addListener((observable, oldValue, newValue) -> changeSpinner(heightSpinner, oldValue, newValue));

        Random rand = new Random();
        generationCombo.getItems().addAll(generationController.getGenerationNames());
        generationCombo.getSelectionModel().select(rand.nextInt(generationCombo.getItems().size()));
        searchCombo.getItems().addAll(searchController.getSearchNames());
        searchCombo.getSelectionModel().select(rand.nextInt(searchCombo.getItems().size()));

        logSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.intValue() > oldValue.intValue() + LOG_SNAPSHOT_INTERVAL) {
                logSlider.setValue(oldValue.intValue() + LOG_SNAPSHOT_INTERVAL);
                return;
            }
            changeLogSlider();
            drawLog();
        });

        generationMaze();
        searchMaze();
        drawMaze();
        drawLog();
    }

    private void changeSpinner(Spinner<Integer> spinner, String oldValue, String newValue) {
        int value;
        try {
            value = new Integer(newValue);
            if(9 < value && value % 2 == 0) {
                value++;
            }
        } catch(NumberFormatException e) {
            value = new Integer(oldValue);
            spinner.getEditor().textProperty().setValue(Integer.toString(value));
        }
        spinner.getValueFactory().setValue(value);
    }

    private void changeLogSlider() {
        logLabel.setText((int) logSlider.getValue() + "/" + (int) logSlider.getMax());
    }

    public MazeData getMaze() {
        return maze;
    }

    private void drawMaze() {
        GraphicsContext graphics = mazeCanvas.getGraphicsContext2D();
        for(int i = 0; i < maze.getWidth(); i++) {
            for(int j = 0; j < maze.getHeight(); j++) {
                drawTile(graphics, i, j, (getMaze().getTile(i, j) == MazeData.Tile.PATH) ? Color.WHITE : Color.BLACK);
            }
        }
    }

    private void drawLog() {
        GraphicsContext graphics = mazeCanvas.getGraphicsContext2D();

        int size = searcherLogImage.size();
        int n = (int) logSlider.getValue();
        int m = Math.min(size - 1, n / LOG_SNAPSHOT_INTERVAL);
        if(m < 0) {
            m = 0;
        } else {
            Image image = searcherLogImage.get(m);
            graphics.drawImage(image, 0, 0);
        }
        for(int i = m * LOG_SNAPSHOT_INTERVAL; i < n; i++) {
            Point point = searcherLog.get(i);
            drawTile(graphics, point.x, point.y, Color.GRAY);
            if(i % LOG_SNAPSHOT_INTERVAL == 0 && size == i / LOG_SNAPSHOT_INTERVAL) {
                Image image = mazeCanvas.snapshot(null, null);
                searcherLogImage.add(image);
                size++;
            }
        }

        Point point = searcherLog.get(n);
        drawTile(graphics, point.x, point.y, Color.RED);

        drawTile(graphics, entrance.x, entrance.y, Color.BLUE, "S", Color.WHITE);
        drawTile(graphics, exist.x, exist.y, Color.BLUE, "G", Color.WHITE);
    }

    private void drawTile(GraphicsContext graphics, int x, int y, Paint paint) {
        drawTile(graphics, x, y, paint, null, paint);
    }

    private void drawTile(GraphicsContext graphics, int x, int y, Paint paint, String text, Paint textPaint) {
        graphics.setFill(paint);
        graphics.fillRect(x * MAZE_TILE_SIZE, y * MAZE_TILE_SIZE, MAZE_TILE_SIZE, MAZE_TILE_SIZE);
        if(text != null) {
            graphics.setFill(textPaint);
            graphics.fillText(text, x * MAZE_TILE_SIZE, (y + 1) * MAZE_TILE_SIZE);
        }
    }

    private void generationMaze() {
        int width = widthSpinner.getValue();
        width += (width + 1) % 2;
        int height = heightSpinner.getValue();
        height += (height + 1) % 2;

        Random rand;
        String text = generationSeed.getText();
        if(text.isEmpty()) {
            rand = new Random();
        } else {
            try {
                rand = new Random(Long.parseLong(text));
            } catch(NumberFormatException e) {
                rand = new Random();
            }
        }

        int index = generationCombo.getSelectionModel().getSelectedIndex();
        IMazeBuilder mazeBuilder = generationController.getGeneration(index).apply(new int[]{width, height}, rand);
        MazeDetector mazeDetector = new MazeDetector(mazeBuilder);
        maze = mazeDetector.construct();

        entrance = new Point(1, 1);
        long start = System.nanoTime();
        do {
            int existX = rand.nextInt(width / 2) * 2 + 1;
            int existY = rand.nextInt(height / 2) * 2 + 1;
            exist = new Point(existX, existY);
        } while(entrance.equals(exist) || maze.getTile(exist.x, exist.y) != MazeData.Tile.PATH);
        long end = System.nanoTime();
        generationUsedTime.setText(end - start + "ns");

        mazeCanvas.setWidth(width * MAZE_TILE_SIZE);
        mazeCanvas.setHeight(height * MAZE_TILE_SIZE);
    }

    private void searchMaze() {
        timer.stop();

        Random rand;
        String text = searchSeed.getText();
        if(text.isEmpty()) {
            rand = new Random();
        } else {
            try {
                rand = new Random(Long.parseLong(text));
            } catch(NumberFormatException e) {
                rand = new Random();
            }
        }

        int index = searchCombo.getSelectionModel().getSelectedIndex();
        IMoveMethod moveMethod = searchController.getSearch(index).apply(rand);
        MazeExplorer mover = new MazeExplorer(moveMethod, new Point(entrance), new Point(exist), maze);
        List<Point> log = new ArrayList<Point>();
        log.add(new Point(entrance));
        mover.setOnSearcherMoveed((e) -> {
            IMoveMethod.Direction direction = e.getDirection();
            int x = mover.getCurrentX();
            int y = mover.getCurrentY();
            if(direction.getDx() != 0 && direction.getDy() != 0) {
                int x2 = x;
                int y2 = y;
                if(maze.isMazePath(x - direction.getDx(), y)) {
                    x2 -= direction.getDx();
                } else {
                    y2 -= direction.getDy();
                }
                log.add(new Point(x2, y2));
                if(x2 == exist.x && y2 == exist.y) {
                    return;
                }
            }
            log.add(new Point(x, y));
            if(x == exist.x && y == exist.y) {
                return;
            }
        });

        long start = System.nanoTime();
        while(!log.get(log.size() - 1).equals(exist)) {
            mover.move();
        }
        long end = System.nanoTime();
        searchUsedTime.setText(end - start + "ns");
        setLog(log);
    }

    private void setLog(List<Point> newLog) {
        searcherLog = newLog;
        logSlider.setMax(searcherLog.size() - 1);
        if(logSlider.getValue() == 0) {
            changeLogSlider();
        }
        logSlider.setValue(0);
        searcherLogImage = new ArrayList<Image>();
    }

    @FXML
    public void onGenerationButtonClicked(ActionEvent event) {
        generationMaze();
        searchMaze();
        drawMaze();
        drawLog();
    }

    @FXML
    public void onSearchButtonClicked(ActionEvent event) {
        searchMaze();
        drawMaze();
        drawLog();
    }

    @FXML
    public void onPlayButtonClicked(ActionEvent event) {
        if(timer.isRunning()) {
            timer.stop();
            timer.setTime(Duration.ZERO);
        } else {
            timer.play();
        }
    }

    @FXML
    public void onSpeedButtonClicked(ActionEvent event) {
        timer.setTime(Duration.ZERO);
        if(speed.equals(SLOW)) {
            speed = FAST;
        } else {
            speed = SLOW;
        }
    }

    private void time(Duration duration) {
        if(duration.greaterThan(speed)) {
            timer.setTime(duration.subtract(speed));
            logSlider.setValue(logSlider.getValue() + 1);
        }
    }
}
