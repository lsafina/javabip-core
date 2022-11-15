package org.javabip.verification.report;

public class ConstructorReport {
    private Boolean statePredicate;
    private Boolean componentInvariant;

    //public ConstructorReport(){}

    public ConstructorReport(Boolean statePredicate, Boolean componentInvariant) {
        this.statePredicate = statePredicate;
        this.componentInvariant = componentInvariant;
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
}
