package ru.langauge.coursework.сontroller;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;
import ru.langauge.coursework.core.mapper.TokenMapper;
import ru.langauge.coursework.core.service.FileService;
import ru.langauge.coursework.core.service.TokenScanner;
import ru.langauge.coursework.view_logic.CommandManager;
import ru.langauge.coursework.view_logic.ErrorModel;
import ru.langauge.coursework.view_logic.TextEditCommand;
import ru.langauge.coursework.view_logic.TokenInfo;

import javax.lang.model.SourceVersion;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;

public class MainWindowController implements Initializable {

    @FXML
    private TextArea textArea;
    @FXML
    private TextFlow lineNumbers;
    @FXML
    private ScrollPane workAreaScrollPane;
    @FXML
    private TextFlow textAreaHighlights;
    @FXML
    private MenuBar menuBar;
    @FXML
    private Button newButton;
    @FXML
    private Button openButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button undoButton;
    @FXML
    private Button redoButton;
    @FXML
    private Button copyButton;
    @FXML
    private Button cutButton;
    @FXML
    private Button pasteButton;
    @FXML
    private TableView<ErrorModel> errorTableView;

    @FXML
    private TableView<TokenInfo> tokenTableView;

    @FXML
    private Tab errorTab;

    @FXML
    private Tab tokenTab;

    private Timeline scrollTimeline = new Timeline();

    private ResourceBundle resourceBundle;

    private Stage stage;

    private HostServices hostServices;

    private final CommandManager commandManager = new CommandManager(30);

    private final TokenScanner tokenParserService = new TokenScanner();

    private final TokenMapper tokenMapper = new TokenMapper();

    private FileService fileService;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        fileService = new FileService(commandManager);

        resourceBundle = resources;

