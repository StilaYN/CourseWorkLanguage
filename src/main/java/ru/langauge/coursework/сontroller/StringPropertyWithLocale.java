package ru.langauge.coursework.сontroller;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public enum StringPropertyWithLocale {
    FILE(new SimpleStringProperty(), "menu.file"),
    CREATE(new SimpleStringProperty(),"menu.file.create"),
    OPEN(new SimpleStringProperty(),"menu.file.open"),
    SAVE(new SimpleStringProperty(),"menu.file.save"),
    SAVE_AS(new SimpleStringProperty(),"menu.file.save.as"),
    EXIT(new SimpleStringProperty(),"menu.file.exit"),

    EDIT(new SimpleStringProperty(),"menu.edit"),
    UNDO(new SimpleStringProperty(),"menu.edit.undo"),
    REPEAT(new SimpleStringProperty(),"menu.edit.repeat"),
    CUT(new SimpleStringProperty(),"menu.edit.cut"),
    COPY(new SimpleStringProperty(),"menu.edit.copy"),
    PASTE(new SimpleStringProperty(),"menu.edit.paste"),
    DELETE(new SimpleStringProperty(),"menu.edit.delete"),
    SELECT(new SimpleStringProperty(),"menu.edit.select"),

    TEXT(new SimpleStringProperty(),"menu.text"),
    TASK(new SimpleStringProperty(),"menu.text.task"),
    GRAMMAR(new SimpleStringProperty(),"menu.text.grammar"),
    GRAMMAR_CLASS(new SimpleStringProperty(),"menu.text.grammar.class"),
    METHOD(new SimpleStringProperty(),"menu.text.method"),
    DIAGNOSIS(new SimpleStringProperty(),"menu.text.diagnosis"),
    TEST(new SimpleStringProperty(),"menu.text.test"),
    LITERATURE(new SimpleStringProperty(),"menu.text.literature"),
    SOURCE_CODE(new SimpleStringProperty(),"menu.text.source.code"),

    RUN(new SimpleStringProperty(),"menu.run"),

    LANGUAGE(new SimpleStringProperty(),"menu.language"),
    ENGLISH(new SimpleStringProperty(),"menu.language.english"),
    RUSSIAN(new SimpleStringProperty(),"menu.language.russian"),

    HELP(new SimpleStringProperty(),"menu.help"),
    HELP_HELP(new SimpleStringProperty(),"menu.help.help"),
    ABOUT(new SimpleStringProperty(),"menu.help.about"),

    PATH(new SimpleStringProperty(), "table.head.path"),
    LINE(new SimpleStringProperty(), "table.head.line"),
    COLUMN(new SimpleStringProperty(), "table.head.column"),
    MESSAGE(new SimpleStringProperty(), "table.head.message"),

    TOKEN_TYPE(new SimpleStringProperty(), "table.head.type"),
    VALUE(new SimpleStringProperty(), "table.head.value"),
    START_COLUMN(new SimpleStringProperty(), "table.head.startColumn"),
    END_COLUMN(new SimpleStringProperty(), "table.head.endColumn"),

    TAB_TOKEN(new SimpleStringProperty(), "tab.token"),
    TAB_ERROR(new SimpleStringProperty(), "tab.error"),
    ;
    private final StringProperty property;

    private final String key;

    StringPropertyWithLocale(StringProperty property, String key) {
        this.property = property;
        this.key = key;
    }

    public StringProperty getProperty() {
        return property;
    }

    public String getKey() {
        return key;
    }
}
