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
import maze.config.Configuration;
import maze.generator.*;
import maze.javafx.animation.TimeLineCountUp;
import maze.javafx.event.HandScrollEventListener;
import maze.javafx.fxml.Finalizable;
import maze.javafx.scene.MazeCanvas;
import maze.searcher.IMoveAlgorithm;
import maze.searcher.MazeExplorer;
import maze.util.LFSRRandom;

import java.awt.Point;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.List;

public class Controller implements Initializable, Finalizable {
    @FXML
    private Spinner<Integer> widthSpinner;
    @FXML
    private Spinner<Integer> heightSpinner;

    @FXML
    private CheckBox randomGenerationCheck;
    @FXML
    private CheckBox randomSearchCheck;
    @FXML
    private CheckBox lfsrCheck;
    @FXML
    private ComboBox<String> generationCombo;
    @FXML
    private ComboBox<String> searchCombo;

    @FXML
    private TextField generationSeed;
    @FXML
    private TextField searchSeed;
    @FXML
    private Label generationUsedTimeLabel;
    @FXML
    private Label searchUsedTimeLabel;

    @FXML
    private ComboBox<String> generationNextActionCombo;
    @FXML
    private ComboBox<String> searchNextActionCombo;
    @FXML
    private ComboBox<String> generationNextPlayCombo;
    @FXML
    private ComboBox<String> searchNextPlayCombo;

    @FXML
    private Label stateLabel;
    @FXML
    private ToggleButton playButton;

    @FXML
    private Slider logSlider;
    @FXML
    private Label logLabel;

    @FXML
    private Spinner<Integer> viewSizeSpinner;
    @FXML
    private CheckBox thinCheck;

    @FXML
    private ScrollPane mazePane;
    @FXML
    private MazeCanvas mazeCanvas;

    private final TimeLineCountUp timer;
    private Duration speed;
    private PlayState state;

    private final Random RANDOM;
    private final GenerationManager generationManager;
    private final SearchManager searchManager;
    private MazeData maze;
    private Point entrance;
    private Point exist;
    private List<Point> generatorLog;
    private List<MazeData.Tile> generatorLogTile;
    private List<Image> generatorLogImage;
    private List<Point> searcherLog;
    private List<Image> searcherLogImage;

    private Duration speedSlow;
    private Duration speedFast;

    private int mazeWallThinSize;
    private int logSnapshotInterval;

