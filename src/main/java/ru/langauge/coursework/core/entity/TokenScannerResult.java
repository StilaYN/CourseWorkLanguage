package ru.langauge.coursework.core.entity;

import java.util.List;

public record TokenScannerResult(
        List<Token> tokens,
        List<ErrorEntity> errors
) {
}
