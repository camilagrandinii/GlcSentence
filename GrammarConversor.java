import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GrammarConversor {
    public boolean createdNewVariable = false;        
    List<String> availableVariables = new ArrayList<>();
    List<String> newlyAddedVariables = new ArrayList<>();
    List<VariableRules> removedLambdaVariableRules = new ArrayList<>();

    GrammarConversor(Grammar grammar){
        getAvailableVariables(grammar.rules);
        List<String> nullableVariables = findAnulaveis(grammar.rules);
        removedLambdaVariableRules = eliminarRegrasLambda(grammar.rules, nullableVariables);
    }

    public Grammar ToFncGrammar(Grammar grammar) {
        List<VariableRules> newVariablesRules = RemoveUnitRules(removedLambdaVariableRules);

        newVariablesRules = transformLongProductions(newVariablesRules);
        newVariablesRules = transformMixedProductions(newVariablesRules);

        grammar.rules = newVariablesRules;
        return grammar;
    }

    public Grammar To2NfGrammar(Grammar grammar){
        List<VariableRules> newVariablesRules = transformLongProductions(removedLambdaVariableRules);

        grammar.rules = newVariablesRules;
        return grammar;
    }

    private void getAvailableVariables(List<VariableRules> rules) {
        for (char letra = 'A'; letra <= 'Z'; letra++) {
            availableVariables.add(String.valueOf(letra));
        }

        for (VariableRules variableRule : rules) {
            if(availableVariables.contains(variableRule.variable)){
                availableVariables.remove(variableRule.variable);
            }
        }
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

    // - fazer conversão de aB para U -> a UB
    public List<VariableRules> transformLongProductions(List<VariableRules> variableRulesList) {
        boolean doesNotHaveBigRules = false;
        List<VariableRules> variableRulesListAux = new ArrayList<VariableRules>(variableRulesList);

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
                        String newVariable = createNewVariable(variableRulesListAux);

                        if (createdNewVariable) {
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

    private String createNewVariable(List<VariableRules> variableRulesList) {
        String newVariable = availableVariables.get(0);
        
        List<String> filteredList = variableRulesList.stream()
        .filter(variableRule -> newlyAddedVariables.contains(variableRule.variable))
        .map(vr -> vr.variable)
        .collect(Collectors.toList());
        
        Optional<String> matchingVariable = variableRulesList.stream()
        .filter(variableRule -> rulesListcontainsVariable(filteredList, variableRule.variable))
        .map(VariableRules::getVariable)
        .findFirst();
        
        if (matchingVariable.isPresent()) {
            createdNewVariable = false;
            return matchingVariable.get();
        } else {
            createdNewVariable = true;
            availableVariables.remove(newVariable);
            newlyAddedVariables.add(newVariable);
            return newVariable;
        }
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

    // Novo método para transformar regras com letras maiúsculas seguidas de
    // minúsculas
    public List<VariableRules> transformMixedProductions(List<VariableRules> variableRulesList) {
        List<VariableRules> variableRulesListAux = new ArrayList<>(variableRulesList);

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
                        String newVariable1 = availableVariables.get(0);
                        availableVariables.remove(newVariable1);
                        newlyAddedVariables.add(newVariable1);

                        String newVariable2 = availableVariables.get(0);
                        availableVariables.remove(newVariable2);
                        newlyAddedVariables.add(newVariable2);
                        
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
                        List<String> terminalsToCreate = GetTerminalsToCreate(lowercasePart, variableRulesListAux);

                        for (String terminalToCreate : terminalsToCreate) {

                            String newVariable1 = availableVariables.get(0);
                            availableVariables.remove(newVariable1);
                            newlyAddedVariables.add(newVariable1);
                            
                            VariableRules newVariableRule1 = new VariableRules(newVariable1, terminalToCreate);
                            variableRulesListAux.add(newVariableRule1);
                        }                                                                                                                     
                       
                        String newRule = addVariablesToRule(lowercasePart, variableRulesListAux, rule);

                        newRulesList.add(newRule);         
                    }                 

                    newRulesList.remove(rule);
                }
            }

            variableRules.setSubstitutionRules(newRulesList);
        }

        return variableRulesListAux;
    }

    private List<String> GetTerminalsToCreate(String lowerCasePart,  List<VariableRules> variablesRulesList) {
        List<String> lowerCaseChar = Arrays.asList(lowerCasePart.split(""));
        List<String> lowerCaseCharAux = new ArrayList<>(lowerCaseChar);

        for (String lowerCase : lowerCaseChar) {
            for (VariableRules variableRule : variablesRulesList) {
                for (String rule : variableRule.substitutionRules) {
                    if(lowerCase.equals(rule)){
                        lowerCaseCharAux.remove(lowerCase);
                    }
                }
            }
        }

        return lowerCaseCharAux;
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
}