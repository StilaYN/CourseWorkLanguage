package ru.langauge.coursework.core.entity;

public enum TokenType {
    FINAL("\\bfinal\\b", "token.name.final"),
    INT("\\bint\\b", "token.name.int"),
    VAR_NAME("[a-zA-Z_][a-zA-Z0-9_]*", "token.name.var"),
    END_OF_LINE("[\\n\\r]", "token.name.line"),
    WHITESPACE("[^\\S\\n\\r]{1,}", "token.name.whitespace"),
    EQUALS("=", "token.name.equals"),
    DIGIT("\\d+", "token.name.digit"),
    OPERATORS("[+-]", "token.name.operators"),
    END(";", "token.name.end"),
    NOT_VALID("[^a-zA-Z0-9_\\s=+\\-;\\n]*", "token.name.not_valid"),
    ;
    private final String regex;

    private final String description;

    TokenType(String regex, String description){
        this.regex = regex;
        this.description = description;
    }

    public String getRegex() {
        return regex;
    }

    public String getDescription() {
        return description;
    }
}
