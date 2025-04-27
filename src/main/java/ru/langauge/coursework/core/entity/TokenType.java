package ru.langauge.coursework.core.entity;

public enum TokenType {
    END_OF_LINE("[\\n\\r]", "token.name.line"),
    WHITESPACE("[^\\S\\n\\r]{1,}", "token.name.whitespace"),
    DIGIT("\\d+", "token.name.digit"),
    OPEN_BRACKET("\\(", "token.name.open_bracket"),
    CLOSE_BRACKET("\\)", "token.name.close_bracket"),
    PLUS_OPERATORS("[+-]", "token.name.operators"),
    MULTIPLY_OPERATORS("[*/]", "token.name.operators"),
    NOT_VALID("[^0-9\\s+\\-\\n*/\\(\\)]*", "token.name.not_valid"),
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
