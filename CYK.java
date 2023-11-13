import java.util.List;

public class CYK {
    public boolean checkSentenceBelongsLanguage(Grammar grammar, String sentence) {
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

    private int getVariableIndex(List<VariableRules> fncRules, String variable) {
        for (int i = 0; i < fncRules.size(); i++) {
            if (fncRules.get(i).getVariable().equals(variable)) {
                return i;
            }
        }
        return -1; // Retorna -1 se a variável não for encontrada
    }    
}
