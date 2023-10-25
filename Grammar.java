import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Grammar {
    String name;
    List<String> variables;
    List<String> alphabet;
    List<VariableRules> rules;
    String startVariable;

    Grammar(){
        this.name = "";
        this.variables = new ArrayList<String>();
        this.alphabet = new ArrayList<String>();
        this.rules = new ArrayList<VariableRules>();
        this.startVariable = "";
    }

    Grammar(String name, List<String> variables, List<String> alphabet, List<VariableRules> rules, String startVariable){
        this.name = name;
        this.variables = new ArrayList<String>();
        this.alphabet = new ArrayList<String>();
        this.rules = new ArrayList<VariableRules>();
        this.startVariable = startVariable;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVariables(String[] variablesArray) {
        for (String variable : variablesArray) {
            this.variables.add(variable);
        }
    }
    
    public void setAlphabet(String[] alphabetArray) {
        for (String alphabetSymbol : alphabetArray) {
            this.alphabet.add(alphabetSymbol);
        }
    }
    
    public void setVariableRules(String variable, String[] specificVariableRules) {

        List<String> specificVariableRulesList = new ArrayList<String>();

        for (String rule : specificVariableRules) {
            specificVariableRulesList.add(rule);
        }

        VariableRules variableRule = new VariableRules(variable, specificVariableRulesList);

        this.rules.add(variableRule);
    }
    
    public void setStartVariable(String startVariable) {
        this.startVariable = startVariable;
    }

    public <T> void PrintStringList(List<T> list){
        for (T item : list) {
            System.out.println(item);
        }
    }
    
    public void PrintGrammar(){
        System.out.println("\n============");
        System.out.println("\nGRAMMAR: "+name+"\n");

        System.out.println("\nVariables: ");
        PrintStringList(variables);

        System.out.println("\nAlphabet: ");
        PrintStringList(alphabet);

        System.out.println("\nRules: ");
        List<String> ruleStrings = rules.stream()
            .map(VariableRules::getRule)
            .collect(Collectors.toList());  

        PrintStringList(ruleStrings);   
        
        System.out.println("\nStart Variable:");
        System.out.println(this.startVariable+"\n");

        System.out.println("============");
    }
}
