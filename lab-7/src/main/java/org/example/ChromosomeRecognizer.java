package org.example;

import java.util.ArrayList;
import java.util.List;

public class ChromosomeRecognizer {
    public record Token(String value, Token left, Token right) {
        boolean isLeaf() {
            return left == null && right == null;
        }
    }

    public enum Chromosome {
        Telocentric,
        Metacentric,
        Unknown
    }

    public static final String SHOULDER = "Плечо";
    public static final String SIDE = "Сторона";
    public static final String BOTTOM = "Основание";
    public static final String LEFT_PART = "Левая часть";
    public static final String RIGHT_PART = "Правая часть";
    public static final String SHOULDER_PAIR = "Пара плеч";

    private Token rootToken;

    public Chromosome recognize(String chain) {
        rootToken = null;

        if (chain == null || !chain.matches("[abcde]*"))
            return Chromosome.Unknown;

        List<Token> tokens = new ArrayList<>();
        for (int i = 0; i < chain.length(); i++) {
            tokens.add(new Token(String.valueOf(chain.charAt(i)), null, null));
        }

        for (int i = 0; i < tokens.size(); i++) {
            if ("a".equals(tokens.get(i).value())) {
                tokens.set(i, new Token(SHOULDER, tokens.get(i), null));
            }
        }

        boolean isChanged = true;
        while (isChanged) {
            isChanged = false;
            int i = 0;
            while (i < tokens.size() - 1) {
                Token left = tokens.get(i);
                Token right = tokens.get(i + 1);

                if ("b".equals(left.value()) && SHOULDER.equals(right.value())) {
                    Token newToken = new Token(SHOULDER, left, right);
                    tokens.set(i, newToken);
                    tokens.remove(i + 1);
                    isChanged = true;
                } else if (SHOULDER.equals(left.value()) && "b".equals(right.value())) {
                    Token newToken = new Token(SHOULDER, left, right);
                    tokens.set(i, newToken);
                    tokens.remove(i + 1);
                    isChanged = true;
                } else {
                    i++;
                }
            }
        }

        for (int i = 0; i < tokens.size(); i++) {
            String val = tokens.get(i).value();
            if ("b".equals(val) || "d".equals(val)) {
                tokens.set(i, new Token(SIDE, tokens.get(i), null));
            }
        }

        isChanged = true;
        while (isChanged) {
            isChanged = false;
            int i = 0;
            while (i < tokens.size() - 1) {
                Token left = tokens.get(i);
                Token right = tokens.get(i + 1);

                if ("b".equals(left.value()) && SIDE.equals(right.value())) {
                    Token newToken = new Token(SIDE, left, right);
                    tokens.set(i, newToken);
                    tokens.remove(i + 1);
                    isChanged = true;
                } else if (SIDE.equals(left.value()) && "b".equals(right.value())) {
                    Token newToken = new Token(SIDE, left, right);
                    tokens.set(i, newToken);
                    tokens.remove(i + 1);
                    isChanged = true;
                } else {
                    i++;
                }
            }
        }

        for (int i = 0; i < tokens.size(); i++) {
            if ("e".equals(tokens.get(i).value())) {
                tokens.set(i, new Token(BOTTOM, tokens.get(i), null));
            }
        }

        isChanged = true;
        while (isChanged) {
            isChanged = false;
            int i = 0;
            while (i < tokens.size() - 1) {
                Token left = tokens.get(i);
                Token right = tokens.get(i + 1);

                if ("b".equals(left.value()) && BOTTOM.equals(right.value())) {
                    Token newToken = new Token(BOTTOM, left, right);
                    tokens.set(i, newToken);
                    tokens.remove(i + 1);
                    isChanged = true;
                } else if (BOTTOM.equals(left.value()) && "b".equals(right.value())) {
                    Token newToken = new Token(BOTTOM, left, right);
                    tokens.set(i, newToken);
                    tokens.remove(i + 1);
                    isChanged = true;
                } else {
                    i++;
                }
            }
        }

        isChanged = true;
        while (isChanged) {
            isChanged = false;
            int i = 0;
            while (i < tokens.size() - 1) {
                Token left = tokens.get(i);
                Token right = tokens.get(i + 1);

                if ("c".equals(left.value()) && SHOULDER.equals(right.value())) {
                    tokens.set(i, new Token(RIGHT_PART, left, right));
                    tokens.remove(i + 1);
                    isChanged = true;
                } else if (SHOULDER.equals(left.value()) && "c".equals(right.value())) {
                    tokens.set(i, new Token(LEFT_PART, left, right));
                    tokens.remove(i + 1);
                    isChanged = true;
                } else {
                    i++;
                }
            }
        }

        isChanged = true;
        while (isChanged) {
            isChanged = false;
            int i = 0;
            while (i < tokens.size() - 1) {
                Token left = tokens.get(i);
                Token right = tokens.get(i + 1);

                if ((LEFT_PART.equals(left.value()) && SHOULDER.equals(right.value())) ||
                        (SHOULDER.equals(left.value()) && RIGHT_PART.equals(right.value())) ||
                        (SHOULDER_PAIR.equals(left.value()) && SIDE.equals(right.value())) ||
                        (SIDE.equals(left.value()) && SHOULDER_PAIR.equals(right.value()))) {

                    Token newToken = new Token(SHOULDER_PAIR, left, right);
                    tokens.set(i, newToken);
                    tokens.remove(i + 1);
                    isChanged = true;
                } else {
                    i++;
                }
            }
        }

        if (tokens.size() == 2) {
            if (SHOULDER_PAIR.equals(tokens.get(0).value()) &&
                    SHOULDER_PAIR.equals(tokens.get(1).value())) {
                rootToken = new Token("S", tokens.get(0), tokens.get(1));
                return Chromosome.Telocentric;
            }
            if (BOTTOM.equals(tokens.get(0).value()) &&
                    SHOULDER_PAIR.equals(tokens.get(1).value())) {
                rootToken = new Token("T", tokens.get(0), tokens.get(1));
                return Chromosome.Metacentric;
            }
        }

        return Chromosome.Unknown;
    }

    public Token getRootToken() {
        return rootToken;
    }
}