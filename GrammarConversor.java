import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GrammarConversor {

    public Grammar RemoveLambdaRules(Grammar grammar){

        List<String> nullableVariables = findAnulaveis(grammar.rules);
        List<VariableRules> newVariablesRules = eliminarRegrasLambda(grammar.rules, nullableVariables);

        List<String> allRulesList = getAllRulesList(grammar.rules);

        for (VariableRules variableString : newVariablesRules) {            
            System.out.println(variableString.getRule());
        }

        return grammar;
    }

    public static List<String> findAnulaveis(List<VariableRules> grammar) {
        List<String> anulaveis = new ArrayList<String>();
        boolean additionMade;
        
        do {
            additionMade = false;
            
            for (VariableRules variableRules : grammar) {
                String variavel = variableRules.getVariable();
                List<String> substitutionRules = variableRules.getSubstitutionRules();
                
                for (String rule : substitutionRules) {
                    if (rule.equals("lambda")) {
                        if (!anulaveis.contains(variavel)) {
                            anulaveis.add(variavel);
                            additionMade = true;
                        }
                    }
                }
            }
        } while (additionMade);
        
        return anulaveis;
    } 

    public List<VariableRules> eliminarRegrasLambda(List<VariableRules> grammar, List<String> anulaveis) {
        List<VariableRules> variableRulesList = new ArrayList<>(grammar);

        // 1. Remova as regras lambda originais (do tipo A -> λ).
        List<String> rulesList = new ArrayList<String>();

        for (VariableRules variableRules : variableRulesList) {
            rulesList = variableRules.getSubstitutionRules();
        
            int index = getVariableRuleIndex(variableRules.variable, variableRulesList);
                
            if (index != -1) {
                List<String> filteredRules = rulesList.stream()
                    .filter(rule -> !rule.equals("lambda"))
                    .collect(Collectors.toList());
        
                variableRulesList.get(index).setSubstitutionRules(filteredRules);
            }
        }
        

        // 2. Para cada variável anulável (A), faça as substituições apropriadas nas regras.
        for (String variavelAnulavel : anulaveis) {
            List<VariableRules> regrasComVariavelAnulavel = new ArrayList<>();

            // Encontre as regras que contêm a variável anulável.
            for (VariableRules rule : variableRulesList) {
                if (containsVariable(rule.getSubstitutionRules(), variavelAnulavel)) {
                    regrasComVariavelAnulavel.add(rule);
                }
            }

            // Gere novas regras com a variável anulável substituída.
            for (VariableRules variable : regrasComVariavelAnulavel) {
                for (String originalRule : variable.getSubstitutionRules()) {   

                    List<String> combinacoes = gerarCombinacoes(originalRule, anulaveis);
                                        
                    if (!combinacoes.isEmpty()) {
                        int index = getVariableRuleIndex(variable.variable, variableRulesList);
                        variableRulesList.get(index).setRules(combinacoes);
                    }                    
                }
            }
        }

        return variableRulesList;
    }

    /*
     * 
        S → AB | SCB
        A → aA | C
        B → bB | b
        C → cC | λ

        S -> AB | SCB | SB
     */

    public static List<String> gerarCombinacoes(String regra, List<String> variaveisAnulaveis) {
        List<String> combinacoes = new ArrayList<>();
        List<String> variaveis = new ArrayList<>(variaveisAnulaveis);

        // Caso base: a regra original já é uma combinação
        combinacoes.add(regra);

        // Para cada variável anulável, substitua todas as ocorrências na regra
        for (String variavelAnulavel : variaveis) {
            List<String> novasCombinacoes = new ArrayList<>();

            for (String combinacao : combinacoes) {
                // Substitua todas as ocorrências da variável anulável na combinação atual
                int indice = combinacao.indexOf(variavelAnulavel);
                while (indice != -1) {
                    // Gere uma nova combinação substituindo a variável anulável
                    String novaCombinacao = combinacao.substring(0, indice) + combinacao.substring(indice + variavelAnulavel.length());
                    novasCombinacoes.add(novaCombinacao);

                    // Continue procurando mais ocorrências da variável anulável na combinação
                    indice = combinacao.indexOf(variavelAnulavel, indice + 1);
                }
            }

            combinacoes.addAll(novasCombinacoes);
        }

        return combinacoes;
    }

    private int getVariableRuleIndex(String variable, List<VariableRules> variableRulesList){
        int index = -1;

        for (int i = 0; i < variableRulesList.size(); i++) {
            if (variableRulesList.get(i).getVariable().equals(variable)) {
                index = i; // Set the index to the matching element's index
                break;     // Exit the loop once a match is found
            }
        }

        return index;
    }

    private boolean containsVariable(List<String> rulesList, String variable) {
        for (String rule : rulesList) {
            for (int i = 0; i < rule.length(); i++) {
                if (rule.charAt(i) == variable.charAt(0)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<String> getAllRulesList(List<VariableRules> variableRulesList){
    List<String> allRulesList = new ArrayList<String>();

        for (VariableRules variableRules : variableRulesList) {
            for (String rule : variableRules.substitutionRules) {
                allRulesList.add(variableRules.variable+"->"+rule);
            }
        }

        return allRulesList;
    }
}
