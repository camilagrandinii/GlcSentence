import java.util.ArrayList;
import java.util.List;

public class VariableRules {
    String variable;
    List<String> substitutionRules;

    VariableRules(String variable, List<String> substitutionRules){
        this.variable = variable;
        this.substitutionRules = substitutionRules;
    }

    VariableRules(String variable, String rule){
        this.variable = variable;

        List<String> substitutionRulesList = new ArrayList<>();
        substitutionRulesList.add(rule);

        this.substitutionRules = substitutionRulesList;
    }

    public String getVariable(){
        return this.variable;
    }

    public void setRule(String substitutionRule) {
        this.substitutionRules.add(substitutionRule);
    }

    public void setRules(List<String> substitutionRules) {
        this.substitutionRules.addAll(substitutionRules);
    }

    public List<String> getSubstitutionRules(){
        return this.substitutionRules;
    }

    public void setSubstitutionRules(List<String> substitutionRules){
        this.substitutionRules = substitutionRules;
    }

    public String getRule(){
        return this.variable + " -> " + this.substitutionRules;
    }
}
