package org.example;

import java.util.*;

public class GrammarBuilder {
    private static Grammar grammar;
    private static int nonterminalCounter = 1;

    public static Grammar build(final List<String> strings) {
        grammar = new Grammar();
        nonterminalCounter = 1;

        IO.println("Исходные цепочки: " + strings);
        IO.println("\n=== ЭТАП 1: Построение нерекурсивной грамматики ===\n");
        buildNonRecursiveGrammar(strings);
        IO.println(grammar);

        IO.println("\n=== ЭТАП 2: Преобразование в рекурсивную грамматику ===\n");
        buildRecursiveGrammar();
        IO.println(grammar);

        IO.println("\n=== ЭТАП 3: Упрощение грамматики ===\n");
        simplifyGrammar();
        IO.println(grammar);

        return grammar;
    }

    private static void addNonRecursiveRules(String str) {
        IO.println("Обработка цепочки: " + str);

        String remaining = str;
        Grammar.Symbol leftSymbol = grammar.startSymbol();

        while (!remaining.isEmpty()) {
            Grammar.Symbol rightTerminal = new Grammar.Symbol(Grammar.SymbolType.Terminal, remaining.substring(0, 1));
            remaining = remaining.substring(1);

            List<Grammar.Production> existingProductions = grammar.productions(leftSymbol, rightTerminal);

            if (existingProductions.isEmpty()) {

                Grammar.Symbol rightSecond;
                if (remaining.isEmpty()) {
                    rightSecond = null;
                } else if (remaining.length() == 1) {
                    rightSecond = new Grammar.Symbol(Grammar.SymbolType.Terminal, remaining.substring(0, 1));
                    remaining = "";
                } else {
                    String nonterminalString = "A" + nonterminalCounter++;
                    rightSecond = new Grammar.Symbol(Grammar.SymbolType.Nonterminal, nonterminalString);
                }

                List<Grammar.Symbol> rightSymbols = rightSecond == null
                        ? List.of(rightTerminal)
                        : List.of(rightTerminal, rightSecond);

                Grammar.Production production = new Grammar.Production(leftSymbol, rightSymbols);
                grammar.addProduction(production);

                leftSymbol = rightSecond;

                IO.println("  Добавлено правило: " + production);
            } else {
                leftSymbol = existingProductions.getFirst().rightSecond();
                IO.println("  Пропущено правило: " + existingProductions.getFirst());
            }
        }
    }



    private static void buildNonRecursiveGrammar(final List<String> strings) {
        strings.stream()
                .distinct()
                .filter(s -> s != null && !s.isEmpty())
                .sorted(Comparator.comparing(String::length).reversed())
                .forEach(GrammarBuilder::addNonRecursiveRules);
    }

    private static void buildRecursiveGrammar() {
        boolean isChanged;
        int iteration = 1;

        do {
            isChanged = false;
            System.out.println("Итерация " + iteration++ + ":");

            for (Grammar.Production general : grammar.productions()) {
                for (Grammar.Production special : grammar.productions()) {

                    if (general == special)
                        continue;

                    if (general.right().size() < 2 || special.right().size() < 2)
                        continue;

                    if (!general.right().get(1).type().equals(Grammar.SymbolType.Nonterminal))
                        continue;

                    if (!special.right().get(1).type().equals(Grammar.SymbolType.Terminal))
                        continue;

                    if (!general.rightTerminal().equals(special.rightTerminal()))
                        continue;

                    //Должно быть правило general.rightSecond() -> special.rightSecond()
                    List<Grammar.Production> tailProd = grammar.productions().stream()
                            .filter(pr -> pr.left().equals(general.rightSecond())
                                    && pr.rightTerminal().equals(special.rightSecond())
                                    && pr.rightSecond() == null)
                            .toList();
                    if (tailProd.isEmpty())
                        continue;

                    IO.println("  Обнаружены эквивалентные продукции:");
                    IO.println("    general:  " + general);
                    IO.println("    special:  " + special);
                    IO.println("    tail:    " + tailProd);

                    merge(general, special);
                    isChanged = true;
                    break;
                }
                if (isChanged) break;
            }

        } while (isChanged);
    }

    private static void merge(Grammar.Production general, Grammar.Production special) {
        Grammar.Symbol Ar = special.left();
        Grammar.Symbol An = general.left();

        List<Grammar.Production> newProductions = new ArrayList<>();

        for (Grammar.Production p : grammar.productions()) {
            Grammar.Symbol left = p.left().equals(Ar)
                    ? An
                    : p.left();

            List<Grammar.Symbol> right = new ArrayList<>();
            for (Grammar.Symbol s : p.right()) {
                right.add(s.equals(Ar)
                        ? An
                        : s);
            }

            newProductions.add(new Grammar.Production(left, right));
        }

        grammar.productions().clear();

        for (Grammar.Production p : newProductions) {
            // удаляем правило Ar → ab
            if (p.left().equals(Ar) && p.right().equals(special.right()))
                continue;

            grammar.addProduction(p);
        }
    }

    private static void simplifyGrammar() {
        boolean changed;

        do {
            changed = false;

            List<Grammar.Symbol> nonterminals = grammar.productions().stream()
                    .map(Grammar.Production::left)
                    .distinct()
                    .toList();

            outer:
            for (int i = 0; i < nonterminals.size(); i++) {
                for (int j = i + 1; j < nonterminals.size(); j++) {

                    Grammar.Symbol A = nonterminals.get(i);
                    Grammar.Symbol B = nonterminals.get(j);

                    if (areEquivalent(A, B)) {
                        mergeNonterminals(A, B);
                        changed = true;
                        break outer;
                    }
                }
            }

        } while (changed);
    }

    private static boolean areEquivalent(Grammar.Symbol A, Grammar.Symbol B) {
        List<Grammar.Production> pA = grammar.productions(A);
        List<Grammar.Production> pB = grammar.productions(B);

        if (pA.size() != pB.size())
            return false;

        return pA.stream().allMatch(pa ->
                pB.stream().anyMatch(pb -> equalProductions(pa, pb))
        );
    }

    private static boolean equalProductions(Grammar.Production p1, Grammar.Production p2) {
        if (p1.right().size() != p2.right().size()) return false;

        for (int i = 0; i < p1.right().size(); i++) {
            Grammar.Symbol s1 = p1.right().get(i);
            Grammar.Symbol s2 = p2.right().get(i);

            if (s1.type() != s2.type()) return false;

            // важно: нетерминалы считаем одинаковыми по типу
            if (s1.type() == Grammar.SymbolType.Terminal && !s1.equals(s2))
                return false;
        }

        return true;
    }

    private static void mergeNonterminals(Grammar.Symbol A, Grammar.Symbol B) {
        List<Grammar.Production> newProductions = new ArrayList<>();

        for (Grammar.Production p : grammar.productions()) {

            Grammar.Symbol left = p.left().equals(B)
                    ? A
                    : p.left();

            List<Grammar.Symbol> right = new ArrayList<>();
            for (Grammar.Symbol s : p.right()) {
                if (s.equals(B))
                    right.add(A);
                else
                    right.add(s);
            }

            newProductions.add(new Grammar.Production(left, right));
        }

        grammar.productions().clear();

        for (Grammar.Production p : newProductions) {
            grammar.addProduction(p);
        }
    }
}