import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GrammarConversor {

    public Grammar ToFncGrammar(Grammar grammar) {
        List<String> nullableVariables = findAnulaveis(grammar.rules);
        List<VariableRules> newVariablesRules = eliminarRegrasLambda(grammar.rules, nullableVariables);
        newVariablesRules = RemoveUnitRules(newVariablesRules);
        newVariablesRules = transformLongProductions(newVariablesRules);
        newVariablesRules = transformMixedProductions(newVariablesRules);
        for (VariableRules variableString : newVariablesRules) {
            System.out.println(variableString.getRule());
        }

        return grammar;
    }

    private static List<String> findAnulaveis(List<VariableRules> grammar) {
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

    private List<VariableRules> RemoveUnitRules(List<VariableRules> variableRules) {

        for (VariableRules variableRule : variableRules) {
            List<String> rulesList = variableRule.getSubstitutionRules();
            List<String> newRulesList = new ArrayList<>(rulesList);

            for (String rule : rulesList) {
                // confere se é uma regra apenas e ta em maiscula pra ter certeza que é variavel
                if (isUnitRule(rule)) {
                    String unitVariable = rule;

                    // variável unitária implica a própria variável pra poder so apagar
                    if (!unitVariable.equals(variableRule.getVariable())) {
                        VariableRules unitVariableRules = null;
                        for (VariableRules rules : variableRules) {
                            if (rules.getVariable().equals(unitVariable)) {
                                unitVariableRules = rules;
                                break;
                            }
                        }

                        List<String> unitRules = unitVariableRules.getSubstitutionRules();

                        newRulesList.addAll(unitRules);
                    }
                }
            }
            variableRule.setSubstitutionRules(newRulesList);
        }

        for (VariableRules variableRule : variableRules) {
            List<String> rulesList = variableRule.getSubstitutionRules();
            List<String> nonUnitRules = rulesList.stream()
                    .filter(rule -> !isUnitRule(rule))
                    .collect(Collectors.toList());
            variableRule.setSubstitutionRules(nonUnitRules);
        }

        return variableRules;
    }

    private boolean isUnitRule(String rule) {
        return rule.length() == 1 && Character.isUpperCase(rule.charAt(0));
    }

    private List<VariableRules> eliminarRegrasLambda(List<VariableRules> variableRulesList, List<String> anulaveis) {

        variableRulesList = removeLambdaRules(variableRulesList);
        List<VariableRules> variableRulesListCombinationAux = new ArrayList<>(variableRulesList);

        do {
            // 1. Remova as regras lambda originais (do tipo A -> λ).

            List<VariableRules> newRulesToAdd = new ArrayList<>(); // Lista temporária para armazenar novas regras.

            // 2. Para cada variável anulável (A), faça as substituições apropriadas nas
            // regras.
            for (String variavelAnulavel : anulaveis) {
                List<VariableRules> regrasComVariavelAnulavel = new ArrayList<>();

                // Encontre as regras que contêm a variável anulável.
                for (VariableRules rule : variableRulesList) {
                    if (rulesListcontainsVariable(rule.getSubstitutionRules(), variavelAnulavel)) {
                        regrasComVariavelAnulavel.add(rule);
                    }
                }

                // Gere novas regras com a variável anulável substituída.
                for (VariableRules variable : regrasComVariavelAnulavel) {
                    List<String> substitutionRules = variable.getSubstitutionRules();

                    for (String originalRule : substitutionRules) {
                        if (ruleContainsVariable(originalRule, variavelAnulavel)) {
                            List<String> combinacoes = gerarCombinacoes(originalRule, anulaveis);

                            if (!combinacoes.isEmpty()) {
                                VariableRules newRule = new VariableRules(variable.variable, combinacoes);
                                newRulesToAdd.add(newRule);
                            }
                        }
                    }
                }
            }

            for (VariableRules variableRules : newRulesToAdd) {
                int index = getVariableRuleIndex(variableRules.variable, variableRulesList);

                if (variableRules.variable == variableRulesListCombinationAux.get(index).variable) {
                    List<String> rulesToAdd = filterRulesToAdd(
                            variableRulesListCombinationAux.get(index).getSubstitutionRules(),
                            variableRules.getSubstitutionRules());
                    variableRulesListCombinationAux.get(index).setRules(rulesToAdd);
                }
            }

            anulaveis = findAnulaveis(variableRulesListCombinationAux);
            variableRulesList = removeLambdaRules(variableRulesList);

        } while (anulaveis.size() > 0);

        return variableRulesListCombinationAux;
    }

    /*
     * 
     * S → AB | SCB
     * A → aA | C
     * B → bB | b
     * C → cC | λ
     * 
     * S -> AB | SCB | SB
     */

    private static List<String> gerarCombinacoes(String regra, List<String> variaveisAnulaveis) {
        List<String> combinacoes = new ArrayList<>();
        List<String> variaveis = new ArrayList<>(variaveisAnulaveis);
        List<Integer> nullableIndexes = new ArrayList<>();

        // Caso base: a regra original já é uma combinação
        combinacoes.add(regra);

        // Para cada variável anulável, substitua todas as ocorrências na regra
        for (String variavelAnulavel : variaveis) {

            List<String> novasCombinacoes = new ArrayList<>();

            for (String combinacao : combinacoes) {
                // Substitua todas as ocorrências da variável anulável na combinação atual
                int indice = combinacao.indexOf(variavelAnulavel);

                while (indice != -1) {

                    nullableIndexes.add(indice);
                    // Gere uma nova combinação substituindo a variável anulável
                    String novaCombinacao = combinacao.substring(0, indice)
                            + combinacao.substring(indice + variavelAnulavel.length());

                    if (novaCombinacao.equals("")) {
                        novaCombinacao = "lambda";
                    }

                    novasCombinacoes.add(novaCombinacao);

                    // Continue procurando mais ocorrências da variável anulável na combinação
                    indice = combinacao.indexOf(variavelAnulavel, indice + 1);

                    if (indice != -1) {
                        nullableIndexes.add(indice);
                    }
                }
            }

            StringBuilder novaRegra = new StringBuilder();

            if (nullableIndexes.size() > 1) {
                for (int i = 0; i < regra.length(); i++) {
                    if (!nullableIndexes.contains(i)) {
                        novaRegra.append(regra.charAt(i));
                    }
                }

                String novaRegraString = novaRegra.toString();
                novasCombinacoes.add(novaRegraString);
            }

            combinacoes.addAll(novasCombinacoes);
        }

        return combinacoes;
    }

    private List<VariableRules> removeLambdaRules(List<VariableRules> variableRulesListAux) {
        for (VariableRules variableRules : variableRulesListAux) {
            List<String> rulesList = variableRules.getSubstitutionRules();

            int index = getVariableRuleIndex(variableRules.variable, variableRulesListAux);

            if (index != -1) {
                List<String> filteredRules = rulesList.stream()
                        .filter(rule -> !rule.equals("lambda"))
                        .collect(Collectors.toList());

                variableRulesListAux.get(index).setSubstitutionRules(filteredRules);
            }
        }

        return variableRulesListAux;
    }

    private int getVariableRuleIndex(String variable, List<VariableRules> variableRulesList) {
        int index = -1;

        for (int i = 0; i < variableRulesList.size(); i++) {
            if (variableRulesList.get(i).getVariable().equals(variable)) {
                index = i; // Set the index to the matching element's index
                break; // Exit the loop once a match is found
            }
        }

        return index;
    }

    private List<String> filterRulesToAdd(List<String> oldRules, List<String> newRules) {
        List<String> filteredRules = new ArrayList<>(newRules);

        filteredRules.removeAll(oldRules);

        return filteredRules;
    }

    private boolean ruleContainsVariable(String rule, String variable) {

        for (int i = 0; i < rule.length(); i++) {
            if (rule.charAt(i) == variable.charAt(0)) {
                return true;
            }
        }

        return false;
    }

    private boolean rulesListcontainsVariable(List<String> rulesList, String variable) {
        for (String rule : rulesList) {
            for (int i = 0; i < rule.length(); i++) {
                if (rule.charAt(i) == variable.charAt(0)) {
                    return true;
                }
            }
        }
        return false;
    }

    // A -> a B -> a
    private List<String> getAllRulesList(List<VariableRules> variableRulesList) {
        List<String> allRulesList = new ArrayList<String>();

        for (VariableRules variableRules : variableRulesList) {
            for (String rule : variableRules.substitutionRules) {
                allRulesList.add(variableRules.variable + "->" + rule);
            }
        }

        return allRulesList;
    }

    // - fazer conversão de aB para U -> a UB
    public List<VariableRules> transformLongProductions(List<VariableRules> variableRulesList) {
        boolean doesNotHaveBigRules = false;
        List<VariableRules> variableRulesListAux = new ArrayList<VariableRules>(variableRulesList);
        int newVariableCounter = 1;

        do {
            for (VariableRules variableRules : variableRulesList) {
                List<String> rulesList = variableRules.getSubstitutionRules();
                List<String> newRulesList = new ArrayList<>(rulesList);

                for (String rule : rulesList) {
                    if (removeNumbers(rule).length() >= 3) {
                        // Realize a transformação para regras com comprimento maior ou igual a três
                        newRulesList.remove(rule); // Remova a regra original

                        // Divida a regra em duas partes
                        String var1 = rule.substring(0, 1);
                        String rest = rule.substring(1);

                        // Crie uma nova variável para a parte restante
                        String newVariable = createNewVariable(variableRulesListAux, newVariableCounter);

                        if (newVariable.startsWith("X") && newVariable.endsWith(String.valueOf(newVariableCounter))) {
                            newVariableCounter++;

                            VariableRules variableRule = new VariableRules(newVariable, rest);

                            variableRulesListAux.add(variableRule);
                        }

                        // Adicione a nova regra
                        newRulesList.add(var1 + newVariable);

                        int index = getVariableRuleIndex(variableRules.variable, variableRulesListAux);

                        variableRulesListAux.get(index).setSubstitutionRules(newRulesList);
                    }
                }

                variableRules.setSubstitutionRules(newRulesList);
            }

            doesNotHaveBigRules = variableRulesListAux.stream()
                    .allMatch(variableRule -> variableRule.getSubstitutionRules().stream()
                            .allMatch(rule -> removeNumbers(rule).length() < 3));

        } while (!doesNotHaveBigRules);

        return variableRulesListAux;
    }

    public static String removeNumbers(String input) {
        // Usamos uma expressão regular para substituir todos os dígitos por uma string
        // vazia
        String result = input.replaceAll("\\d", "");
        return result;
    }

    private String createNewVariable(List<VariableRules> variableRulesList, int newVariableCounter) {
        String newVariable = "X" + newVariableCounter;

        List<String> filteredList = variableRulesList.stream()
        .filter(variableRule -> variableRule.getVariable().startsWith("X"))
        .map(vr -> vr.variable)
        .collect(Collectors.toList());

        Optional<String> matchingVariable = variableRulesList.stream()
                .filter(variableRule -> rulesListcontainsVariable(filteredList, variableRule.variable))
                .map(VariableRules::getVariable)
                .findFirst();

        if (matchingVariable.isPresent()) {
            return matchingVariable.get();
        } else {
            return newVariable;
        }
    }

    private String createNewMixedVariable(List<VariableRules> variableRulesList, int newVariableCounter,
            String variableString, String rule) {
        String newVariable = variableString + newVariableCounter;

        List<VariableRules> filteredList = variableRulesList.stream()
                .filter(variableRule -> variableRule.getVariable().startsWith(variableString))
                .collect(Collectors.toList());

        String matchingVariable = variableRulesList.stream()
                .filter(variableRule -> duplicatedRule(variableRule, variableRule.variable, rule))
                .map(v -> v.variable)
                .findFirst()
                .orElse(newVariable);

        return matchingVariable;
    }

    private boolean hasTerminalProduction(String actualRule, List<VariableRules> variableRules){
        List<String> newRuleList = Arrays.asList(actualRule.split(""));
        
        List<String> ruleMatches = new ArrayList<>();
            
        for (VariableRules variableRule : variableRules) {
            for (String rule : variableRule.substitutionRules) {
                if(newRuleList.contains(rule)){
                    ruleMatches.add(rule);
                }
            }
        }

        return newRuleList.size() == ruleMatches.size();
    }
    
    private boolean duplicatedRule(VariableRules variableRule, String variable, String newRule) {        
        List<String> newRuleList = Arrays.asList(newRule.split(""));

        String lowerCasePart = getLowercasePart(newRule);
        String upperCasePart = getUppercasePart(newRule);

        boolean isLowerCaseRule = newRuleList.stream()
            .allMatch(str -> str.chars().allMatch(Character::isLowerCase));

        List<String> ruleMatches = new ArrayList<>();

       if(isLowerCaseRule){       
            for (String rule : variableRule.substitutionRules) {
                if(newRuleList.contains(rule)){
                    ruleMatches.add(rule);
                }
            }        
       } else if(!lowerCasePart.isEmpty() && !upperCasePart.isEmpty() ){        
            for (String rule : variableRule.substitutionRules) {
                if(newRuleList.contains(rule)){
                    ruleMatches.add(rule);
                }
            }        
       }
       else{      
            for (String rule : variableRule.substitutionRules) {
                if(rule.equals(newRule)){
                    return true;
                }
            }        
       }

       boolean duplicatedRule = ruleMatches.size() == newRuleList.size();
       boolean lowerAndUpperDuplicated = ruleMatches.size() >= lowerCasePart.length();

        return duplicatedRule || lowerAndUpperDuplicated;
    }

    // Novo método para transformar regras com letras maiúsculas seguidas de
    // minúsculas
    public List<VariableRules> transformMixedProductions(List<VariableRules> variableRulesList) {
        List<VariableRules> variableRulesListAux = new ArrayList<>(variableRulesList);
        int newVariableCounter = 1;

        for (VariableRules variableRules : variableRulesList) {
            List<String> rulesList = variableRules.getSubstitutionRules();
            List<String> newRulesList = new ArrayList<>(rulesList);

            for (String rule : rulesList) {
                String lowercasePart = getLowercasePart(rule);
                String uppercasePart = getUppercasePart(rule);
                boolean isLowerCaseRule = rule.chars().allMatch(Character::isLowerCase);

                // Adicione uma verificação para regras de dois caracteres minúsculos
                if (rule.length() == 2 && isLowerCaseRule) {
                    // Adicione o código para criar novas variáveis para cada caractere
                    if(!hasTerminalProduction(rule, variableRulesListAux)){
                        String newVariable1 = "Y" + newVariableCounter;
                        String newVariable2 = "Y" + ++newVariableCounter;
                        
                        newVariableCounter++;

                        VariableRules newVariableRule1 = new VariableRules(newVariable1,
                            (String.valueOf(rule.charAt(0))));
                        VariableRules newVariableRule2 = new VariableRules(newVariable2,
                                (String.valueOf(rule.charAt(1))));

                        variableRulesListAux.add(newVariableRule1);
                        variableRulesListAux.add(newVariableRule2);

                        newRulesList.remove(rule);
                        newRulesList.add(newVariable1 + newVariable2);
                    }
                    else{
                        String newRule = addVariablesToRule(lowercasePart, variableRulesListAux, rule);
                        newRulesList.add(newRule);    
                        newRulesList.remove(rule);
                    }
                } else if (!lowercasePart.isEmpty() && !uppercasePart.isEmpty()) {
                    if(hasTerminalProduction(lowercasePart, variableRulesListAux)){
                        String newRule = addVariablesToRule(lowercasePart, variableRulesListAux, rule);

                        newRulesList.add(newRule);                        
                    }
                    else{
                    String newVariable = createNewMixedVariable(variableRulesListAux, newVariableCounter, "Y", rule);
                        if (newVariable.startsWith("Y") && newVariable.endsWith(String.valueOf(newVariableCounter))) {
                            newVariableCounter++;
                            VariableRules newVariableRule = new VariableRules(newVariable, lowercasePart);

                            variableRulesListAux.add(newVariableRule);
                        }
                    }

                    newRulesList.remove(rule);
                }
            }

            variableRules.setSubstitutionRules(newRulesList);
        }

        return variableRulesListAux;
    }

    private String addVariablesToRule(String lowerCasePart, List<VariableRules> variablesRulesList, String actualRule) {
        List<String> newRuleList = Arrays.asList(lowerCasePart.split(""));
        
        for (String lowerCaseString : newRuleList) {
            for (VariableRules variableRule : variablesRulesList) {
                for (String rule : variableRule.substitutionRules) {                
                    if(rule.equals(lowerCaseString)){
                        actualRule = actualRule.replace(lowerCaseString, variableRule.variable);
                    }
                }
            }
        }

        return actualRule;
    }

    private String getLowercasePart(String rule) {
        StringBuilder lowercasePart = new StringBuilder();

        for (int i = 0; i < rule.length(); i++) {
            char character = rule.charAt(i);
            if (Character.isLowerCase(character)) {
                lowercasePart.append(character);
            }
        }

        return lowercasePart.toString();
    }

    private String getUppercasePart(String rule) {
        StringBuilder uppercasePart = new StringBuilder();

        for (int i = 0; i < rule.length(); i++) {
            char character = rule.charAt(i);
            if (Character.isUpperCase(character)) {
                uppercasePart.append(character);
            }
        }

        return uppercasePart.toString();
    }

    // public boolean checkSentenceBelongsLanguage(Grammar grammar, String sentence) {
    //     Grammar fncGrammar = ToFncGrammar(grammar);
    //     List<VariableRules> fncRules = fncGrammar.rules;
    //     int n = sentence.length();

    //     // Inicialização da tabela CYK
    //     boolean[][][] table = new boolean[n][n][fncRules.size()];

    //     // Preenchimento da tabela com as produções unitárias
    //     for (int i = 0; i < n; i++) {
    //         for (int j = 0; j < fncRules.size(); j++) {
    //             VariableRules variableRules = fncRules.get(j);
    //             if (variableRules.getSubstitutionRules().contains(String.valueOf(sentence.charAt(i)))) {
    //                 table[i][i][j] = true;
    //             }
    //         }
    //     }

    //     // Preenchimento da tabela com as produções de dois símbolos ou mais
    //     for (int len = 2; len <= n; len++) {
    //         for (int i = 0; i <= n - len; i++) {
    //             int j = i + len - 1;
    //             for (int k = i; k < j; k++) {
    //                 for (VariableRules variableRules : fncRules) {
    //                     for (String rule : variableRules.getSubstitutionRules()) {
    //                         if (rule.length() == 2) {
    //                             char first = rule.charAt(0);
    //                             char second = rule.charAt(1);
    //                             int firstIndex = getVariableIndex(fncRules, String.valueOf(first));
    //                             int secondIndex = getVariableIndex(fncRules, String.valueOf(second));

    //                             if (table[i][k][firstIndex] && table[k + 1][j][secondIndex]) {
    //                                 table[i][j][getVariableIndex(fncRules, variableRules.getVariable())] = true;
    //                             }
    //                         }
    //                     }
    //                 }
    //             }
    //         }
    //     }

    //     // Verificar se a sentença pertence à linguagem
    //     int startVariableIndex = getVariableIndex(fncRules, String.valueOf(fncGrammar.startVariable));
    //     return table[0][n - 1][startVariableIndex];
    // }

    // private int getVariableIndex(List<VariableRules> fncRules, String variable) {
    //     for (int i = 0; i < fncRules.size(); i++) {
    //         if (fncRules.get(i).getVariable().equals(variable)) {
    //             return i;
    //         }
    //     }
    //     return -1;
    // }
}