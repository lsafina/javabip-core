/*
 * Copyright 2012-2016 École polytechnique fédérale de Lausanne (EPFL), Switzerland
 * Copyright 2012-2016 Crossing-Tech SA, Switzerland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Simon Bliudze, Anastasia Mavridou, Radoslaw Szymanek and Alina Zolotukhina
 */
package org.javabip.executor;

import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javabip.api.*;
import org.javabip.exceptions.BIPException;

import java.lang.invoke.MethodHandle;
import java.util.*;

/**
 * Implements the Behaviour and ExecutableBehaviour interfaces, providing the behaviour of the component together with
 * additional helper structures.
 *
 * @author Alina Zolotukhina
 */
class BehaviourImplRV extends BehaviourImpl implements ExecutableBehaviour {
    private String currentState;

    private String componentType;

    private Set<String> states;
    private ArrayList<Port> allPorts;
    private ArrayList<Port> enforceablePorts;
    private Map<String, Port> spontaneousPorts;
    /**
     * For each port provides data it needs for guards
     */
    private Hashtable<Port, Set<Data<?>>> portToDataInForGuard;
    /**
     * For each port provides data it needs for transitions
     */
    private Hashtable<Port, Set<Data<?>>> portToDataInForTransition;

    /**
     * Maps state to its transitions
     */
    private Hashtable<String, ArrayList<ExecutableTransition>> stateTransitions;
    /**
     * Maps state to its enforceable ports
     */
    private Hashtable<String, Set<Port>> stateToPorts;
    // TODO DISCUSS since it's a hashtable, there cannot be several transitions with the same name
    // for different data variables (see theory?)
    /**
     * Gives a Transition instance from two keys - first key is currentState, second key is transition name.
     */
    private Hashtable<String, Hashtable<String, ExecutableTransition>> nameToTransition;

    private ArrayList<ExecutableTransition> allTransitions;
    private ArrayList<ExecutableTransition> internalTransitions;
    private ArrayList<ExecutableTransition> spontaneousTransitions;
    private ArrayList<ExecutableTransition> enforceableTransitions;
    /**
     * for each enforceable transition get its port instance
     */
    private Hashtable<ExecutableTransition, Port> transitionToPort;

    /**
     * The list of guards whose evaluation does not depend on data
     */
    private ArrayList<Guard> guardsWithoutData;
    /**
     * The list of guards whose evaluation depends on data
     */
    private ArrayList<Guard> guardsWithData;

    /**
     * The list of dataOut variables for this component
     */
    private ArrayList<DataOutImpl<?>> dataOut;

    /**
     * The map between the name of the out variable and the method computing it
     */
    private Hashtable<String, MethodHandle> dataOutName;

    private Map<String, List<Port>> dataFromTransitionToPorts;
    private Map<String, List<Port>> dataFromGuardsToPorts;
    private Map<String, List<Port>> portsNeedingData;

    private InvariantImpl invariant;
    private HashMap<TransitionImpl, InvariantImpl> transitionToPreConditionMap;
    private HashMap<TransitionImpl, InvariantImpl> transitionToPostConditionMap;
    private HashMap<String, InvariantImpl> stateToPredicateMap;

    private Object bipComponent;
    private Class<?> componentClass;

    //private Logger logger = LoggerFactory.getLogger(BehaviourImpl.class);
    protected static final Logger logger = LogManager.getLogger();

    // ******************************** Constructors *********************************************

