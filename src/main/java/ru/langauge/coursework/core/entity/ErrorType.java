package ru.langauge.coursework.core.entity;

public enum ErrorType {
    REPLACE("error.replace"),
    DELETE("error.delete"),
    PUSH("error.push"),;
    private final String description;

    ErrorType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