    public Controller() {
        timer = new TimeLineCountUp(Duration.ONE);
        timer.addEventHandler(e -> this.time(((TimeLineCountUp) e.getSource()).getTime()));

        RANDOM = new Random();
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

        widthSpinner.setEditable(true);
        heightSpinner.setEditable(true);
        viewSizeSpinner.setEditable(true);
        if(widthSpinner.getValueFactory() == null) {
            widthSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 99, 0, 2));
        }
        if(heightSpinner.getValueFactory() == null) {
            heightSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 99, 0, 2));
        }
        if(viewSizeSpinner.getValueFactory() == null) {
            viewSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 0, 1));
        }
        widthSpinner.getEditor().textProperty().addListener((observable, oldValue, newValue) ->
                changeInteger2Spinner(widthSpinner, oldValue, newValue));
        heightSpinner.getEditor().textProperty().addListener((observable, oldValue, newValue) ->
                changeInteger2Spinner(heightSpinner, oldValue, newValue));
        viewSizeSpinner.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            changeIntegerSpinner(viewSizeSpinner, oldValue, newValue);
            if(getMaze() != null) {
                updateMazeTileSize();
            }
        });

        generationCombo.getItems().addAll(generationManager.getGenerationNames());
        searchCombo.getItems().addAll(searchManager.getSearchNames());

        for(NextAction action : NextAction.values()) {
            generationNextActionCombo.getItems().add(action.text);
            searchNextActionCombo.getItems().add(action.text);
        }
        for(NextPlay play : NextPlay.values()) {
            if(play != NextPlay.GENERATION) {
                generationNextPlayCombo.getItems().add(play.text);
            }
            if(play != NextPlay.SEARCH) {
                searchNextPlayCombo.getItems().add(play.text);
            }
        }

        logSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.intValue() > oldValue.intValue() + logSnapshotInterval) {
                logSlider.setValue(oldValue.intValue() + logSnapshotInterval);
                return;
            }
            changeLogSlider();
            drawMaze();
        });

        boolean isPlay = load();
        generationMaze();
        searchMaze();
        updateMazeTileSize();
        if(isPlay) {
            playButton.fire();
        }
    }

    private boolean load() {
        Configuration config = new Configuration(new File("maze.config"));
        widthSpinner.getValueFactory().setValue(config.get("maze-weight", 21));
        heightSpinner.getValueFactory().setValue(config.get("maze-height", 21));

        randomGenerationCheck.setSelected(config.get("generation-random", false));
        lfsrCheck.setSelected(config.get("generation-lfsr", false));
        generationCombo.getSelectionModel().select(config.get("generation-select", FxUtils.getRandomSelectIndex(RANDOM, generationCombo)));
        randomSearchCheck.setSelected(config.get("search-random", false));
        searchCombo.getSelectionModel().select(config.get("search-select", FxUtils.getRandomSelectIndex(RANDOM, searchCombo)));

        generationNextActionCombo.getSelectionModel().select(config.get("generation-next-action", 0));
        generationNextPlayCombo.getSelectionModel().select(config.get("generation-next-play", 0));
        searchNextActionCombo.getSelectionModel().select(config.get("search-next-action", 0));
        searchNextPlayCombo.getSelectionModel().select(config.get("search-next-play", 0));

        boolean isPlay = config.get("play", false);
        speedSlow = Duration.millis(config.get("speed-duration-slow", 100.0));
        speedFast = Duration.millis(config.get("speed-duration-fast", 10.0));
        speed = config.get("speed", false) ? speedFast : speedSlow;
        state = config.get("play-state", PlayState.GENERATION.text).equals(PlayState.GENERATION.text) ? PlayState.GENERATION : PlayState.SEARCH;

        viewSizeSpinner.getValueFactory().setValue(config.get("maze-size-tile", 10));
        mazeWallThinSize = config.get("maze-size-wall-thin", 2);
        thinCheck.setSelected(config.get("maze-wall-thin", false));
        logSnapshotInterval = config.get("log-snapshot-interval", 1000);
        if(config.isChanged()) {
            config.save();
        }
        return isPlay;
    }

    @Override
    public void finalize(URL location, ResourceBundle resources) {
        Configuration config = new Configuration(new File("maze.config"));
        if(!config.get("save", true)) {
            return;
        }
        config.set("maze-weight", widthSpinner.getValue());
        config.set("maze-height", heightSpinner.getValue());

        config.set("generation-random", randomGenerationCheck.isSelected());
        config.set("generation-lfsr", lfsrCheck.isSelected());
        config.set("generation-select", generationCombo.getSelectionModel().getSelectedIndex());
        config.set("search-random", randomSearchCheck.isSelected());
        config.set("search-select", searchCombo.getSelectionModel().getSelectedIndex());

        config.set("generation-next-action", generationNextActionCombo.getSelectionModel().getSelectedIndex());
        config.set("generation-next-play", generationNextPlayCombo.getSelectionModel().getSelectedIndex());
        config.set("search-next-action", searchNextActionCombo.getSelectionModel().getSelectedIndex());
        config.set("search-next-play", searchNextPlayCombo.getSelectionModel().getSelectedIndex());

        config.set("play", playButton.isSelected());
        config.set("speed-duration-slow", speedSlow.toMillis());
        config.set("speed-duration-fast", speedFast.toMillis());
        config.set("speed", speed.equals(speedFast));
        config.set("play-state", getState().text);

        config.set("maze-size-tile", viewSizeSpinner.getValue());
        config.set("maze-size-wall-thin", mazeWallThinSize);
        config.set("maze-wall-thin", thinCheck.isSelected());
        config.set("log-snapshot-interval", logSnapshotInterval);
        config.save();
    }

    public PlayState getState() {
        return state;
    }

    public void setAndUpdateState(PlayState state) {
        boolean isRunning = timer.isRunning();
        timer.stop();

        this.state = state;
        stateLabel.setText(state.text);

        int size;
        if(state == PlayState.GENERATION) {
            // maze.getWidth() * maze.getHeight()
            size = generatorLog.size() - getMaze().getWidth() * getMaze().getHeight() + 1;
        } else {
            size = searcherLog.size();
        }
        logSlider.setMax(size - 1);
        if(logSlider.getValue() == 0) {
            changeLogSlider();
        }

        timer.setTime(Duration.ZERO);
        logSlider.setValue(0);
        initGeneratorLogImage();
        initSearcherLogImage();
        drawMaze();
        if(isRunning) {
            timer.play();
        }
    }

    public MazeData getMaze() {
        return maze;
    }

    private void setMaze(MazeData mazeData) {
        maze = mazeData;
    }

    public void initGeneratorLogImage() {
        generatorLogImage = new ArrayList<>();
    }

    public void initSearcherLogImage() {
        searcherLogImage = new ArrayList<>();
    }

    private void drawMaze() {
        GraphicsContext graphics = mazeCanvas.getGraphicsContext2D();
        final int N = (int) logSlider.getValue();

        if(getState() == PlayState.GENERATION) {
            int size = generatorLogImage.size();
            int start = Math.min(size - 1, N / logSnapshotInterval);
            if(start < 0) {
                start = 0;
                // graphics.setFill(Color.SILVER);
                // graphics.fillRect(0, 0, mazeCanvas.getWidth(), mazeCanvas.getHeight());
            } else {
                Image image = generatorLogImage.get(start);
                graphics.drawImage(image, 0, 0);
            }
            // maze.getWidth() * maze.getHeight()
            for(int i = start * logSnapshotInterval; i < N + getMaze().getWidth() * getMaze().getHeight(); i++) {
                Point point = generatorLog.get(i);
                MazeData.Tile tile = generatorLogTile.get(i);
                mazeCanvas.draw(graphics, point.x, point.y, tile);
                if(i % logSnapshotInterval == 0 && size == i / logSnapshotInterval) {
                    Image image = mazeCanvas.snapshot(null, null);
                    generatorLogImage.add(image);
                    size++;
                }
            }
        } else {
            int size = searcherLogImage.size();
            int start = Math.min(size - 1, N / logSnapshotInterval);
            if(start < 0) {
                start = 0;
                mazeCanvas.draw(getMaze());
            } else {
                Image image = searcherLogImage.get(start);
                graphics.drawImage(image, 0, 0);
            }
            for(int i = start * logSnapshotInterval; i < N; i++) {
                Point point = searcherLog.get(i);
                mazeCanvas.drawTile(graphics, point.x, point.y, Color.GRAY);
                if(i % logSnapshotInterval == 0 && size == i / logSnapshotInterval) {
                    Image image = mazeCanvas.snapshot(null, null);
                    searcherLogImage.add(image);
                    size++;
                }
            }

            Point point = searcherLog.get(N);
            mazeCanvas.drawTile(graphics, point.x, point.y, Color.RED);

            mazeCanvas.drawTile(graphics, entrance.x, entrance.y, Color.BLUE, "S", Color.WHITE);
            mazeCanvas.drawTile(graphics, exist.x, exist.y, Color.BLUE, "G", Color.WHITE);
        }
    }

    private void generationMaze() {
        int width = widthSpinner.getValue();
        width += (width + 1) % 2;
        int height = heightSpinner.getValue();
        height += (height + 1) % 2;

        Random rand;
        String text = generationSeed.getText();
        Long seed = null;
        if(!text.isEmpty()) {
            try {
                seed = Long.parseLong(text);
            } catch(NumberFormatException ignored) {
            }
        }
        boolean isLfsr = lfsrCheck.isSelected();
        if(seed == null) {
            rand = isLfsr ? new LFSRRandom() : new Random();
        } else {
            rand = isLfsr ? new LFSRRandom(seed) : new Random(seed);
        }

        if(randomGenerationCheck.isSelected()) {
            FxUtils.randomSelect(RANDOM, generationCombo);
        }
        int index = generationCombo.getSelectionModel().getSelectedIndex();
        IMazeBuilder mazeBuilder = generationManager.getGeneration(index).apply(new int[]{width, height}, rand);
        MazeDetector mazeDetector = new MazeDetector(mazeBuilder);
        List<Point> log = new ArrayList<>();
        List<MazeData.Tile> logTile = new ArrayList<>();

        if(mazeBuilder instanceof AbstractMazeBuilder) {
            ((AbstractMazeBuilder) mazeBuilder).setOnMazeChanged(e -> {
                Point point = new Point(e.getX(), e.getY());
                MazeData.Tile tile = e.getTile();

                log.add(point);
                logTile.add(tile);
            });
        }

        entrance = new Point(1, 1);

        long start = System.nanoTime();
        setMaze(mazeDetector.construct());
        do {
            int existX = rand.nextInt(width / 2) * 2 + 1;
            int existY = rand.nextInt(height / 2) * 2 + 1;
            exist = new Point(existX, existY);
        } while(entrance.equals(exist) || getMaze().getTile(exist.x, exist.y) != MazeData.Tile.PATH);
        long end = System.nanoTime();

        generationUsedTimeLabel.setText(end - start + "ns");

        setGeneratorLog(log, logTile);
    }

    private void searchMaze() {
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

        if(randomSearchCheck.isSelected()) {
            FxUtils.randomSelect(RANDOM, searchCombo);
        }
        int index = searchCombo.getSelectionModel().getSelectedIndex();
        IMoveAlgorithm moveMethod = searchManager.getSearch(index).apply(rand);
        MazeExplorer mover = new MazeExplorer(moveMethod, new Point(entrance), new Point(exist), getMaze());
        List<Point> log = new ArrayList<>();
        log.add(new Point(entrance));

        mover.setOnSearcherMoved(e -> {
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
                if(getMaze().isMazePath(x - direction.getDx(), y)) {
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

        searchUsedTimeLabel.setText(end - start + "ns");

        setSearcherLog(log);
    }

    private void setGeneratorLog(List<Point> newLog, List<MazeData.Tile> newLogTile) {
        generatorLog = newLog;
        generatorLogTile = newLogTile;
    }

    private void setSearcherLog(List<Point> newLog) {
        searcherLog = newLog;
    }

    public void resizeCanvas() {
        mazeCanvas.resize(getMaze());
    }

    public void updateMazeTileSize() {
        int mazeTileSize = viewSizeSpinner.getValue();

        mazeCanvas.setTileSize(mazeTileSize);
        if(thinCheck.isSelected()) {
            mazeCanvas.setWallWeight(Math.min(mazeWallThinSize, mazeTileSize));
        } else {
            mazeCanvas.setWallWeight(mazeTileSize);
        }
        resizeCanvas();

        initGeneratorLogImage();
        initSearcherLogImage();
        drawMaze();
    }

    private void changeInteger2Spinner(Spinner<Integer> spinner, String oldValue, String newValue) {
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

    private void changeIntegerSpinner(Spinner<Integer> spinner, String oldValue, String newValue) {
        int value;
        try {
            value = new Integer(newValue);
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
        setAndUpdateState(getState());
    }

    @FXML
    public void onSearchButtonClicked() {
        searchMaze();
        setAndUpdateState(getState());
    }

    @FXML
    public void onStateButtonClicked() {
        if(getState() == PlayState.GENERATION) {
            setAndUpdateState(PlayState.SEARCH);
        } else {
            setAndUpdateState(PlayState.GENERATION);
        }
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
        if(speed.equals(speedSlow)) {
            speed = speedFast;
        } else {
            speed = speedSlow;
        }
    }

    @FXML
    public void onDisplayChanged(ActionEvent event) {
        updateMazeTileSize();
    }

    private void time(Duration duration) {
        if(duration.greaterThan(speed)) {
            timer.setTime(duration.subtract(speed));
            if(!logSlider.isValueChanging()) {
                double value = logSlider.getValue();
                if(value == logSlider.getMax()) {
                    logSlider.setValue(value);
                    playFinished();
                    return;
                }
                if(value % 2 == 0) {
                    value++;
                }
                logSlider.setValue(value + 1);
            }
        }
    }

    private void playFinished() {
        String actionText;
        String playText;
        if(getState() == PlayState.GENERATION) {
            actionText = generationNextActionCombo.getSelectionModel().getSelectedItem();
            playText = generationNextPlayCombo.getSelectionModel().getSelectedItem();
        } else {
            actionText = searchNextActionCombo.getSelectionModel().getSelectedItem();
            playText = searchNextPlayCombo.getSelectionModel().getSelectedItem();
        }
        NextAction action = NextAction.NONE;
        NextPlay play = NextPlay.NONE;
        for(NextAction nextAction : NextAction.values()) {
            if(nextAction.text.equals(actionText)) {
                action = nextAction;
                break;
            }
        }
        for(NextPlay nextPlay : NextPlay.values()) {
            if(nextPlay.text.equals(playText)) {
                play = nextPlay;
                break;
            }
        }

        boolean isInit = false;
        switch(action) {
        case GENERATION:
            generationMaze();
            resizeCanvas();
        case SEARCH:
            searchMaze();
            isInit = true;
            break;
        }
        switch(play) {
        case NONE:
            if(isInit) {
                setAndUpdateState(getState());
            }
            break;
        case GENERATION:
            setAndUpdateState(PlayState.GENERATION);
            break;
        case SEARCH:
            setAndUpdateState(PlayState.SEARCH);
            break;
        }
    }

    private enum PlayState {
        GENERATION("再生中：生成"),
        SEARCH("再生中：探索"),
        ;

        final String text;

        PlayState(String text) {
            this.text = text;
        }
    }

    private enum NextAction {
        NONE("何もしない"),
        GENERATION("新規生成"),
        SEARCH("新規探索"),
        ;

        final String text;

        NextAction(String text) {
            this.text = text;
        }
    }

    private enum NextPlay {
        NONE("何もしない"),
        GENERATION("「再生中：生成」に切替"),
        SEARCH("「再生中：探索」に切替"),
        ;

        final String text;

        NextPlay(String text) {
            this.text = text;
        }
    }
}