    /**
     * Calling the BehaviourImpl constructor, setting RV properties
     *
     * @param componentType
     * @param currentState
     * @param allTransitions
     * @param componentPorts
     * @param states
     * @param values
     * @param component
     * @param componentInvariant
     * @param transitionToPreConditionMap
     * @param transitionToPostConditionMap
     * @param stateToPredicateMap
     * @throws BIPException
     */
    public BehaviourImplRV(String componentType, String currentState, ArrayList<ExecutableTransition> allTransitions, ArrayList<Port> componentPorts, HashSet<String> states, Collection<Guard> values, ArrayList<DataOutImpl<?>> dataOut, Hashtable<String, MethodHandle> dataOutName, Object component, InvariantImpl componentInvariant, HashMap<TransitionImpl, InvariantImpl> transitionToPreConditionMap, HashMap<TransitionImpl, InvariantImpl> transitionToPostConditionMap, HashMap<String, InvariantImpl> stateToPredicateMap) {
        super(componentType, currentState, allTransitions, componentPorts, states, values, dataOut, dataOutName, component);

        this.invariant = componentInvariant;
        this.transitionToPreConditionMap = transitionToPreConditionMap;
        this.transitionToPostConditionMap = transitionToPostConditionMap;
        this.stateToPredicateMap = stateToPredicateMap;
    }

    // ************************************ Execution ********************************************

    public void execute(String portID, Map<String, ?> data) throws BIPException {
        // this component does not take part in the interaction

        if (portID == null) {
            return;
        }

        //check state predicate;
        checkStatePredicate(currentState);

        // getTransition works correctly with spontaneous as well, as it addresses the list of all transitions
        ExecutableTransition transition = getTransition(currentState, portID);

        //checkTransitionPreCondition(transition);
        checkTransitionCondition(transition, true);

        invokeMethod(transition, data);

        //checkTransitionPostcondition(transition);
        checkTransitionCondition(transition, false);
    }

    // ExecutorKernel, the owner of BehaviourImpl is checking the correctness of the execution.
    public void executePort(String portID) throws BIPException {
        // this component does not take part in the interaction
        if (portID == null) {
            return;
        }
        ExecutableTransition transition = getTransition(currentState, portID);
        if (transition == null) { // this shouldn't normally happen
            throw new BIPException("The spontaneous transition for port " + portID + " cannot be null after inform");
        }

        //checkTransitionPreCondition(transition);
        checkTransitionCondition(transition, true);

        invokeMethod(transition);

        //checkTransitionPostcondition(transition);
        checkTransitionCondition(transition, false);
    }

    // ****************************** End of Execution *******************************************

    // ****************************** Runtime Verification ***************************************
    public Pair<Boolean, String> checkInvariant() {
        //TODO result is ignored
        if (invariant != null)
            synchronized (this) {
                try {
                    Pair<Boolean, String> result = new Pair<>(invariant.evaluateInvariant(componentClass, bipComponent), invariant.expression());
                    if ( !result.getKey()) {
                        logger.error("Invariant violation: " + result.getValue());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        //if there is no invariant we just ignore this execution
        return new Pair<>(true, "");
    }

    public Pair<Boolean, String> checkTransitionCondition(Object transition, Boolean pre) {
        InvariantImpl condition;
        String message;
        if (pre) {
            condition = transitionToPreConditionMap.get((TransitionImpl) transition);
            message = "Pre-condition violation: ";
        } else {
            condition = transitionToPostConditionMap.get((TransitionImpl) transition);
            message = "Post-condition violation: ";
        }

        if (condition != null) {
            synchronized (this) {
                try {
                    Pair<Boolean, String> result = new Pair<>(condition.evaluateInvariant(componentClass, bipComponent), condition.expression());
                    if (!result.getKey()) {
                        logger.error(message + result.getValue());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        //if there is no conditions we just ignore this execution
        return new Pair<>(true, "");
    }

    public Pair<Boolean, String> checkStatePredicate(String currentState) {
        InvariantImpl statePredicate = stateToPredicateMap.get(currentState);
        if (statePredicate != null) {
            synchronized (this) {
                try {
                    Pair<Boolean, String> result = new Pair<>(statePredicate.evaluateInvariant(componentClass, bipComponent), statePredicate.expression());
                    if (!result.getKey()) {
                        logger.error("State predicate violation: " + result.getValue() + ", for the state: " + currentState);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        //if there is no state predicate for a state we just ignore this execution
        return new Pair<>(true, "");
    }
    // ****************************** End of Runtime Verification *******************************************
}
