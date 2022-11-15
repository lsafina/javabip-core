package org.javabip.verification.report;

import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public class VerCorsReportParser {
    //json consts
    final String STATE_INVARIANT = "stateInvariant";
    final String COMPONENT_INVARIANT = "componentInvariant";
    final String POST_CONDITION = "postCondition";
    final String CONSTRUCTOR = "<constructor>";
    final String TRANSITIONS = "transitions";
    final String PROVEN = "proven";
    final String NOT_PROVEN = "not proven";
    final String DATA = "data";
    final String NAME = "name";
    final String SOURCE = "source";
    final String TARGET = "target";
    final String GUARD = "guard";
    final String RESULTS = "results";

    final String FILE_PATH = "/Users/lsafina/Projects/javabip-core/org.javabip.api/src/main/java/org/javabip/verification/visitors/json.txt";

    public static void main(String[] args) throws JSONException, IOException, ParseException {
        VerCorsReportParser a = new VerCorsReportParser();
        ArrayList<ComponentResult> componentResults = a.parseVerCorsResults();
        System.out.printf("1");
    }

    public ArrayList<ComponentResult> parseVerCorsResults() throws IOException, ParseException {
        JSONParser parser = new JSONParser();

        try {
            JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(FILE_PATH));
            Set componentSet = jsonObject.keySet();
            ArrayList<ComponentResult> componentResults = new ArrayList<>();

            for (Object cn : componentSet) {
                String componentName = cn.toString();
                JSONObject componentInvariants = (JSONObject) jsonObject.get(cn);

                //collect constructor results
                JSONObject constructorInvariants = (JSONObject) componentInvariants.get(CONSTRUCTOR);
                Boolean stateInvariantResult = parseInvariantResult(String.valueOf(constructorInvariants.get(STATE_INVARIANT)));
                Boolean componentInvariantResult = parseInvariantResult(String.valueOf(constructorInvariants.get(COMPONENT_INVARIANT)));
                ConstructorReport constructorReport = new ConstructorReport(stateInvariantResult, componentInvariantResult);

                //collect transition results
                JSONArray transitionsInvariants = (JSONArray) componentInvariants.get(TRANSITIONS);
                ArrayList<TransactionReport> transactionReports = new ArrayList<>();
                for (Object t : transitionsInvariants) {
                    JSONObject transaction = (JSONObject) t;

                    //collect transaction result
                    JSONObject results = (JSONObject) transaction.get(RESULTS);
                    Boolean transactionInvariantResult = parseInvariantResult(String.valueOf(results.get(COMPONENT_INVARIANT)));
                    Boolean transactionStateInvariantResult = parseInvariantResult(String.valueOf(results.get(STATE_INVARIANT)));
                    Boolean transactionPostConditionResult = parseInvariantResult(String.valueOf(results.get(POST_CONDITION)));

                    //collect transaction data
                    JSONObject data = (JSONObject) transaction.get(DATA);
                    Object guard = data.get(GUARD);

                    transactionReports.add(
                            new TransactionReport(
                                    String.valueOf(data.get(NAME)),
                                    String.valueOf(data.get(SOURCE)),
                                    String.valueOf(data.get(TARGET)),
                                    guard == null ? null : guard.toString(),
                                    transactionPostConditionResult,
                                    transactionStateInvariantResult,
                                    transactionInvariantResult
                            )
                    );
                }

                componentResults.add(
                        new ComponentResult(
                                constructorReport,
                                transactionReports,
                                componentName
                        )
                );
            }
            return componentResults;

        } catch (FileNotFoundException e) {
            //do smart stuff
            throw e;
        } catch (ParseException e) {
            //do smart stuff
            throw e;
        }
    }

    Boolean parseInvariantResult(String result) {
        return result != null && !result.equals(NOT_PROVEN);
    }
}
