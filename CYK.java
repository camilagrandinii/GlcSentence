import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public boolean Cyk2Nf(Grammar grammar, String sentence) {
        List<VariableRules> twoNfRules = grammar.rules;
        int n = sentence.length();

        // Inicialização da tabela CYK
        boolean[][][] table = new boolean[n][n][twoNfRules.size()];

        // Preenchimento da tabela com as produções unitárias e terminais
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < twoNfRules.size(); j++) {
                VariableRules variableRules = twoNfRules.get(j);
                for (String rule : variableRules.getSubstitutionRules()) {
                    if (rule.length() == 1) {
                        // Verifica se é uma produção terminal ou uma produção unitária não-terminal
                        if (rule.equals(String.valueOf(sentence.charAt(i))) ||
                                (Character.isUpperCase(rule.charAt(0)) && getVariableIndex(twoNfRules, rule) != -1)) {
                            table[i][i][j] = true;
                        }
                    }
                }
            }
        }

        // Preenchimento da tabela com produções binárias
        for (int len = 2; len <= n; len++) {
            for (int i = 0; i <= n - len; i++) {
                int j = i + len - 1;
                for (int k = i; k < j; k++) {
                    for (VariableRules variableRules : twoNfRules) {
                        for (String rule : variableRules.getSubstitutionRules()) {
                            if (rule.length() == 2) {
                                int firstIndex = 0;
                                int secondIndex = 0;

                                if (IsLowerCaseRule(rule)) {
                                    firstIndex = getLowerCaseVariableIndex(twoNfRules, String.valueOf(rule.charAt(0)));
                                    secondIndex = getLowerCaseVariableIndex(twoNfRules, String.valueOf(rule.charAt(1)));
                                } else {
                                    firstIndex = getVariableIndex(twoNfRules, String.valueOf(rule.charAt(0)));
                                    secondIndex = getVariableIndex(twoNfRules, String.valueOf(rule.charAt(1)));
                                }

                                if (firstIndex != -1 && secondIndex != -1 && table[i][k][firstIndex]
                                        && table[k + 1][j][secondIndex]) {
                                    table[i][j][getVariableIndex(twoNfRules, variableRules.getVariable())] = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        // Verificar se a sentença pertence à linguagem
        int startVariableIndex = getVariableIndex(twoNfRules, String.valueOf(grammar.startVariable));
        if (startVariableIndex != -1) {
            return table[0][n - 1][startVariableIndex];
        } else {
            return false;
        }
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
