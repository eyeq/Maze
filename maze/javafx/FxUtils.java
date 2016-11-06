package maze.javafx;

import javafx.scene.control.ComboBox;

import java.util.Random;

public class FxUtils {
    public static void randomSelect(Random rand, ComboBox comboBox) {
        comboBox.getSelectionModel().select(getRandomSelectIndex(rand, comboBox));
    }

    public static int getRandomSelectIndex(Random rand, ComboBox comboBox) {
        return rand.nextInt(comboBox.getItems().size());
    }
}
