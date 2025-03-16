package ru.langauge.coursework.core.entity;

public record Token(
        TokenType tokenType,
        int lineNumber,
        int startColumn,
        int endColumn
) {

}
