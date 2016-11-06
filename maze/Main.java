package maze;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import maze.config.Configuration;
import maze.javafx.fxml.Finalizable;

import java.io.File;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Maze Generation & Search Algorithm");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("javafx/maze.fxml"));
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root));
        load(primaryStage);
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            Object controller = loader.getController();
            if(controller instanceof Finalizable) {
                ((Finalizable) controller).finalize(loader.getLocation(), loader.getResources());
            }
            save(primaryStage);
        });
    }

    private void load(Stage primaryStage) {
        Configuration config = new Configuration(new File("windows.config"));
        primaryStage.setX(config.get("x", 10.0));
        primaryStage.setY(config.get("y", 10.0));
        primaryStage.setWidth(config.get("width", 800.0));
        primaryStage.setHeight(config.get("height", 600.0));
        if(config.isChanged()) {
            config.save();
        }
    }

    private void save(Stage primaryStage) {
        Configuration config = new Configuration(new File("windows.config"));
        if(!config.get("save", true)) {
            return;
        }
        config.set("x", primaryStage.getX());
        config.set("y", primaryStage.getY());
        config.set("width", primaryStage.getWidth());
        config.set("height", primaryStage.getHeight());
        config.save();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
