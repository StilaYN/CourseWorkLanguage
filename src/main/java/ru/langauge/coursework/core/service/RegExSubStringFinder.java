package ru.langauge.coursework.core.service;

import ru.langauge.coursework.core.entity.ErrorEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExSubStringFinder {

    private final String PUNCTUATION_MARK = "[!\"(),./:;?\\[\\]_\\`{}]";
    private final String TIN = "(?<!\\d)(?<!\\w)\\d{12}(?!\\d)(?!\\w)";
    private final String CHEMICAL_ELEMENT = "\\b(A[cglmrstu]" +
            "|B[aehikr]?" +
            "|C[adeflmnorsu]?" +
            "|D[bsy]" +
            "|E[rsu]" +
            "|F[elmr]?" +
            "|G[ade]" +
            "|H[eog]?" +
            "|I[nr]?" +
            "|Kr?" +
            "|L[airuv]" +
            "|M[cdgnot]" +
            "|N[adehiop]?" +
            "|O[gs]?" +
            "|P[abdmortu]?" +
            "|R[abefhnsu]" +
            "|S[bcegimn]?" +
            "|T[abcehilms]" +
            "|U" +
            "|V" +
            "|W" +
            "|Xe" +
            "|Yb?" +
            "|Z[nr]" +
            ")\\b";

    public List<ErrorEntity> findPunctuationMark(String input) {
        return findSubstringByRegex(input, PUNCTUATION_MARK);
    }

    public List<ErrorEntity> findTin(String input) {
        return findSubstringByRegex(input, TIN);
    }

    public List<ErrorEntity> findChemicalElement(String input) {
        return findSubstringByRegex(input, CHEMICAL_ELEMENT);
    }

    private List<ErrorEntity> findSubstringByRegex(String input, String regex) {
        List<ErrorEntity> errors = new ArrayList<ErrorEntity>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            errors.add(new ErrorEntity(matcher.group(), 1, matcher.start()));
        }
        return errors;
    }
}
