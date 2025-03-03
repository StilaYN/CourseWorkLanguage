module ru.langauge.coursework {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires java.compiler;
    requires java.desktop;

    opens ru.langauge.coursework to javafx.fxml;
    exports ru.langauge.coursework;
    exports ru.langauge.coursework.сontroller;
    opens ru.langauge.coursework.сontroller to javafx.fxml;
}