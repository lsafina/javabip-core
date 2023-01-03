package org.javabip.verification.report;

import org.javabip.annotations.Transition;

public class TransactionReport {
    private String name;
    private String source;
    private String target;
    private String guard;

    private Boolean preCondition;
    private Boolean postCondition;
    private Boolean statePredicate;
    private Boolean componentInvariant;

    //public TransactionReport(){}

    public TransactionReport(String name, String source, String target, String guard, Boolean preCondition, Boolean postCondition, Boolean statePredicate, Boolean componentInvariant) {
        this.name = name;
        this.source = source;
        this.target = target;
        this.guard = guard;
        this.preCondition = preCondition;
        this.postCondition = postCondition;
        this.statePredicate = statePredicate;
        this.componentInvariant = componentInvariant;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getGuard() {
        return guard;
    }

    public void setGuard(String guard) {
        this.guard = guard;
    }

    public Boolean getPreCondition() {
        return preCondition;
    }

    public Boolean getPostCondition() {
        return postCondition;
    }

    public void setPostCondition(Boolean postCondition) {
        this.postCondition = postCondition;
    }

    public Boolean getStatePredicate() {
        return statePredicate;
    }

    public void setStatePredicate(Boolean statePredicate) {
        this.statePredicate = statePredicate;
    }

    public Boolean getComponentInvariant() {
        return componentInvariant;
    }

    public void setComponentInvariant(Boolean componentInvariant) {
        this.componentInvariant = componentInvariant;
    }

    public boolean equalsTransition(Transition transition){
        if (!transition.name().equals(this.name)) return false;
        if (!transition.source().equals(this.source)) return false;
        if (!transition.target().equals(this.target)) return false;
        return (transition.guard().equals(this.guard));
    }
}
