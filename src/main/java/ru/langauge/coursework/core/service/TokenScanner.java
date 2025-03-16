package ru.langauge.coursework.core.service;

import ru.langauge.coursework.core.entity.Token;
import ru.langauge.coursework.core.entity.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TokenScanner {

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

}
