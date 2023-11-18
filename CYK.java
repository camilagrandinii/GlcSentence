import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

    public static boolean Cyk2Nf(Grammar grammar, String sentence) {
        List<VariableRules> rules = grammar.rules;
        int n = sentence.length();

        // Inicialização da tabela CYK
        Set<String>[][] T = new HashSet[n][n];

        // Inicializar a tabela com conjuntos vazios
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                T[i][j] = new HashSet<>();
            }
        }

        // Construir o fecho transitivo das produções unitárias
        Map<String, Set<String>> unitClosure = buildUnitClosure(rules);

        // Preenchimento da tabela com as produções terminais usando o fecho unitário
        for (int i = 0; i < n; i++) {
            T[i][i].addAll(unitClosure.getOrDefault(String.valueOf(sentence.charAt(i)), new HashSet<>()));
        }

        // Preenchimento da tabela com produções binárias
        for (int j = 1; j < n; j++) {
            for (int i = j - 1; i >= 0; i--) {
                for (int h = i; h < j; h++) {
                    for (VariableRules rule : rules) {
                        for (String production : rule.getSubstitutionRules()) {
                            if (production.length() == 2) {
                                char y = production.charAt(0);
                                char z = production.charAt(1);
                                if (T[i][h].contains(String.valueOf(y)) && T[h + 1][j].contains(String.valueOf(z))) {
                                    T[i][j].add(rule.getVariable());
                                }
                            }
                        }
                    }
                    // Aplicar o fecho transitivo às produções unitárias
                    Set<String> closure = new HashSet<>();
                    for (String nonTerminal : T[i][j]) {
                        closure.addAll(unitClosure.getOrDefault(nonTerminal, new HashSet<>()));
                    }
                    T[i][j].addAll(closure);
                }
            }
        }

        // Verificar se a sentença pertence à linguagem
        return T[0][n - 1].contains(grammar.startVariable);
    }

    private static Map<String, Set<String>> buildUnitClosure(List<VariableRules> rules) {
        Map<String, Set<String>> closure = new HashMap<>();

        // Inicializar o fecho com produções diretas
        for (VariableRules rule : rules) {
            for (String production : rule.getSubstitutionRules()) {
                if (production.length() == 1 && Character.isUpperCase(production.charAt(0))) {
                    closure.computeIfAbsent(rule.getVariable(), k -> new HashSet<>()).add(production);
                }
            }
        }

        // Calcular o fecho transitivo
        boolean updated;
        do {
            updated = false;
            for (VariableRules rule : rules) {
                Set<String> ruleClosure = closure.get(rule.getVariable());
                if (ruleClosure != null) {
                    for (String unit : new HashSet<>(ruleClosure)) {
                        if (closure.containsKey(unit) && closure.get(unit).size() > 0) {
                            if (ruleClosure.addAll(closure.get(unit))) {
                                updated = true;
                            }
                        }
                    }
                }
            }
        } while (updated);

        // Incluir os próprios não-terminais no fecho
        for (String nonTerminal : closure.keySet()) {
            closure.get(nonTerminal).add(nonTerminal);
        }

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
