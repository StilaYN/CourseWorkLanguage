module ru.langauge.coursework {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires java.compiler;

    opens ru.langauge.coursework to javafx.fxml;
    exports ru.langauge.coursework;
    exports ru.langauge.coursework.Controller;
    opens ru.langauge.coursework.Controller to javafx.fxml;
}