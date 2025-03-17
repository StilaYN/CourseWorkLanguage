package ru.langauge.coursework.core.service;

import ru.langauge.coursework.core.entity.ErrorEntity;
import ru.langauge.coursework.core.entity.FinalStateMachineResult;
import ru.langauge.coursework.core.entity.Token;
import ru.langauge.coursework.core.entity.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class TokenParser {

    private ResourceBundle resourceBundle;

    public void setResourceBundle(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    public List<ErrorEntity> analyzeTokens(List<Token> tokens) {
        List<ErrorEntity> errors = new ArrayList<ErrorEntity>();
        Token currentToken;
        Token previousToken = null;
        int startIndex = 0;
        if (!tokens.isEmpty() && tokens.getFirst().tokenType() == TokenType.FINAL) {
            currentToken = tokens.getFirst();
            startIndex = 1;
        } else if (!tokens.isEmpty()) {
            currentToken = new Token(TokenType.FINAL, 0, 0, 0);
            Token firstToken = tokens.getFirst();
            errors.add(new ErrorEntity(
                    createErrorMessage(currentToken, firstToken), firstToken.lineNumber(), firstToken.startColumn())
            );
        } else {
            return errors;
        }
        for (int i = startIndex; i < tokens.size(); i++) {
            Token nextToken = tokens.get(i);
            FinalStateMachineResult result = isCorrectState(previousToken, currentToken, nextToken);
            switch (result) {
                case TRUE -> {
                    previousToken = currentToken;
                    currentToken = nextToken;
                    break;
                }
                case FALSE -> {
                    errors.add(new ErrorEntity(
                                    createErrorMessage(getNextToken(currentToken, previousToken), nextToken),
                                    nextToken.lineNumber(),
                                    nextToken.startColumn()
                            )
                    );
                    if (nextToken.tokenType() == TokenType.FINAL) {
                        previousToken = currentToken;
                        currentToken = nextToken;
                        continue;
                    }
                    if (nextToken.tokenType() == TokenType.NOT_VALID) {
                        continue;
                    }
                    nextToken = getNextToken(currentToken, previousToken);
                    previousToken = currentToken;
                    currentToken = nextToken;
                    i--;
                    break;
                }
                case SKIP -> {
                    continue;
                }
            }
        }
        return errors;
    }

    private FinalStateMachineResult isCorrectState(Token previousToken, Token currentToken, Token nextToken) {
        if (currentToken.tokenType() == TokenType.FINAL) { //START -> SPACE AFTER FINAL
            if (nextToken.tokenType() == TokenType.WHITESPACE) {
                return FinalStateMachineResult.TRUE;
            } else {
                return FinalStateMachineResult.FALSE;
            }
        } else if (currentToken.tokenType() == TokenType.WHITESPACE && previousToken != null && previousToken.tokenType() == TokenType.FINAL) {
            if (nextToken.tokenType() == TokenType.INT) { //SPACE AFTER FINAL -> INT
                return FinalStateMachineResult.TRUE;
            } else {
                return FinalStateMachineResult.FALSE;
            }
        } else if (currentToken.tokenType() == TokenType.INT) { //INT -> SPACE AFTER TYPE
            if (nextToken.tokenType() == TokenType.WHITESPACE) {
                return FinalStateMachineResult.TRUE;
            } else {
                return FinalStateMachineResult.FALSE;
            }
        } else if (currentToken.tokenType() == TokenType.WHITESPACE && previousToken != null && previousToken.tokenType() == TokenType.INT) {
            if (nextToken.tokenType() == TokenType.VAR_NAME) { //SPACE AFTER TYPE -> VARREM
                return FinalStateMachineResult.TRUE;
            } else {
                return FinalStateMachineResult.FALSE;
            }
        } else if (currentToken.tokenType() == TokenType.VAR_NAME) { // VARREM -> NUMBER
            if (nextToken.tokenType() == TokenType.EQUALS) {
                return FinalStateMachineResult.TRUE;
            } else if (nextToken.tokenType() == TokenType.WHITESPACE) {
                return FinalStateMachineResult.SKIP;
            } else {
                return FinalStateMachineResult.FALSE;
            }
        } else if (currentToken.tokenType() == TokenType.EQUALS) { //NUMBER -> INTREM
            if (nextToken.tokenType() == TokenType.OPERATORS) {
                return FinalStateMachineResult.TRUE;
            } else if (nextToken.tokenType() == TokenType.DIGIT) {
                return FinalStateMachineResult.TRUE;
            } else if (nextToken.tokenType() == TokenType.WHITESPACE) {
                return FinalStateMachineResult.SKIP;
            } else {
                return FinalStateMachineResult.FALSE;
            }
        } else if (currentToken.tokenType() == TokenType.OPERATORS) { //NUMBER -> INTREM
            if (nextToken.tokenType() == TokenType.DIGIT) {
                return FinalStateMachineResult.TRUE;
            } else if (nextToken.tokenType() == TokenType.WHITESPACE) {
                return FinalStateMachineResult.SKIP;
            } else {
                return FinalStateMachineResult.FALSE;
            }
        } else if (currentToken.tokenType() == TokenType.DIGIT) { //INTREM -> END
            if (nextToken.tokenType() == TokenType.END) {
                return FinalStateMachineResult.TRUE;
            } else if (nextToken.tokenType() == TokenType.WHITESPACE) {
                return FinalStateMachineResult.SKIP;
            } else {
                return FinalStateMachineResult.FALSE;
            }
        } else if (currentToken.tokenType() == TokenType.END) {//END
            if (nextToken.tokenType() == TokenType.FINAL) {
                return FinalStateMachineResult.TRUE;
            } else if (nextToken.tokenType() == TokenType.WHITESPACE) {
                return FinalStateMachineResult.SKIP;
            } else {
                return FinalStateMachineResult.FALSE;
            }
        } else {
            throw new RuntimeException();
        }
    }

    private Token getNextToken(Token currentToken, Token previousToken) {
        if (currentToken.tokenType() == TokenType.FINAL) {
            return new Token(TokenType.WHITESPACE, 0, 0, 0);
        } else if (currentToken.tokenType() == TokenType.WHITESPACE && previousToken != null && previousToken.tokenType() == TokenType.FINAL) {
            return new Token(TokenType.INT, 0, 0, 0);
        } else if (currentToken.tokenType() == TokenType.INT) {
            return new Token(TokenType.WHITESPACE, 0, 0, 0);
        } else if (currentToken.tokenType() == TokenType.WHITESPACE && previousToken != null && previousToken.tokenType() == TokenType.INT) {
            return new Token(TokenType.VAR_NAME, 0, 0, 0);
        } else if (currentToken.tokenType() == TokenType.VAR_NAME) {
            return new Token(TokenType.EQUALS, 0, 0, 0);
        } else if (currentToken.tokenType() == TokenType.EQUALS) {
            return new Token(TokenType.OPERATORS, 0, 0, 0);
        } else if (currentToken.tokenType() == TokenType.OPERATORS) {
            return new Token(TokenType.DIGIT, 0, 0, 0);
        } else if (currentToken.tokenType() == TokenType.DIGIT) {
            return new Token(TokenType.END, 0, 0, 0);
        } else {
            return new Token(TokenType.FINAL, 0, 0, 0);
        }
    }

    private String createErrorMessage(Token expectedToken, Token actualToken) {
        return resourceBundle.getString("expected")  + ":'"
                + resourceBundle.getString(expectedToken.tokenType().getDescription())
                + "' " + resourceBundle.getString("actual") + ":'"
                + resourceBundle.getString(actualToken.tokenType().getDescription()) + "'";
    }
}
