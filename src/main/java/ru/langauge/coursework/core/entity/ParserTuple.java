package ru.langauge.coursework.core.entity;

import java.util.List;

public record ParserTuple(
        int currentPosition,
        List<ErrorEntity> errors
) {
}
