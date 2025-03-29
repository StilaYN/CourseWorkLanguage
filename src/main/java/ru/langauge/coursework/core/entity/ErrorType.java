package ru.langauge.coursework.core.entity;

public enum ErrorType {
    REPLACE("error.replace"),
    DELETE("error.delete"),
    PUSH("error.push"),
    DELETE_END("error.delete_end"),
    ;
    private final String description;

    ErrorType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
