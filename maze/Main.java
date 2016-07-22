package maze;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import maze.javafx.fxml.Finalizable;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Maze Generation & Search Algorithm");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("javafx/maze.fxml"));
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        primaryStage.setWidth(800);
        primaryStage.setHeight(480);

        primaryStage.setOnCloseRequest(event -> {
            Object controller = loader.getController();
            if(controller instanceof Finalizable) {
                ((Finalizable) controller).finalize(loader.getLocation(), loader.getResources());
            }

            // TODO save file
            // System.out.println(primaryStage.getWidth());
            // System.out.println(primaryStage.getHeight());
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
