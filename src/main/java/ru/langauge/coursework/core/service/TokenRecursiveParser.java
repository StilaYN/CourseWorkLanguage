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
            List<ErrorEntity> errors = new ArrayList<>();
            ParserTuple tuple = lexpSeq(currentPosition, errors);

            return tuple.errors();

        }

        private ParserTuple lexpSeq(int currentPosition, List<ErrorEntity> errors) {
            if (isAtEnd(currentPosition)) {
                return new ParserTuple(currentPosition, errors);
            }
            if (
                    match(currentPosition, TokenType.DIGIT, errors)
                            || match(currentPosition, TokenType.VAR_NAME, errors)
                            || match(currentPosition, TokenType.OPEN_BRACKET, errors)
            ) {
                ParserTuple parserTuple = lexp(currentPosition, errors);
                return lexpSeq(parserTuple.currentPosition(), parserTuple.errors());
            } else {
                if(openBracketCount == 0) {
                    return lexp(currentPosition, errors);
                }
                return new ParserTuple(currentPosition, errors);
            }
        }

        private ParserTuple lexp(int currentPosition, List<ErrorEntity> errors) {
            if (isAtEnd(currentPosition)) {
                return new ParserTuple(currentPosition, errors);
            }
            if (
                    match(currentPosition, TokenType.DIGIT, errors)
                            || match(currentPosition, TokenType.VAR_NAME, errors)
            ) {
                return atom(currentPosition, errors);
            } else {
                return list(currentPosition, errors);
            }

        }

        private ParserTuple atom(int currentPosition, List<ErrorEntity> errors) {
            if (isAtEnd(currentPosition)) {
                return new ParserTuple(currentPosition, errors);
            }
            return new ParserTuple(currentPosition + 1, errors);
        }

        private ParserTuple list(int currentPosition, List<ErrorEntity> errors) {
            if (isAtEnd(currentPosition)) {
                return new ParserTuple(currentPosition, errors);
            }
            if(match(currentPosition, TokenType.OPEN_BRACKET, errors)) {
                openBracketCount++;
                ParserTuple tuple = lexpSeq(currentPosition + 1, errors);
                openBracketCount--;
                return closeBracket(tuple.currentPosition(), tuple.errors());
            } else {
                addError(currentPosition, TokenType.OPEN_BRACKET, ErrorType.DELETE, errors);
                return new ParserTuple(currentPosition + 1, errors);
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

        private List<ErrorEntity> end(int currentPosition, List<ErrorEntity> errors) {
            if (!isAtEnd(currentPosition)) {
                addError(currentPosition, TokenType.WHITESPACE, ErrorType.DELETE_END, errors);
                return errors;
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
