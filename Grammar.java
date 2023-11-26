import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

public class Grammar {
    String name;
    List<String> variables;
    List<String> alphabet;
    List<VariableRules> rules;
    String startVariable;

    Grammar() {
        this.name = "";
        this.variables = new ArrayList<String>();
        this.alphabet = new ArrayList<String>();
        this.rules = new ArrayList<VariableRules>();
        this.startVariable = "";
    }

    Grammar(String name, List<String> variables, List<String> alphabet, List<VariableRules> rules,
            String startVariable) {
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

    public <T> void PrintStringList(List<T> list) {
        for (T item : list) {
            System.out.println(item);
        }
    }

    public Map<String, Set<String>> GetUnitRelation(List<String> nullableVariables) {
        GrammarConversor grammarConversor = new GrammarConversor();
        Map<String, Set<String>> UG = new HashMap<>();

        for (VariableRules variableRule : this.rules) {
            for (String rule : variableRule.substitutionRules) {
                String lowercasePart = grammarConversor.getLowercasePart(rule);
                boolean isUpperCasePartNullable = rule.chars() // Cria um stream de inteiros representando os caracteres
                                                               // da string
                        .mapToObj(c -> (char) c) // Converte cada inteiro de volta para um caractere
                        .filter(Character::isUpperCase) // Filtra apenas caracteres maiúsculos
                        .allMatch(c -> nullableVariables.contains(String.valueOf(c))); // Verifica se cada caractere
                                                                                       // maiúsculo está na lista de
                                                                                       // anuláveis

                boolean isAnyUpperCaseNullable = rule.chars() // Cria um stream de inteiros representando os caracteres
                        // da string
                        .mapToObj(c -> (char) c) // Converte cada inteiro de volta para um caractere
                        .filter(Character::isUpperCase) // Filtra apenas caracteres maiúsculos
                        .anyMatch(c -> nullableVariables.contains(String.valueOf(c))); // Verifica se cada caractere
                                                                                       // maiúsculo está na lista de
                                                                                       // anuláveis

                // Split the production to consider individual symbols
                String[] symbols = rule.split("");

                // Check if the rule is a direct unit rule (single non-terminal)
                if (symbols.length == 1 && this.variables.contains(symbols[0])) {
                    UG.computeIfAbsent(variableRule.variable, k -> new HashSet<>()).add(symbols[0]);
                } else if (isUpperCasePartNullable && !lowercasePart.equals("lambda")) {
                    // If the rule consists of a terminal followed by a nullable non-terminal, it is
                    // a unit rule
                    UG.computeIfAbsent(variableRule.variable, k -> new HashSet<>()).add(lowercasePart);
                }
                else if (rule.length() == 2 && isAnyUpperCaseNullable) {
                    int nullablePosition = FindNullablePosition(symbols, nullableVariables);
                    UG.computeIfAbsent(variableRule.variable, k -> new HashSet<>()).add(symbols[nullablePosition]);
                }
            }
        }

        return UG;
    }

    private int FindNullablePosition(String[] symbols, List<String> nullableVariables){
        for (int i = 0; i < symbols.length-1; i++) {
            if(!nullableVariables.contains(symbols[i])){
                return i;
            }
        }

        return -1;
    }

    public Set<String> ComputeV(Grammar grammar) {
        Set<String> V = new HashSet<>();

        for (VariableRules variableRule : grammar.rules) {
            // Add all non-terminal variables to V
            V.add(variableRule.variable);

            for (String rule : variableRule.substitutionRules) {
                V.add(rule);
            }
        }

        // Add all terminal symbols to V
        V.addAll(grammar.alphabet);

        return V;
    }

    public Map<String, Set<String>> GetInverseUnitGraph(Map<String, Set<String>> unitRelations,
            Set<String> grammarSymbols) {
        // Inicializa o grafo com nós para cada símbolo da gramática
        Map<String, Set<String>> inverseUnitGraph = new HashMap<>();

        for (String symbol : grammarSymbols) {
            inverseUnitGraph.put(symbol, new HashSet<>());
        }

        // Adiciona arestas ao grafo para cada relação unitária inversa
        for (Map.Entry<String, Set<String>> entry : unitRelations.entrySet()) {
            String from = entry.getKey();
            for (String to : entry.getValue()) {
                // Adiciona uma aresta de 'to' para 'from'
                inverseUnitGraph.get(to).add(from);
            }
        }

        return inverseUnitGraph;
    }

    // Realiza uma busca em profundidade (DFS) para encontrar todos os símbolos que
    // podem derivar um dado símbolo
    public static Set<String> dfs(Map<String, Set<String>> graph, String start) {
        Set<String> visited = new HashSet<>();
        Stack<String> stack = new Stack<>();

        stack.push(start);
        while (!stack.isEmpty()) {
            String current = stack.pop();
            if (!visited.contains(current)) {
                visited.add(current);
                for (String neighbor : graph.getOrDefault(current, new HashSet<>())) {
                    stack.push(neighbor);
                }
            }
        }

        return visited;
    }

    public Map<String, Set<String>> computeTransitiveClosure(Map<String, Set<String>> graph, Set<String> allSymbols) {
        Map<String, Set<String>> allSymbolsTransitiveClosure = new HashMap<String, Set<String>>();

        for (String startSymbol : allSymbols) {
            Set<String> closure = new HashSet<>();
            Stack<String> stack = new Stack<>();
            stack.push(startSymbol);

            while (!stack.isEmpty()) {
                String symbol = stack.pop();
                if (!closure.contains(symbol)) {
                    closure.add(symbol);
                    // Se o símbolo atual é um não-terminal e tem arestas saindo dele, continue a
                    // busca
                    if (graph.containsKey(symbol)) {
                        for (String nextSymbol : graph.get(symbol)) {
                            stack.push(nextSymbol);
                        }
                    }
                }
            }

            allSymbolsTransitiveClosure.put(startSymbol, closure);
        }

        return allSymbolsTransitiveClosure;
    }

    public void PrintGrammar() {
        System.out.println("\n============");
        System.out.println("\nGRAMMAR: " + name + "\n");

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
        System.out.println(this.startVariable + "\n");

        System.out.println("============");
    }
}
