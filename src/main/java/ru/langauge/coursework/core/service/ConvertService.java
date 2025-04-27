package ru.langauge.coursework.core.service;

import ru.langauge.coursework.core.entity.ErrorEntity;
import ru.langauge.coursework.core.entity.Operand;
import ru.langauge.coursework.core.entity.Token;
import ru.langauge.coursework.core.entity.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConvertService {

    private final Map<TokenType, Integer> priority = Map.of(
            TokenType.OPEN_BRACKET, 0,
            TokenType.CLOSE_BRACKET, 1,
            TokenType.PLUS_OPERATORS, 7,
            TokenType.MULTIPLY_OPERATORS, 8
    );

    public List<ErrorEntity> getResult(List<Token> tokens, String input) {
        List<ErrorEntity> errors = new ArrayList<>();
        StringBuilder description = new StringBuilder();
        List<Token> convertedTokens = convert(tokens);
        for (Token token : convertedTokens) {
            description.append(input, token.startColumn(), token.endColumn());
            description.append(" ");
        }
        errors.add(new ErrorEntity(description.toString(), 1, 0));
        errors.add(new ErrorEntity(calculate(convertedTokens, input), 1, 0));
        return errors;
    }

    private String calculate(List<Token> tokens, String input) {

        List<Operand> operands = new ArrayList<>(tokens.stream().map(
                (token) -> {
                    return new Operand(input.substring(token.startColumn(), token.endColumn()), token.tokenType());
                }
        ).toList());


        for(int i = 0; i<operands.size(); i++) {
            if(operands.get(i).tokenType() == TokenType.PLUS_OPERATORS
                    || operands.get(i).tokenType() == TokenType.MULTIPLY_OPERATORS) {
                Operand operand1 = operands.get(i - 1);
                Operand operand2 = operands.get(i - 2);
                Operand operation = operands.get(i);
                operands.remove(i);
                operands.remove(i - 1);
                operands.remove(i - 2);
                if(operation.value().equals("+")) {
                    double result = Double.parseDouble(operand1.value()) + Double.parseDouble(operand2.value());
                    operands.add(i - 2, new Operand(
                            (String.valueOf(result)), TokenType.DIGIT));
                } else if(operation.value().equals("-")) {
                    double result = Double.parseDouble(operand2.value()) - Double.parseDouble(operand1.value());
                    operands.add(i - 2, new Operand(
                            (String.valueOf(result)), TokenType.DIGIT));
                } else if(operation.value().equals("*")) {
                    double result = Double.parseDouble(operand1.value()) * Double.parseDouble(operand2.value());
                    operands.add(i - 2, new Operand(
                            (String.valueOf(result)), TokenType.DIGIT));
                } else {
                    double result = Double.parseDouble(operand2.value()) / Double.parseDouble(operand1.value());
                    operands.add(i - 2, new Operand(
                            (String.valueOf(result)), TokenType.DIGIT));
                }
                i = i-3;
            }
        }
        return operands.getFirst().value();
    }

    private List<Token> convert(List<Token> tokens) {
        List<Token> outputStack = new ArrayList<>();
        List<Token> operationStack = new ArrayList<>();
        for (Token token : tokens) {
            if(token.tokenType() == TokenType.PLUS_OPERATORS || token.tokenType() == TokenType.MULTIPLY_OPERATORS
            || token.tokenType() == TokenType.OPEN_BRACKET || token.tokenType() == TokenType.CLOSE_BRACKET) {
                 for(int i = operationStack.size() - 1; i >= 0; i--) {
                     if(priority.get(token.tokenType()) != 0
                             && priority.get(token.tokenType()) <= priority.get(operationStack.get(i).tokenType())) {
                         outputStack.add(operationStack.get(i));
                         operationStack.remove(i);
                     } else {
                         break;
                     }
                 }
                 if (token.tokenType() == TokenType.CLOSE_BRACKET) {
                     operationStack.removeLast();
                 } else {
                     operationStack.add(token);
                 }
            } else {
                outputStack.add(token);
            }
        }
        for (int i = operationStack.size() -1 ; i >= 0 ; i--) {
            outputStack.add(operationStack.get(i));
        }
        return outputStack;
    }
}