        initializeMenuBar();
        initializeButtonAction();
        initializeTableView();
        updateUI();
        setupKeyboardHotkeys();

        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!commandManager.getCommandExecution()) {
                commandManager.executeCommand(new TextEditCommand(oldValue, newValue, textArea));
            }
            updateLineNumbers(newValue);
            updateHighlight(newValue);
            updateTokenTable(newValue);

        });

        textArea.caretPositionProperty().addListener((observable, oldValue, newValue) -> {
            int caretPosition = textArea.getCaretPosition();
            int totalLength = textArea.getLength();

            if (totalLength > 0) {
                double viewportHeight = workAreaScrollPane.getViewportBounds().getHeight();
                double textAreaHeight = textArea.getHeight();
                double caretRelativePosition = caretPosition / (double) totalLength;
                double targetVvalue = caretRelativePosition - (viewportHeight / textAreaHeight) / 10;
                targetVvalue = Math.min(1, Math.max(0, targetVvalue));
                scrollTimeline.stop();
                scrollTimeline.getKeyFrames().clear();

                scrollTimeline.getKeyFrames().add(
                        new KeyFrame(Duration.millis(200),
                                new KeyValue(workAreaScrollPane.vvalueProperty(), targetVvalue))
                );

                scrollTimeline.play();
            }
        });

        setupDragAndDrop();
        updateLineNumbers(textArea.getText());
        updateHighlight(textArea.getText());
    }


    private void initializeButtonAction() {
        newButton.setOnAction(e -> createNewFile());
        openButton.setOnAction(e -> openFile());
        saveButton.setOnAction(e -> saveFile());
        undoButton.setOnAction(e -> commandManager.cancelCommand());
        redoButton.setOnAction(e -> commandManager.repeatCommand());
        copyButton.setOnAction(e -> textArea.copy());
        cutButton.setOnAction(e -> textArea.cut());
        pasteButton.setOnAction(e -> textArea.paste()
        );
    }

    private void initializeMenuBar() {

        Menu fileMenu = new Menu();
        fileMenu.textProperty().bind(StringPropertyWithLocale.FILE.getProperty());
        MenuItem createMenuItem = new MenuItem();
        createMenuItem.setOnAction(e -> createNewFile());
        createMenuItem.textProperty().bind(StringPropertyWithLocale.CREATE.getProperty());
        MenuItem openMenuItem = new MenuItem();
        openMenuItem.setOnAction(e -> openFile());
        openMenuItem.textProperty().bind(StringPropertyWithLocale.OPEN.getProperty());
        MenuItem saveMenuItem = new MenuItem();
        saveMenuItem.setOnAction(e -> fileService.save(textArea.getText()));
        saveMenuItem.textProperty().bind(StringPropertyWithLocale.SAVE.getProperty());
        MenuItem saveAsMenuItem = new MenuItem();
        saveAsMenuItem.setOnAction(e -> saveFile());
        saveAsMenuItem.textProperty().bind(StringPropertyWithLocale.SAVE_AS.getProperty());
        MenuItem exitMenuItem = new MenuItem();
        exitMenuItem.textProperty().bind(StringPropertyWithLocale.EXIT.getProperty());
        exitMenuItem.setOnAction(e -> {
            if (saveOrCancel()) {
                Platform.exit();
            }
        });
        fileMenu.getItems().addAll(createMenuItem, openMenuItem, saveMenuItem, saveAsMenuItem, exitMenuItem);

        Menu editMenu = new Menu();
        editMenu.textProperty().bind(StringPropertyWithLocale.EDIT.getProperty());
        MenuItem undoMenuItem = new MenuItem();
        editMenu.setOnAction(e -> commandManager.cancelCommand());
        undoMenuItem.textProperty().bind(StringPropertyWithLocale.UNDO.getProperty());
        MenuItem repeatMenuItem = new MenuItem();
        repeatMenuItem.setOnAction(e -> commandManager.repeatCommand());
        repeatMenuItem.textProperty().bind(StringPropertyWithLocale.REPEAT.getProperty());
        MenuItem cutMenuItem = new MenuItem();
        cutMenuItem.setOnAction(e -> textArea.cut());
        cutMenuItem.textProperty().bind(StringPropertyWithLocale.CUT.getProperty());
        MenuItem copyMenuItem = new MenuItem();
        copyMenuItem.setOnAction(e -> textArea.copy());
        copyMenuItem.textProperty().bind(StringPropertyWithLocale.COPY.getProperty());
        MenuItem pasteMenuItem = new MenuItem();
        pasteMenuItem.setOnAction(e -> textArea.paste());
        pasteMenuItem.textProperty().bind(StringPropertyWithLocale.PASTE.getProperty());
        MenuItem deleteMenuItem = new MenuItem();
        deleteMenuItem.setOnAction(
                e -> {
                    textArea.deleteText(
                            textArea.getSelection().getStart(),
                            textArea.getSelection().getEnd()
                    );
                }
        );
        deleteMenuItem.textProperty().bind(StringPropertyWithLocale.DELETE.getProperty());
        MenuItem selectMenuItem = new MenuItem();
        selectMenuItem.setOnAction(e -> textArea.selectAll());
        selectMenuItem.textProperty().bind(StringPropertyWithLocale.SELECT.getProperty());
        editMenu.getItems().addAll(
                undoMenuItem, repeatMenuItem, cutMenuItem, copyMenuItem,
                pasteMenuItem, deleteMenuItem, selectMenuItem
        );

        Menu textMenu = new Menu();
        textMenu.textProperty().bind(StringPropertyWithLocale.TEXT.getProperty());
        MenuItem taskMenuItem = new MenuItem();
        taskMenuItem.textProperty().bind(StringPropertyWithLocale.TASK.getProperty());
        MenuItem grammarMenuItem = new MenuItem();
        grammarMenuItem.textProperty().bind(StringPropertyWithLocale.GRAMMAR.getProperty());
        MenuItem grammarClassMenuItem = new MenuItem();
        grammarClassMenuItem.textProperty().bind(StringPropertyWithLocale.GRAMMAR_CLASS.getProperty());
        MenuItem methodMenuItem = new MenuItem();
        methodMenuItem.textProperty().bind(StringPropertyWithLocale.METHOD.getProperty());
        MenuItem diagnosisMenuItem = new MenuItem();
        diagnosisMenuItem.textProperty().bind(StringPropertyWithLocale.DIAGNOSIS.getProperty());
        MenuItem testMenuItem = new MenuItem();
        testMenuItem.textProperty().bind(StringPropertyWithLocale.TEST.getProperty());
        MenuItem literatureMenuItem = new MenuItem();
        literatureMenuItem.textProperty().bind(StringPropertyWithLocale.LITERATURE.getProperty());
        MenuItem sourceMenuItem = new MenuItem();
        sourceMenuItem.textProperty().bind(StringPropertyWithLocale.SOURCE_CODE.getProperty());
        textMenu.getItems().addAll(
                taskMenuItem, grammarMenuItem, grammarClassMenuItem, methodMenuItem,
                diagnosisMenuItem, testMenuItem, literatureMenuItem, sourceMenuItem
        );

        Menu runMenu = new Menu();
        runMenu.textProperty().bind(StringPropertyWithLocale.RUN.getProperty());
        MenuItem runMenuItem = new MenuItem();
        runMenuItem.textProperty().bind(StringPropertyWithLocale.RUN.getProperty());
        runMenu.getItems().addAll(runMenuItem);

        Menu languageMenu = new Menu();
        languageMenu.textProperty().bind(StringPropertyWithLocale.LANGUAGE.getProperty());
        MenuItem englishMenuItem = new MenuItem();
        englishMenuItem.setOnAction(e -> setLocale(new Locale("en", "EN")));
        englishMenuItem.textProperty().bind(StringPropertyWithLocale.ENGLISH.getProperty());
        MenuItem russianMenuItem = new MenuItem();
        russianMenuItem.setOnAction(e -> setLocale(new Locale("ru", "RU")));
        russianMenuItem.textProperty().bind(StringPropertyWithLocale.RUSSIAN.getProperty());
        languageMenu.getItems().addAll(englishMenuItem, russianMenuItem);

        Menu helpMenu = new Menu();
        helpMenu.textProperty().bind(StringPropertyWithLocale.HELP.getProperty());
        MenuItem helpMenuItem = new MenuItem();
        helpMenuItem.setOnAction(event -> openPageInBrowser("/pages/help.html"));
        helpMenuItem.textProperty().bind(StringPropertyWithLocale.HELP_HELP.getProperty());
        MenuItem aboutMenuItem = new MenuItem();
        aboutMenuItem.setOnAction(event -> openPageInBrowser("/pages/about.html"));
        aboutMenuItem.textProperty().bind(StringPropertyWithLocale.ABOUT.getProperty());
        helpMenu.getItems().addAll(helpMenuItem, aboutMenuItem);

        menuBar.getMenus().addAll(fileMenu, editMenu, textMenu, runMenu, languageMenu, helpMenu);
    }

    private void setupDragAndDrop() {
        textArea.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        textArea.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            boolean success = false;
            if (dragboard.hasFiles()) {
                File file = dragboard.getFiles().get(0);
                if (file != null && file.isFile() && file.getName().endsWith(".txt") || file.getName().endsWith(".java")) {
                    if (saveOrCancel()) {
                        textArea.setText(fileService.read(file.getAbsolutePath()));
                    }
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        textArea.setOnDragExited(event -> {
            event.consume();
        });
    }

    private void initializeTableView() {
        TableColumn<ErrorModel, String> pathColumn = new TableColumn<>();
        pathColumn.textProperty().bind(StringPropertyWithLocale.PATH.getProperty());
        pathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));
        TableColumn<ErrorModel, Integer> line = new TableColumn<>();
        line.textProperty().bind(StringPropertyWithLocale.LINE.getProperty());
        line.setCellValueFactory(new PropertyValueFactory<>("line"));
        TableColumn<ErrorModel, Integer> column = new TableColumn<>();
        column.textProperty().bind(StringPropertyWithLocale.COLUMN.getProperty());
        column.setCellValueFactory(new PropertyValueFactory<>("column"));
        TableColumn<ErrorModel, String> messageColumn = new TableColumn<>();
        messageColumn.textProperty().bind(StringPropertyWithLocale.MESSAGE.getProperty());
        messageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));

        errorTableView.getColumns().addAll(pathColumn, line, column, messageColumn);

        TableColumn<TokenInfo, String> tokenTypeColumn = new TableColumn<>();
        tokenTypeColumn.textProperty().bind(StringPropertyWithLocale.TOKEN_TYPE.getProperty());
        tokenTypeColumn.setCellValueFactory(new PropertyValueFactory<>("tokenName"));
        TableColumn<TokenInfo, String> valueColumn = new TableColumn<>();
        valueColumn.textProperty().bind(StringPropertyWithLocale.VALUE.getProperty());
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        TableColumn<TokenInfo, Integer> lineNumberColumn = new TableColumn<>();
        lineNumberColumn.textProperty().bind(StringPropertyWithLocale.LINE.getProperty());
        lineNumberColumn.setCellValueFactory(new PropertyValueFactory<>("lineNumber"));
        TableColumn<TokenInfo, Integer> startColumnColumn = new TableColumn<>();
        startColumnColumn.textProperty().bind(StringPropertyWithLocale.START_COLUMN.getProperty());
        startColumnColumn.setCellValueFactory(new PropertyValueFactory<>("startColumn"));
        TableColumn<TokenInfo, Integer> endColumnColumn = new TableColumn<>();
        endColumnColumn.textProperty().bind(StringPropertyWithLocale.END_COLUMN.getProperty());
        endColumnColumn.setCellValueFactory(new PropertyValueFactory<>("endColumn"));
        tokenTableView.getColumns().addAll(tokenTypeColumn, valueColumn, lineNumberColumn, startColumnColumn, endColumnColumn);

        tokenTab.textProperty().bind(StringPropertyWithLocale.TAB_TOKEN.getProperty());
        errorTab.textProperty().bind(StringPropertyWithLocale.TAB_ERROR.getProperty());
    }

    private void setupKeyboardHotkeys() {
        KeyCombination copyKeyCombination = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);
        KeyCombination pasteKeyCombination = new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN);
        KeyCombination cutKeyCombination = new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN);
        KeyCombination cancelKeyCombination = new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN);
        KeyCombination redoKeyCombination = new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN);
        KeyCombination newKeyCombination = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
        KeyCombination saveAsKeyCombination = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
        KeyCombination openNewFile = new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN);

        textArea.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (cancelKeyCombination.match(keyEvent)) {
                keyEvent.consume();
                commandManager.cancelCommand();
            } else if (redoKeyCombination.match(keyEvent)) {
                keyEvent.consume();
                commandManager.redoCommand();
            } else if (newKeyCombination.match(keyEvent)) {
                createNewFile();
            } else if (saveAsKeyCombination.match(keyEvent)) {
                saveFile();
            } else if (openNewFile.match(keyEvent)) {
                openFile();
            }
        });

        textArea.addEventHandler(KeyEvent.KEY_RELEASED, keyEvent -> {
            if (copyKeyCombination.match(keyEvent)) {
                textArea.copy();
            } else if (pasteKeyCombination.match(keyEvent)) {
                textArea.paste();
            } else if (cutKeyCombination.match(keyEvent)) {
                textArea.cut();
            }
        });


    }

    private void updateTokenTable(String text) {
        tokenTableView.setItems(
                FXCollections.observableList(
                        tokenMapper.map(
                                tokenParserService.getTokens(text),
                                resourceBundle
                        )
                )
        );
    }

    private void updateUI() {
        for (StringPropertyWithLocale localeProperty : StringPropertyWithLocale.values()) {
            localeProperty.getProperty().setValue(
                    resourceBundle.getString(localeProperty.getKey())
            );
        }
    }

    private void setLocale(Locale locale) {
        resourceBundle = ResourceBundle.getBundle("locale.locale", locale);
        updateUI();
    }

    private void updateLineNumbers(String text) {
        lineNumbers.getChildren().clear();
        String[] lines = text.split("\n");

        for (int i = 1; i <= lines.length; i++) {
            Text lineNumber = new Text(i + "\n");
            lineNumbers.getChildren().add(lineNumber);
        }
        if (lines.length == 0) {
            Text lineNumber = new Text("1\n");
            lineNumbers.getChildren().add(lineNumber);
        }
    }

    private void updateHighlight(String text) {
        textAreaHighlights.getChildren().clear();
        String[] lines = text.split("\n");

        for (String line : lines) {
            String[] words = line.split("\\s+");
            String[] spaces = line.split("[^ ]+");
            spaces = Arrays.stream(spaces)
                    .filter(s -> !s.isEmpty())
                    .toArray(String[]::new);

            for (int i = 0; i < words.length; i++) {
                Text textNode = (i < words.length - 1) ? new Text(words[i] + spaces[i]) : new Text(words[i]);
                if (isKeyword(words[i])) {
                    textNode.setStyle("-fx-fill: blue; -fx-font-weight: bold;");
                }
                textAreaHighlights.getChildren().add(textNode);
            }
            textAreaHighlights.getChildren().add(new Text("\n"));
        }
    }

    private boolean isKeyword(String word) {
        return SourceVersion.isKeyword(word);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        stage.setOnCloseRequest(event -> {
            if (!saveOrCancel()) {
                event.consume();
            }
        });
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    private boolean saveOrCancel() {
        if (!commandManager.isSaveState()) {
            switch (Dialog.saveChangeDialog(resourceBundle)) {
                case CANCEL -> {
                    return false;
                }
                case YES -> {
                    fileService.save(textArea.getText());
                    break;
                }
                case NO -> {
                }
            }
        }
        return true;
    }

    private void openFile() {
        File file = Dialog.openFileDialog(resourceBundle, stage);
        if (file == null) {
            return;
        }
        if (saveOrCancel()) {
            textArea.setText(fileService.read(file.getAbsolutePath()));
        }
    }

    private void saveFile() {
        File file = Dialog.saveFileDialog(resourceBundle, stage);
        if (file == null) {
            return;
        }
        fileService.save(file.getAbsolutePath(), textArea.getText());
    }

    private void createNewFile() {
        if (saveOrCancel()) {
            textArea.setText("");
        }
    }

    private void openPageInBrowser(String path) {
        try {
            // Получаем URL локального HTML-файла из ресурсов
            URL helpPageUrl = getClass().getResource(path);
            if (helpPageUrl == null) {
                System.err.println("Файл справки не найден!");
                return;
            }
            UUID uuid = UUID.randomUUID();
            // Копируем файл во временную директорию
            File tempFile = File.createTempFile(uuid.toString(), ".html");
            tempFile.deleteOnExit(); // Удалить файл после завершения работы приложения

            try (InputStream in = helpPageUrl.openStream();
                 OutputStream out = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            // Открываем файл в браузере
            Desktop.getDesktop().browse(tempFile.toURI());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Ошибка при открытии HTML-страницы в браузере.");
        }
    }


}