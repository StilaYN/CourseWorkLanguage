package ru.langauge.coursework.view_logic;

public record ErrorModel(
        String path,
        String error,
        int line,
        int column
) {

    public String getPath() {
        return path;
    }

    public String getError() {
        return error;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
