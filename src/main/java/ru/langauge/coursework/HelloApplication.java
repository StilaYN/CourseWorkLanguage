package ru.langauge.coursework;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.langauge.coursework.Controller.MainWindowController;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        Locale locale = new Locale("ru", "RU");
        ResourceBundle bundle = ResourceBundle.getBundle("locale.locale", locale);

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("main_window_view.fxml"), bundle);
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);

        MainWindowController controller = fxmlLoader.getController();
        controller.setStage(stage);

        stage.setTitle("CodeAnalyzer");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}