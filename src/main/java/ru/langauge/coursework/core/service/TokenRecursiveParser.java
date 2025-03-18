package ru.langauge.coursework.core.service;

import ru.langauge.coursework.core.entity.ErrorEntity;
import ru.langauge.coursework.core.entity.ErrorType;
import ru.langauge.coursework.core.entity.Token;
import ru.langauge.coursework.core.entity.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class TokenRecursiveParser {

    private ResourceBundle resourceBundle;
    private final List<Token> tokens;

    public TokenRecursiveParser(List<Token> tokens, ResourceBundle resourceBundle) {
        this.tokens = tokens;
        this.resourceBundle = resourceBundle;
    }

    public List<ErrorEntity> parse() {
        List<ErrorEntity> errors = new ArrayList<>();
        for (List<Token> line : splitTokensIntoLines(tokens)) {
            OneLineTokenRecursiveParser onelineTokenRecursiveParser = new OneLineTokenRecursiveParser(line, resourceBundle);
            errors.addAll(onelineTokenRecursiveParser.parse());
        }
        return errors;
    }

    private class OneLineTokenRecursiveParser {

        private final List<Token> tokens;
        private final ResourceBundle resourceBundle;

        public OneLineTokenRecursiveParser(List<Token> tokens, ResourceBundle resourceBundle) {
            this.tokens = tokens;
            this.resourceBundle = resourceBundle;
        }

        public List<ErrorEntity> parse() {
            int currentPosition = 0;
            List<ErrorEntity> errors = new ArrayList<>();
            return start(currentPosition, errors);
        }

        private List<ErrorEntity> start(int currentPosition, List<ErrorEntity> errors) {
            if (isAtEnd(currentPosition)) {
                return errors;
            }
            currentPosition = skipNotValid(currentPosition, TokenType.FINAL, errors);
            if (match(currentPosition, TokenType.WHITESPACE, errors))
                return start(currentPosition + 1, errors);
            if (!match(currentPosition, TokenType.FINAL, errors)) {
                return getMinErrorList(
                        spaceAfterFinal(currentPosition, createErrorList(currentPosition, TokenType.FINAL, ErrorType.PUSH, errors)),
                        spaceAfterFinal(currentPosition + 1, createErrorList(currentPosition, TokenType.FINAL, ErrorType.REPLACE, errors)),
                        start(currentPosition + 1, createErrorList(currentPosition, TokenType.FINAL, ErrorType.DELETE, errors))
                );
            }
            return spaceAfterFinal(currentPosition + 1, errors);
        }

        private List<ErrorEntity> spaceAfterFinal(int currentPosition, List<ErrorEntity> errors) {
            if (isAtEnd(currentPosition)) {
                return errors;
            }
            currentPosition = skipNotValid(currentPosition, TokenType.WHITESPACE, errors);
            if (!match(currentPosition, TokenType.WHITESPACE, errors)) {
                return getMinErrorList(
                        type(currentPosition, createErrorList(currentPosition, TokenType.WHITESPACE, ErrorType.PUSH, errors)),
                        type(currentPosition + 1, createErrorList(currentPosition, TokenType.WHITESPACE, ErrorType.REPLACE, errors)),
                        spaceAfterFinal(currentPosition + 1, createErrorList(currentPosition, TokenType.WHITESPACE, ErrorType.DELETE, errors))
                );
            }
            return type(currentPosition + 1, errors);
        }

        private List<ErrorEntity> type(int currentPosition, List<ErrorEntity> errors) {
            if (isAtEnd(currentPosition)) {
                return errors;
            }
            currentPosition = skipNotValid(currentPosition, TokenType.INT, errors);
            if (match(currentPosition, TokenType.WHITESPACE, errors))
                return type(currentPosition + 1, errors);
            if (!match(currentPosition, TokenType.INT, errors)) {
                return getMinErrorList(
                        spaceAfterType(currentPosition, createErrorList(currentPosition, TokenType.INT, ErrorType.PUSH, errors)),
                        spaceAfterType(currentPosition + 1, createErrorList(currentPosition, TokenType.INT, ErrorType.REPLACE, errors)),
                        type(currentPosition + 1, createErrorList(currentPosition, TokenType.INT, ErrorType.DELETE, errors))
                );
            }
            return spaceAfterType(currentPosition + 1, errors);
        }

        private List<ErrorEntity> spaceAfterType(int currentPosition, List<ErrorEntity> errors) {
            if (isAtEnd(currentPosition)) {
                return errors;
            }
            currentPosition = skipNotValid(currentPosition, TokenType.WHITESPACE, errors);
            if (!match(currentPosition, TokenType.WHITESPACE, errors)) {
                return getMinErrorList(
                        varName(currentPosition, createErrorList(currentPosition, TokenType.WHITESPACE, ErrorType.PUSH, errors)),
                        varName(currentPosition + 1, createErrorList(currentPosition, TokenType.WHITESPACE, ErrorType.REPLACE, errors)),
                        spaceAfterType(currentPosition + 1, createErrorList(currentPosition, TokenType.WHITESPACE, ErrorType.DELETE, errors))
                );
            }
            return varName(currentPosition + 1, errors);
        }

        private List<ErrorEntity> varName(int currentPosition, List<ErrorEntity> errors) {
            if (isAtEnd(currentPosition)) {
                return errors;
            }
            currentPosition = skipNotValid(currentPosition, TokenType.VAR_NAME, errors);
            if (match(currentPosition, TokenType.WHITESPACE, errors))
                return varName(currentPosition + 1, errors);
            if (!match(currentPosition, TokenType.VAR_NAME, errors)) {
                return getMinErrorList(
                        equals(currentPosition, createErrorList(currentPosition, TokenType.VAR_NAME, ErrorType.PUSH, errors)),
                        equals(currentPosition + 1, createErrorList(currentPosition, TokenType.VAR_NAME, ErrorType.REPLACE, errors)),
                        varName(currentPosition + 1, createErrorList(currentPosition, TokenType.VAR_NAME, ErrorType.DELETE, errors))
                );
            }
            return equals(currentPosition + 1, errors);
        }

        private List<ErrorEntity> equals(int currentPosition, List<ErrorEntity> errors) {
            if (isAtEnd(currentPosition)) {
                return errors;
            }
            currentPosition = skipNotValid(currentPosition, TokenType.EQUALS, errors);
            if (match(currentPosition, TokenType.WHITESPACE, errors))
                return equals(currentPosition + 1, errors);
            if (!match(currentPosition, TokenType.EQUALS, errors)) {
                return getMinErrorList(
                        number(currentPosition, createErrorList(currentPosition, TokenType.EQUALS, ErrorType.PUSH, errors)),
                        number(currentPosition + 1, createErrorList(currentPosition, TokenType.EQUALS, ErrorType.REPLACE, errors)),
                        equals(currentPosition + 1, createErrorList(currentPosition, TokenType.EQUALS, ErrorType.DELETE, errors))
                );
            }
            return number(currentPosition + 1, errors);
        }

        private List<ErrorEntity> number(int currentPosition, List<ErrorEntity> errors) {
            if (isAtEnd(currentPosition)) {
                return errors;
            }
            currentPosition = skipNotValid(currentPosition, TokenType.DIGIT, errors);
            if (match(currentPosition, TokenType.WHITESPACE, errors))
                return number(currentPosition + 1, errors);
            if (match(currentPosition, TokenType.OPERATORS, errors)) {
                return digit(currentPosition + 1, errors);
            } else if (match(currentPosition, TokenType.DIGIT, errors)) {
                return end(currentPosition + 1, errors);
            } else {
                return getMinErrorList(
                        digit(currentPosition, createErrorList(currentPosition, TokenType.DIGIT, ErrorType.PUSH, errors)),
                        digit(currentPosition + 1, createErrorList(currentPosition, TokenType.DIGIT, ErrorType.REPLACE, errors)),
                        number(currentPosition + 1, createErrorList(currentPosition, TokenType.DIGIT, ErrorType.DELETE, errors))
                );
            }
        }

        private List<ErrorEntity> digit(int currentPosition, List<ErrorEntity> errors) {
            if (isAtEnd(currentPosition)) {
                return errors;
            }
            currentPosition = skipNotValid(currentPosition, TokenType.DIGIT, errors);
            if (match(currentPosition, TokenType.WHITESPACE, errors))
                return digit(currentPosition + 1, errors);
            if (!match(currentPosition, TokenType.DIGIT, errors)) {
                return getMinErrorList(
                        end(currentPosition, createErrorList(currentPosition, TokenType.DIGIT, ErrorType.PUSH, errors)),
                        end(currentPosition + 1, createErrorList(currentPosition, TokenType.DIGIT, ErrorType.REPLACE, errors)),
                        digit(currentPosition + 1, createErrorList(currentPosition, TokenType.DIGIT, ErrorType.DELETE, errors))
                );
            }
            return end(currentPosition + 1, errors);
        }

        private List<ErrorEntity> end(int currentPosition, List<ErrorEntity> errors) {
            currentPosition = skipNotValid(currentPosition, TokenType.END, errors);
            if (match(currentPosition, TokenType.WHITESPACE, errors))
                return end(currentPosition + 1, errors);
            if (!match(currentPosition, TokenType.END, errors)) {
                addError(currentPosition-1, TokenType.END, ErrorType.REPLACE, errors);
            }
            return errors;
        }

        private boolean match(int currentPosition, TokenType expectedTokentype, List<ErrorEntity> errors) {
            return check(currentPosition, expectedTokentype, errors);
        }

        private boolean check(int currentPosition, TokenType expectedTokenType, List<ErrorEntity> errors) {
            if (isAtEnd(currentPosition)) {
                return false;
            } else {
                return getToken(currentPosition).tokenType() == expectedTokenType;
            }
        }

        private int skipNotValid(int currentPosition, TokenType expectedTokentype, List<ErrorEntity> errors) {
            while (!isAtEnd(currentPosition) && getToken(currentPosition).tokenType() == TokenType.NOT_VALID) {
                addError(currentPosition, expectedTokentype, ErrorType.DELETE, errors);
                currentPosition++;
            }
            return currentPosition;
        }

        private boolean isAtEnd(int currentPosition) {
            return currentPosition >= tokens.size();
        }


        private Token getToken(int currentPosition) {
            return tokens.get(currentPosition);
        }


        private void addError(int currentPosition, TokenType expectedTokentype, ErrorType errorType, List<ErrorEntity> errors) {
            errors.add(
                    new ErrorEntity(
                            createErrorMessage(currentPosition, expectedTokentype, errorType),
                            getToken(currentPosition).lineNumber(),
                            getToken(currentPosition).startColumn()
                    )
            );
        }

        private String createErrorMessage(int currentPosition, TokenType expectedTokentype, ErrorType errorType) {

            return switch (errorType) {
                case DELETE -> resourceBundle.getString(errorType.getDescription()) + ":'"
                        + resourceBundle.getString(getToken(currentPosition).tokenType().getDescription()) + "'";
                case PUSH -> resourceBundle.getString(errorType.getDescription()) + ":'"
                        + resourceBundle.getString(expectedTokentype.getDescription()) + "'";
                case REPLACE -> resourceBundle.getString("expected") + ":'"
                        + resourceBundle.getString(expectedTokentype.getDescription())
                        + "' " + resourceBundle.getString("actual") + ":'"
                        + resourceBundle.getString(getToken(currentPosition).tokenType().getDescription()) + "'";
            };
        }

        private List<ErrorEntity> getMinErrorList(List<ErrorEntity> e1, List<ErrorEntity> e2, List<ErrorEntity> e3) {
            if (e1.size() <= e2.size() && e1.size() <= e3.size()) {
                return e1;
            } else if (e2.size() <= e1.size() && e2.size() <= e3.size()) {
                return e2;
            } else {
                return e3;
            }
        }

        private List<ErrorEntity> createErrorList(
                int currentPosition,
                TokenType expectedTokenType,
                ErrorType errorType,
                List<ErrorEntity> errorEntities
        ) {
            List<ErrorEntity> errors = new ArrayList<>(errorEntities);
            addError(currentPosition, expectedTokenType, errorType, errors);
            return errors;
        }
    }

    private List<List<Token>> splitTokensIntoLines(List<Token> tokens) {
        List<List<Token>> lines = new ArrayList<>();
        List<Token> currentLine = new ArrayList<>();

        for (Token token : tokens) {
            currentLine.add(token);

            if (token.tokenType() == TokenType.END) {
                lines.add(currentLine);
                currentLine = new ArrayList<>();
            }
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine);
        }

        return lines;
    }
}
