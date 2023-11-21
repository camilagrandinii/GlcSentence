import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.util.Pair;

public class CYK {
    public boolean CykCnf(Grammar grammar, String sentence) {
        List<VariableRules> fncRules = grammar.rules;
        int n = sentence.length();

        // Inicialização da tabela CYK
        boolean[][][] table = new boolean[n][n][fncRules.size()];

        // Preenchimento da tabela com as produções unitárias
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < fncRules.size(); j++) {
                VariableRules variableRules = fncRules.get(j);
                if (variableRules.getSubstitutionRules().contains(String.valueOf(sentence.charAt(i)))) {
                    table[i][i][j] = true;
                }
            }
        }

        // Preenchimento da tabela com as produções de dois símbolos ou mais
        for (int len = 2; len <= n; len++) {
            for (int i = 0; i <= n - len; i++) {
                int j = i + len - 1;
                for (int k = i; k < j; k++) {
                    for (VariableRules variableRules : fncRules) {
                        for (String rule : variableRules.getSubstitutionRules()) {
                            if (rule.length() == 2) {
                                char first = rule.charAt(0);
                                char second = rule.charAt(1);
                                int firstIndex = getVariableIndex(fncRules, String.valueOf(first));
                                int secondIndex = getVariableIndex(fncRules, String.valueOf(second));

                                if (table[i][k][firstIndex] && table[k + 1][j][secondIndex]) {
                                    table[i][j][getVariableIndex(fncRules, variableRules.getVariable())] = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        // Verificar se a sentença pertence à linguagem
        int startVariableIndex = getVariableIndex(fncRules, String.valueOf(grammar.startVariable));
        return table[0][n - 1][startVariableIndex];
    }

    public boolean Cyk2Nf(Grammar grammar, Set<String> allSymbols, Map<String, Set<String>> reflexiveTransitiveClosure,
            String w) {
        int n = w.length();

        // Verificação inicial para caracteres válidos
        boolean hasOnlyValidCharacters = w.chars()
                .mapToObj(c -> String.valueOf((char) c))
                .allMatch(allSymbols::contains);

        if (!hasOnlyValidCharacters) {
            return false;
        }

        // Inicialização da tabela CYK
        ArrayList<ArrayList<Set<String>>> table = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            table.add(new ArrayList<>());
            for (int j = 0; j < n; j++) {
                table.get(i).add(new HashSet<>());
            }
        }

        // Preenchimento da diagonal principal
        for (int i = 0; i < n; i++) {
            String symbol = String.valueOf(w.charAt(i));
            table.get(i).get(i).addAll(reflexiveTransitiveClosure.getOrDefault(symbol, new HashSet<>()));
        }

        // Mapeamento otimizado para produções binárias
        Map<Pair<String, String>, Set<String>> binaryProductionsMap = new HashMap<>();
        for (VariableRules vr : grammar.rules) {
            for (String production : vr.substitutionRules) {
                String[] parts = production.split("");
                if (parts.length == 2) {
                    Pair<String, String> pair = new Pair<>(parts[0], parts[1]);
                    binaryProductionsMap.computeIfAbsent(pair, k -> new HashSet<>()).add(vr.variable);
                }
            }
        }

        // Preenchimento do restante da tabela
        for (int j = 1; j < n; j++) {
            for (int i = j - 1; i >= 0; i--) {
                for (int h = i; h < j; h++) {
                    Set<String> BSet = table.get(i).get(h);
                    Set<String> CSet = table.get(h + 1).get(j);
                    for (String B : BSet) {
                        for (String C : CSet) {
                            Set<String> ASet = binaryProductionsMap.get(new Pair<>(B, C));
                            if (ASet != null) {
                                table.get(i).get(j).addAll(ASet);
                            }
                        }
                    }
                    // Aplica o fechamento transitivo reflexivo uma vez
                    Set<String> currentCellSymbols = new HashSet<>(table.get(i).get(j));
                    for (String symbol : currentCellSymbols) {
                        table.get(i).get(j).addAll(reflexiveTransitiveClosure.getOrDefault(symbol, new HashSet<>()));
                    }
                }
            }
        }

        // Verificação final
        return table.get(0).get(n - 1).contains(grammar.startVariable);
    }

    private int getVariableIndex(List<VariableRules> fncRules, String variable) {
        for (int i = 0; i < fncRules.size(); i++) {
            if (fncRules.get(i).getVariable().equals(variable)) {
                return i;
            }
        }
        return -1; // Retorna -1 se a variável não for encontrada
    }

    private List<String> GetSeparatedLowerCaseRules(List<String> rules) {
        List<String> newRules = new ArrayList<>();

        for (String rule : rules) {
            if (IsLowerCaseRule(rule)) {
                newRules.addAll(Arrays.asList(rule.split("")));
            } else {
                newRules.add(rule);
            }
        }

        return newRules;
    }

    private int getLowerCaseVariableIndex(List<VariableRules> fncRules, String variable) {
        for (int i = 0; i < fncRules.size(); i++) {

            fncRules.get(i).substitutionRules = GetSeparatedLowerCaseRules(fncRules.get(i).substitutionRules);

            for (String rule : fncRules.get(i).getSubstitutionRules()) {
                if (rule.equals(variable)) {
                    return i;
                }
            }
        }
        return -1; // Retorna -1 se a variável não for encontrada
    }

    private boolean IsLowerCaseRule(String rule) {
        return rule.chars().allMatch(Character::isLowerCase);
    }
}
