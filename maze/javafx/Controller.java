package maze.javafx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import maze.MazeData;
import maze.generator.*;
import maze.javafx.animation.TimeLineCountUp;
import maze.javafx.event.HandScrollEventListener;
import maze.javafx.scene.MazeCanvas;
import maze.searcher.IMoveAlgorithm;
import maze.searcher.MazeExplorer;
import maze.util.LFSRRandom;

import java.awt.Point;
import java.net.URL;
import java.util.*;
import java.util.List;

public class Controller implements Initializable {
    public static final int MAZE_TILE_SIZE = 10;
    public static final int MAZE_WALL_THIN_SIZE = 2;
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
    private CheckBox lfsrCheck;
    @FXML
    private CheckBox stopCheck;

    @FXML
    private Label generationUsedTime;
    @FXML
    private Label searchUsedTime;

    @FXML
    private ToggleButton playButton;

    @FXML
    private ScrollPane mazePane;
    @FXML
    private MazeCanvas mazeCanvas;

    private final TimeLineCountUp timer;
    private Duration speed;

    private final GenerationManager generationManager;
    private final SearchManager searchManager;
    private MazeData maze;
    private Point entrance;
    private Point exist;
    private List<Point> searcherLog;
    private List<Image> searcherLogImage;

    public Controller() {
        timer = new TimeLineCountUp(Duration.ONE);
        timer.addEventHandler(e -> this.time(((TimeLineCountUp) e.getSource()).getTime()));
        speed = SLOW;

        generationManager = new GenerationManager();
        searchManager = new SearchManager();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourcebundle) {
        HandScrollEventListener hansScroll = new HandScrollEventListener(4);
        mazeCanvas.setOnMouseReleased((e) -> ((Node) e.getSource()).setCursor(Cursor.DEFAULT));
        mazeCanvas.setOnMousePressed((e) -> {
            ((Node) e.getSource()).setCursor(Cursor.CLOSED_HAND);
            hansScroll.mousePressed(e);
        });
        mazeCanvas.setOnMouseDragged((e) -> hansScroll.mouseDragged(e, mazePane));
        mazeCanvas.setTileSize(MAZE_TILE_SIZE);
        mazeCanvas.setWallWeight(MAZE_TILE_SIZE);

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
        generationCombo.getItems().addAll(generationManager.getGenerationNames());
        generationCombo.getSelectionModel().select(rand.nextInt(generationCombo.getItems().size()));
        searchCombo.getItems().addAll(searchManager.getSearchNames());
        searchCombo.getSelectionModel().select(rand.nextInt(searchCombo.getItems().size()));

        logSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.intValue() > oldValue.intValue() + LOG_SNAPSHOT_INTERVAL) {
                logSlider.setValue(oldValue.intValue() + LOG_SNAPSHOT_INTERVAL);
                return;
            }
            changeLogSlider();
            drawLog();
        });

        onGenerationButtonClicked();
    }

    public MazeData getMaze() {
        return maze;
    }

    private void drawMaze() {
        mazeCanvas.draw(getMaze());
    }

    public void initSearcherLogImage() {
        searcherLogImage = new ArrayList<>();
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
            mazeCanvas.drawTile(graphics, point.x, point.y, Color.GRAY);
            if(i % LOG_SNAPSHOT_INTERVAL == 0 && size == i / LOG_SNAPSHOT_INTERVAL) {
                Image image = mazeCanvas.snapshot(null, null);
                searcherLogImage.add(image);
                size++;
            }
        }

        Point point = searcherLog.get(n);
        mazeCanvas.drawTile(graphics, point.x, point.y, Color.RED);

        mazeCanvas.drawTile(graphics, entrance.x, entrance.y, Color.BLUE, "S", Color.WHITE);
        mazeCanvas.drawTile(graphics, exist.x, exist.y, Color.BLUE, "G", Color.WHITE);
    }

    private void generationMaze() {
        int width = widthSpinner.getValue();
        width += (width + 1) % 2;
        int height = heightSpinner.getValue();
        height += (height + 1) % 2;

        boolean isLfsr = lfsrCheck.isSelected();
        Random rand;
        String text = generationSeed.getText();
        Long seed = null;
        if(!text.isEmpty()) {
            try {
                seed = Long.parseLong(text);
            } catch(NumberFormatException ignored) {
            }
        }
        if(seed == null) {
            rand = isLfsr ? new LFSRRandom() : new Random();
        } else {
            rand = isLfsr ? new LFSRRandom(seed) : new Random(seed);
        }

        int index = generationCombo.getSelectionModel().getSelectedIndex();
        IMazeBuilder mazeBuilder = generationManager.getGeneration(index).apply(new int[]{width, height}, rand);
        MazeDetector mazeDetector = new MazeDetector(mazeBuilder);

        entrance = new Point(1, 1);

        long start = System.nanoTime();
        maze = mazeDetector.construct();
        do {
            int existX = rand.nextInt(width / 2) * 2 + 1;
            int existY = rand.nextInt(height / 2) * 2 + 1;
            exist = new Point(existX, existY);
        } while(entrance.equals(exist) || maze.getTile(exist.x, exist.y) != MazeData.Tile.PATH);
        long end = System.nanoTime();

        generationUsedTime.setText(end - start + "ns");
    }

    private void searchMaze() {
        if(stopCheck.isSelected()) {
            timer.stop();
            playButton.setSelected(false);
        }

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
        IMoveAlgorithm moveMethod = searchManager.getSearch(index).apply(rand);
        MazeExplorer mover = new MazeExplorer(moveMethod, new Point(entrance), new Point(exist), maze);
        List<Point> log = new ArrayList<Point>();
        log.add(new Point(entrance));
        mover.setOnSearcherMoveed((e) -> {
            IMoveAlgorithm.Direction direction = e.getDirection();
            if(direction == null) {
                return;
            }
            int x = mover.getCurrentX();
            int y = mover.getCurrentY();
            // 斜め移動
            if(direction.getDx() != 0 && direction.getDy() != 0) {
                int preX = x;
                int preY = y;
                if(maze.isMazePath(x - direction.getDx(), y)) {
                    preX -= direction.getDx();
                } else {
                    preY -= direction.getDy();
                }
                log.add(new Point(preX, preY));
                if(preX == exist.x && preY == exist.y) {
                    return;
                }
            }

            log.add(new Point(x, y));
        });

        long start = System.nanoTime();
        while(!log.get(log.size() - 1).equals(exist)) {
            if(!mover.move()) {
                break;
            }
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
    }

    public void resizeCanvas() {
        mazeCanvas.resize(getMaze());
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

    @FXML
    public void onGenerationButtonClicked() {
        generationMaze();
        resizeCanvas();
        searchMaze();
        initSearcherLogImage();
        drawMaze();
        drawLog();
    }

    @FXML
    public void onSearchButtonClicked() {
        searchMaze();
        initSearcherLogImage();
        drawMaze();
        drawLog();
    }

    @FXML
    public void onPlayButtonClicked() {
        if(timer.isRunning()) {
            timer.stop();
            timer.setTime(Duration.ZERO);
        } else {
            timer.play();
        }
    }

    @FXML
    public void onSpeedButtonClicked() {
        timer.setTime(Duration.ZERO);
        if(speed.equals(SLOW)) {
            speed = FAST;
        } else {
            speed = SLOW;
        }
    }

    @FXML
    public void onDisplayChanged(ActionEvent event) {
        if(((CheckBox) event.getSource()).isSelected()) {
            mazeCanvas.setWallWeight(MAZE_WALL_THIN_SIZE);
        } else {
            mazeCanvas.setWallWeight(MAZE_TILE_SIZE);
        }
        resizeCanvas();
        initSearcherLogImage();
        drawMaze();
        drawLog();
    }

    private void time(Duration duration) {
        if(duration.greaterThan(speed)) {
            timer.setTime(duration.subtract(speed));
            if(!logSlider.isValueChanging()) {
                double value = logSlider.getValue();
                if(value % 2 == 0) {
                    value++;
                }
                logSlider.setValue(value + 1);
            }
        }
    }
}
