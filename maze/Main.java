package maze;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Maze Generation & Search Algorithm");

        Parent root = FXMLLoader.load(getClass().getResource("javafx/maze.fxml"));
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        primaryStage.setWidth(primaryStage.getWidth() + 20);
        primaryStage.setHeight(primaryStage.getHeight() + 20);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
