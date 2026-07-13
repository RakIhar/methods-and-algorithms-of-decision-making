package org.example;

import java.util.*;

public class Grammar {
    public record Production(Symbol left, List<Symbol> right) {
        Symbol rightTerminal() {
            return right.getFirst();
        }

        Symbol rightSecond() {
            return right.size() < 2 ? null : right.get(1);
        }
    }

    public record Symbol(SymbolType type, String value) { }

    public enum SymbolType {
        Terminal,
        Nonterminal,
        Start
    }

    private final List<Production> productions = new ArrayList<>();
    private final Symbol startSymbol = new Symbol(SymbolType.Start, "S");

    public Grammar() { }

    public Symbol startSymbol() {
        return startSymbol;
    }

    public void addProduction(Production production) {
        if (!productions.contains(production))
            productions.add(production);
    }

    public List<Production> productions() {
        return productions;
    }

    public List<Production> productions(Symbol left) {
         return productions.stream()
                 .filter(pr -> pr.left.equals(left))
                 .toList();
    }

    public List<Production> productions(Symbol left, Symbol rightTerminal) {
         return productions().stream()
                 .filter(pr -> pr.left.equals(left) && pr.rightTerminal().equals(rightTerminal))
                 .toList();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Production pr : productions) {
            sb.append(pr);
            sb.append("\n");
        }
        return sb.toString();
    }
}