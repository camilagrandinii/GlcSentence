import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public Map<String, Set<String>> GetUnitRleation(List<String> nullableVariables) {
        Map<String, Set<String>> UG = new HashMap<>();

        for (VariableRules rule : this.rules) {
            String nonTerminal = rule.getVariable();
            for (String production : rule.getSubstitutionRules()) {
                // Split the production to consider individual symbols
                String[] symbols = production.split(" ");
                boolean isUnitProduction = symbols.length == 1;

                if (isUnitProduction) {
                    // Direct unit relation A -> y
                    UG.computeIfAbsent(nonTerminal, k -> new HashSet<>()).add(symbols[0]);
                } else {
                    // Check if all symbols except one are nullable
                    for (String symbol : symbols) {
                        if (!nullableVariables.contains(symbol)) {
                            // This is the non-nullable symbol 'y'
                            UG.computeIfAbsent(nonTerminal, k -> new HashSet<>()).add(symbol);
                            break; // Only one non-nullable symbol is needed for UG
                        }
                    }
                }
            }
        }

        return UG;
    }

    public Set<String> ComputeV() {
        Set<String> V = new HashSet<>();

        // Add all non-terminal variables to V
        V.addAll(this.variables);

        // Add all terminal symbols to V
        V.addAll(this.alphabet);

        // Optionally, you can add all symbols present in the rules to V
        for (VariableRules vr : this.rules) {
            for (String rule : vr.getSubstitutionRules()) {
                // Assuming that each rule is a space-separated string of symbols
                String[] symbols = rule.split(" ");
                for (String symbol : symbols) {
                    if (!symbol.equals("ε")) { // Don't add the empty string symbol
                        V.add(symbol);
                    }
                }
            }
        }
        
        return V;
    }

     // Method to compute the inverse unit graph IG from the set of all symbols V and UG
     public Map<String, Set<String>> GetInverseUnitGraph(Set<String> V, Map<String, Set<String>> UG) {
        // Initialize the graph with nodes for every symbol in V
        Map<String, Set<String>> inverseUnitGraph = new HashMap<>();
        for (String symbol : V) {
            inverseUnitGraph.put(symbol, new HashSet<>()); // Each symbol in V becomes a node in the graph
        }

        // Add edges to the graph for each unit relation (y, A) for (A, y) in UG
        for (Map.Entry<String, Set<String>> entry : UG.entrySet()) {
            String A = entry.getKey();
            for (String y : entry.getValue()) {
                // Add an edge from y to A in the inverse unit graph
                if(!y.equals("lambda")){
                    inverseUnitGraph.get(y).add(A);
                }
            }
        }

        return inverseUnitGraph;
    }

    // public Map<String, Set<String>> GetInverseUnitRelation(Map<String, Set<String>> UG) {
    //     Map<String, Set<String>> inverseUG = new HashMap<>();

    //     // Iterate through each entry in UG
    //     for (Map.Entry<String, Set<String>> entry : UG.entrySet()) {
    //         String A = entry.getKey();
    //         Set<String> symbols = entry.getValue();

    //         // For each symbol y related to A, add the inverse relation (y, A) to inverseUG
    //         for (String y : symbols) {
    //             inverseUG.computeIfAbsent(y, k -> new HashSet<>()).add(A);
    //         }
    //     }

    //     return inverseUG;
    // }
    
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
