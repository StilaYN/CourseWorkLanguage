package ru.langauge.coursework.view_logic;

public record ErrorModel(
        String path,
        String error,
        int line,
        int column
) {

}
