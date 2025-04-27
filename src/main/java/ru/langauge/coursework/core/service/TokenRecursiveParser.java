package ru.langauge.coursework.core.service;

import ru.langauge.coursework.core.entity.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class TokenRecursiveParser {

    private ResourceBundle resourceBundle;
    private final List<Token> tokens;
    private final List<ErrorEntity> errorsEntity;
    private int openBracketCount = 0;

    public TokenRecursiveParser(List<Token> tokens, List<ErrorEntity> errors, ResourceBundle resourceBundle) {
        this.tokens = tokens;
        this.errorsEntity = errors;
        this.resourceBundle = resourceBundle;
    }

    public List<ErrorEntity> parse() {
        List<ErrorEntity> errors = new ArrayList<>();
        for (List<Token> line : splitTokensIntoLines(tokens)) {
            OneLineTokenRecursiveParser onelineTokenRecursiveParser = new OneLineTokenRecursiveParser(line, resourceBundle);
            errors.addAll(onelineTokenRecursiveParser.parse());
        }
        errors.addAll(errorsEntity);
        return errors.stream().sorted(Comparator.comparing(ErrorEntity::column)).toList();
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
            tokens.add(new Token(TokenType.WHITESPACE,0,0,0));
            List<ErrorEntity> errors = new ArrayList<>();
            ParserTuple tuple = expression(currentPosition, errors);
            return end(tuple.currentPosition(), tuple.errors());
        }

        private ParserTuple expression(int currentPosition, List<ErrorEntity> errors) {
            if (isAtEnd(currentPosition)) {
                return new ParserTuple(currentPosition, errors);
            }
            ParserTuple tuple = t(currentPosition, errors);
            return a(tuple.currentPosition(), tuple.errors());
        }

        private ParserTuple t(int currentPosition, List<ErrorEntity> errors) {
            if (isAtEnd(currentPosition)) {
                return new ParserTuple(currentPosition, errors);
            }
            ParserTuple tuple = o(currentPosition, errors);
            return b(tuple.currentPosition(), tuple.errors());
        }

        private ParserTuple o(int currentPosition, List<ErrorEntity> errors) {
            if (isAtEnd(currentPosition)) {
                return new ParserTuple(currentPosition, errors);
            }
            if (match(currentPosition, TokenType.DIGIT, errors)) {
                return new ParserTuple(currentPosition + 1, errors);
            } else if (match(currentPosition, TokenType.OPEN_BRACKET, errors)) {
                openBracketCount++;
                ParserTuple tuple = expression(currentPosition + 1, errors);
                openBracketCount--;
                return closeBracket(tuple.currentPosition(), tuple.errors());
            } else {
                if (openBracketCount == 0 && match(currentPosition, TokenType.CLOSE_BRACKET, errors))
                {
                    addError(currentPosition, TokenType.DIGIT, ErrorType.REPLACE, errors);
                    return new ParserTuple(currentPosition + 1, errors);
                } else
                {
                    addError(currentPosition, TokenType.DIGIT, ErrorType.PUSH, errors);
                }
            }
            return new ParserTuple(currentPosition, errors);
        }

        private ParserTuple b(int currentPosition, List<ErrorEntity> errors) {
            if (isAtEnd(currentPosition)) {
                return new ParserTuple(currentPosition, errors);
            }
            if (match(currentPosition, TokenType.OPEN_BRACKET, errors) || match(currentPosition, TokenType.DIGIT, errors)) {
                addError(currentPosition, TokenType.MULTIPLY_OPERATORS, ErrorType.PUSH, errors);
                ParserTuple tuple = t(currentPosition, errors);
                return b(tuple.currentPosition(), tuple.errors());
            } else if (match(currentPosition, TokenType.MULTIPLY_OPERATORS, errors)) {
                ParserTuple tuple = t(currentPosition + 1, errors);
                return b(tuple.currentPosition(), tuple.errors());
            } else if (openBracketCount == 0 && match(currentPosition, TokenType.CLOSE_BRACKET, errors)
                    && (match(currentPosition + 1, TokenType.MULTIPLY_OPERATORS, errors)
                    || match(currentPosition + 1, TokenType.CLOSE_BRACKET, errors))) {
                addError(currentPosition, TokenType.MULTIPLY_OPERATORS, ErrorType.DELETE, errors);
                return b(currentPosition + 1, errors);
            } else {
                return new ParserTuple(currentPosition, errors);
            }
        }

        private ParserTuple closeBracket(int currentPosition, List<ErrorEntity> errors) {
            if (isAtEnd(currentPosition)) {
                addError(currentPosition - 1, TokenType.CLOSE_BRACKET, ErrorType.PUSH, errors);
                return new ParserTuple(currentPosition, errors);
            }
            if (!match(currentPosition, TokenType.CLOSE_BRACKET, errors)) {
                addError(currentPosition, TokenType.CLOSE_BRACKET, ErrorType.PUSH, errors);
                return new ParserTuple(currentPosition, errors);
            }
            return new ParserTuple(currentPosition + 1, errors);
        }


        private ParserTuple a(int currentPosition, List<ErrorEntity> errors) {
            if (isAtEnd(currentPosition)) {
                return new ParserTuple(currentPosition, errors);
            }
            if (match(currentPosition, TokenType.OPEN_BRACKET, errors) || match(currentPosition, TokenType.DIGIT, errors)) {
                addError(currentPosition, TokenType.PLUS_OPERATORS, ErrorType.PUSH, errors);
                ParserTuple tuple = t(currentPosition, errors);
                return a(tuple.currentPosition(), tuple.errors());
            } else if (match(currentPosition, TokenType.PLUS_OPERATORS, errors)) {
                ParserTuple tuple = t(currentPosition + 1, errors);
                return a(tuple.currentPosition(), tuple.errors());
            } else if (openBracketCount == 0 && match(currentPosition, TokenType.CLOSE_BRACKET, errors)
                    && (match(currentPosition + 1, TokenType.PLUS_OPERATORS, errors)
                    || match(currentPosition + 1, TokenType.CLOSE_BRACKET, errors))) {
                addError(currentPosition, TokenType.PLUS_OPERATORS, ErrorType.DELETE, errors);
                return a(currentPosition + 1, errors);
            } else {
                return new ParserTuple(currentPosition, errors);
            }
        }

        private List<ErrorEntity> end(int currentPosition, List<ErrorEntity> errors) {
            if (isAtEnd(currentPosition)) {
                //addError(currentPosition - 1, TokenType.END, ErrorType.PUSH, errors);
                errors.add(
                        new ErrorEntity(
                                createErrorMessage(currentPosition, TokenType.WHITESPACE, ErrorType.PUSH),
                                getToken(currentPosition - 1).lineNumber(),
                                getToken(currentPosition - 1).endColumn()
                        )
                );
                return errors;
            }
            if (!match(currentPosition, TokenType.WHITESPACE, errors)) {
                addError(currentPosition, TokenType.WHITESPACE, ErrorType.DELETE, errors);
                end(currentPosition + 1, errors);
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
                case DELETE, DELETE_END -> resourceBundle.getString(errorType.getDescription()) + ":'"
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

        private ParserTuple getMinErrorList(ParserTuple e1, ParserTuple e2, ParserTuple e3) {
            if (e1.errors().size() <= e2.errors().size() && e1.errors().size() <= e3.errors().size()) {
                return e1;
            } else if (e2.errors().size() <= e1.errors().size() && e2.errors().size() <= e3.errors().size()) {
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
        List<Token> currentLine = new ArrayList<>(tokens);
        lines.add(currentLine);
        return lines;
    }
}
