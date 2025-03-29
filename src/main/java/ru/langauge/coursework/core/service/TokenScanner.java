package ru.langauge.coursework.core.service;

import ru.langauge.coursework.core.entity.*;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TokenScanner {

    private ResourceBundle resourceBundle;

    public void setResourceBundle(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    public List<Token> getTokens(String text) {
        List<Token> tokens = new ArrayList<Token>();
        int position = 0;
        int lineNumber = 1;
        while (position < text.length()) {
            for (TokenType tokenType : TokenType.values()) {
                Pattern pattern = Pattern.compile("^" + tokenType.getRegex());
                Matcher matcher = pattern.matcher(text);
                matcher.region(position, text.length());

                if (matcher.find()) {
                    String value = matcher.group();

                    if (tokenType == TokenType.END_OF_LINE) {
                        lineNumber++;
                    } else {
                        tokens.add(
                                new Token(
                                        tokenType,
                                        lineNumber,
                                        position,
                                        position + value.length()
                                )
                        );
                    }
                    position += value.length();
                    break;
                }
            }
        }
        return tokens;
    }

    public TokenScannerResult getTokenScannerResult(String text) {
        List<Token> tokens = getTokens(text);
        List<ErrorEntity> tokenErrors = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            Token currentToken = tokens.get(i);
            if (currentToken.tokenType() == TokenType.NOT_VALID) {
                tokenErrors.add(
                        new ErrorEntity(
                                createNotValidSymbolErrorMessage(currentToken, text),
                                currentToken.lineNumber(),
                                currentToken.startColumn()
                        )
                );
                tokens.remove(i);
                if (i > 0 && i < tokens.size()) {
                    Token nextToken = tokens.get(i);
                    Token previousToken = tokens.get(i - 1);
                    if (previousToken.tokenType() != TokenType.WHITESPACE && nextToken.tokenType() != TokenType.WHITESPACE) {
                        tokens.remove(i);
                        tokens.remove(i - 1);
                        tokens.addAll(i - 1, analyzeToken(text, previousToken, nextToken));
                        i--;
                    }
                }

            }
        }
        return new TokenScannerResult(tokens, tokenErrors);
    }

    private List<Token> analyzeToken(String text, Token previousToken, Token nextToken) {
        String analyzeString = text.substring(previousToken.startColumn(), previousToken.endColumn()) +
                text.substring(nextToken.startColumn(), nextToken.endColumn());
        List<Token> analyzeTokens = getTokens(analyzeString);
        if (analyzeTokens.size() > 2) {
            String newAnylizeString = "";
            for (Token token : analyzeTokens) {
                if (token.tokenType() != TokenType.NOT_VALID) {
                    newAnylizeString += analyzeString.substring(token.startColumn(), token.endColumn());
                }
            }
            analyzeTokens = getTokens(newAnylizeString);
        }
        if (analyzeTokens.size() == 2) {
            return List.of(previousToken, nextToken);
        } else {
            return List.of(new Token(
                            analyzeTokens.getFirst().tokenType(),
                            previousToken.lineNumber(),
                            previousToken.startColumn(),
                            nextToken.endColumn()
                    )
            );
        }

    }

    private String createNotValidSymbolErrorMessage(Token currentToken, String text) {
        return resourceBundle.getString(ErrorType.DELETE.getDescription()) + ":'"
                + resourceBundle.getString(TokenType.NOT_VALID.getDescription()) + "' "
                + text.substring(currentToken.startColumn(), currentToken.endColumn());
    }


}
