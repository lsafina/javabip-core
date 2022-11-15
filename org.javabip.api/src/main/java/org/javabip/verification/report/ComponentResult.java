package org.javabip.verification.report;

import java.util.ArrayList;

public class ComponentResult {
    String componentName;
    ConstructorReport constructorResults;
    ArrayList<TransactionReport> transactionResults;

    //public VerCorsReportResults(){}

    public ComponentResult(ConstructorReport constructorResults, ArrayList<TransactionReport> transactionResults, String componentName) {
        this.componentName = componentName;
        this.constructorResults = constructorResults;
        this.transactionResults = transactionResults;
    }

    public ConstructorReport getConstructorResults() {
        return constructorResults;
    }

    public void setConstructorResults(ConstructorReport constructorResults) {
        this.constructorResults = constructorResults;
    }

    public ArrayList<TransactionReport> getTransactionResults() {
        return transactionResults;
    }

    public void setTransactionResults(ArrayList<TransactionReport> transactionResults) {
        this.transactionResults = transactionResults;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }
}
