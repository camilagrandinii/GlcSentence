import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public boolean Cyk2Nf(Grammar grammar, Map<String, Set<String>> inverseUnitGraph, String w) {
        int n = w.length();
        Set<String>[][] T = new Set[n + 1][n + 1];
    
        // Inicialização da tabela T com conjuntos vazios
        for (int i = 0; i <= n; i++) {
            for (int j = 0; j <= n; j++) {
                T[i][j] = new HashSet<>();
            }
        }
    
        // Primeira fase do algoritmo CYK: preenchimento da diagonal principal
        for (int i = 1; i <= n; i++) {
            // O fechamento reflexivo-transitivo para um único símbolo é o próprio símbolo
            // mais qualquer não-terminal que pode levar a ele diretamente
            T[i][i].add(String.valueOf(w.charAt(i - 1)));
            T[i][i] = getReflexiveTransitiveClosure(inverseUnitGraph, T[i][i]);
        }
    
        // Segunda fase do algoritmo CYK: preenchimento do restante da tabela
        for (int j = 2; j <= n; j++) {
            for (int i = j - 1; i >= 1; i--) {
                for (int h = i; h <= j - 1; h++) {
                    for (VariableRules vr : grammar.rules) {
                        String variable = vr.getVariable();
                        for (String rule : vr.getSubstitutionRules()) {
                            String[] parts = rule.split(" ");
                            if (parts.length == 2) {
                                String B = parts[0];
                                String C = parts[1];
                                if (T[i][h].contains(B) && T[h + 1][j].contains(C)) {
                                    T[i][j].add(variable);
                                }
                            }
                        }
                    }
                }
                // Após cada atualização de T[i][j], aplicamos o fechamento reflexivo-transitivo
                T[i][j] = getReflexiveTransitiveClosure(inverseUnitGraph, T[i][j]);
            }
        }
    
        // Verificação final para ver se a palavra w é gerada pela gramática
        return T[1][n].contains(grammar.startVariable);
    }
    
    private Set<String> getReflexiveTransitiveClosure(Map<String, Set<String>> inverseUnitGraph, Set<String> symbols) {
        Set<String> closure = new HashSet<>(symbols);
        boolean changed;
        do {
            changed = false;
            Set<String> newSymbols = new HashSet<>();
            for (String symbol : closure) {
                if (inverseUnitGraph.containsKey(symbol)) {
                    Set<String> edges = inverseUnitGraph.get(symbol);
                    for (String edge : edges) {
                        if (!closure.contains(edge)) {
                            newSymbols.add(edge);
                            changed = true;
                        }
                    }
                }
            }
            closure.addAll(newSymbols);
        } while (changed);
        return closure;
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
