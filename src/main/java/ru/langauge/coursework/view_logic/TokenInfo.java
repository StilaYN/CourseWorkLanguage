package ru.langauge.coursework.view_logic;

public record TokenInfo(
        String tokenName,
        String value,
        int lineNumber,
        int startColumn,
        int endColumn
) {

    public String getTokenName() {
        return tokenName;
    }

    public String getValue() {
        return value;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getStartColumn() {
        return startColumn;
    }

    public int getEndColumn() {
        return endColumn;
    }
}
