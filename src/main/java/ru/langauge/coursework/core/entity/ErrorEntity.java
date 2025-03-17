package ru.langauge.coursework.core.entity;

public record ErrorEntity(
        String error,
        int line,
        int column
) {

}
