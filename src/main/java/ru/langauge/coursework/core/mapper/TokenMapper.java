package ru.langauge.coursework.core.mapper;

import ru.langauge.coursework.core.entity.Token;
import ru.langauge.coursework.view_logic.TokenInfo;

import java.util.List;
import java.util.ResourceBundle;

public class TokenMapper {

    public List<TokenInfo> map(List<Token> tokenList, ResourceBundle resourceBundle) {
        return tokenList.stream()
                .map((token) -> {
                    return new TokenInfo(
                            token.tokenType().name(),
                            resourceBundle.getString(token.tokenType().getDescription()),
                            token.lineNumber(),
                            token.startColumn()+1,
                            token.endColumn()
                    );
                })
                .toList();
    }

}
