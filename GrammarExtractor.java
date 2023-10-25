public class GrammarExtractor {
    public Grammar ExtractGrammar(String input, Grammar grammar){
        
        String[] linhaSplited;

        input = input.replace('"', ' ')
                     .replace("(", "")
                     .replace(")", " ")
                     .replace("=", " ")
                     .replace(")", "")
                     .replace("},", "")
                     .replace("}", "")
                     .replace(" ", "");

        linhaSplited = input.split("\\{");

        String grammarName = linhaSplited[0];
        grammar.setName(grammarName);

        String[] variablesArray = linhaSplited[1].split(",");
        grammar.setVariables(variablesArray);

        String[] alphabetArray = linhaSplited[2].split(",");
        grammar.setAlphabet(alphabetArray);

        String[] rulesArray = linhaSplited[3].split(",");

        for (String rule : rulesArray) {
             String[] variableRulesArray = rule.split("->");
             variableRulesArray[0] = variableRulesArray[0].replace(" ", "");
             String[] specificVariableRules = variableRulesArray[1].split("\\|");

             grammar.setVariableRules(variableRulesArray[0], specificVariableRules);
        }

        grammar.setStartVariable(linhaSplited[4]);

        return grammar;
    }
}
