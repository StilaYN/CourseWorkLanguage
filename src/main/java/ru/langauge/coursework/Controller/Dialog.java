package ru.langauge.coursework.Controller;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;
import java.util.ResourceBundle;

public class Dialog {

    public static SaveChanges saveChangeDialog(ResourceBundle resourceBundle) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(resourceBundle.getString("save.change.title"));
        alert.setHeaderText(resourceBundle.getString("save.change.header"));
        alert.setContentText(resourceBundle.getString("save.change.text"));

        ButtonType buttonTypeYes = new ButtonType(resourceBundle.getString("save.change.yes"));
        ButtonType buttonTypeNo = new ButtonType(resourceBundle.getString("save.change.no"));
        ButtonType buttonTypeCancel = new ButtonType(resourceBundle.getString("save.change.cancel"));

        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo, buttonTypeCancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == buttonTypeYes) {
                return SaveChanges.YES;
            } else if (result.get() == buttonTypeNo) {
                return SaveChanges.NO;
            } else if (result.get() == buttonTypeCancel) {
                return SaveChanges.CANCEL;
            }
        }
        return SaveChanges.CANCEL;
    }

    public static File openFileDialog(ResourceBundle resourceBundle, Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(resourceBundle.getString("file.chooser.title"));
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                resourceBundle.getString("file.chooser.txt"), "*.txt",
                resourceBundle.getString("file.chooser.java"), "*.java"
        );
        fileChooser.getExtensionFilters().add(extFilter);
        return fileChooser.showOpenDialog(stage);
    }

    public static File saveFileDialog(ResourceBundle resourceBundle, Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(resourceBundle.getString("file.chooser.title.save"));

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                resourceBundle.getString("file.chooser.txt"), "*.txt",
                resourceBundle.getString("file.chooser.java"), "*.java"
        );
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showSaveDialog(stage);
        return file;
    }

}
