package org.javabip.verification.report;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class VerCorsReportParser {
    protected static final Logger logger = LogManager.getLogger();
    //json consts
    final String STATE_INVARIANT = "stateInvariant";
    final String COMPONENT_INVARIANT = "componentInvariant";
    final String PRE_CONDITION = "precondition";
    final String POST_CONDITION = "postcondition";
    final String CONSTRUCTOR = "constructor";
    final String TRANSITIONS = "transitions";
    final String PROVEN = "proven";
    final String NOT_PROVEN = "not proven";
    final String SIGNATURE = "signature";
    final String NAME = "name";
    final String SOURCE = "source";
    final String TARGET = "target";
    final String GUARD = "guard";
    final String RESULTS = "results";

    final String FILE_PATH = "../verificationReport.json";

    public ArrayList<ComponentResult> parseVerCorsResults() throws IOException, ParseException {
        JSONParser parser = new JSONParser();

        try {
            JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(FILE_PATH));
            System.out.println("VERCORS PARSER: USING VERIFICATION REPORT");


            Set componentsSet = jsonObject.keySet();
            ArrayList<ComponentResult> componentResults = new ArrayList<>();

            for (Object component : componentsSet) {
                Object[] componentNameSplit = Arrays.stream(component.toString().split("\\.")).toArray();
                String componentName = componentNameSplit[componentNameSplit.length-1].toString();
                JSONObject componentInvariants = (JSONObject) jsonObject.get(component);

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
                    Boolean transactionPreConditionResult = parseInvariantResult(String.valueOf(results.get(PRE_CONDITION)));
                    Boolean transactionPostConditionResult = parseInvariantResult(String.valueOf(results.get(POST_CONDITION)));

                    //collect transaction data
                    JSONObject data = (JSONObject) transaction.get(SIGNATURE);
                    Object guard = data.get(GUARD);

                    transactionReports.add(
                            new TransactionReport(
                                    String.valueOf(data.get(NAME)),
                                    String.valueOf(data.get(SOURCE)),
                                    String.valueOf(data.get(TARGET)),
                                    guard == null ? "" : guard.toString(),
                                    transactionPreConditionResult,
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
            logger.error("VerCors report was not found.");
            return new ArrayList<>();
        } catch (ParseException e){
            logger.error("VerCors report was not parsed correctly and will be not used for runtime verification");
            return new ArrayList<>();
        }
    }

    Boolean parseInvariantResult(String result) {
        return result != null && !result.equals(NOT_PROVEN);
    }
}
