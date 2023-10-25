import java.util.List;

public class VariableRules {
    String variable;
    List<String> substitutionRule;

    VariableRules(String variable, List<String> substitutionRules){
        this.variable = variable;
        this.substitutionRule = substitutionRules;
    }

    public void setRule(String substitutionRule) {
        this.substitutionRule.add(substitutionRule);
    }

    public String getRule(){
        return this.variable + " -> " + this.substitutionRule;
    }
}
