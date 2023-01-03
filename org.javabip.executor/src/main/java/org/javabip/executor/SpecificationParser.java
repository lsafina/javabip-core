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

import org.javabip.annotations.*;
import org.javabip.api.Behaviour;
import org.javabip.api.ComponentProvider;
import org.javabip.api.ExecutableBehaviour;
import org.javabip.exceptions.BIPException;
import org.javabip.verification.report.ComponentResult;
import org.javabip.verification.report.ConstructorReport;
import org.javabip.verification.report.TransactionReport;
import org.javabip.verification.report.VerCorsReportParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

/**
 * Parses the component specification to create a behaviour instance. The behaviour is specified either via annotations
 * of programmatically.
 *
 * @author Alina Zolotukhina
 */
public abstract class SpecificationParser implements ComponentProvider {

    protected Object bipComponent;
    protected ExecutableBehaviour behaviour;
    protected Class<?> componentClass;
    private Logger logger = LoggerFactory.getLogger(SpecificationParser.class);

    /**
     * Creates an instance of SpecificationParser
     *
     * @param bipComponent      the BIP component specification to parse
     * @param useAnnotationSpec true, if the annotations are used; false, if the behaviour is specified programmatically
     */
    public SpecificationParser(Object bipComponent, boolean useAnnotationSpec) throws BIPException {
        this.bipComponent = bipComponent;
        this.componentClass = bipComponent.getClass();

        if (useAnnotationSpec) {
            this.behaviour = parseAnnotations(bipComponent.getClass()).build(this);
        } else {
            this.behaviour = getExecutableBehaviour(bipComponent.getClass()).build(this);
        }

    }

    public SpecificationParser(Object bipComponent, boolean useAnnotationSpec, boolean useRuntimeVerification) throws BIPException {
        this.bipComponent = bipComponent;
        this.componentClass = bipComponent.getClass();

        if (useAnnotationSpec) {
            this.behaviour = parseAnnotations(bipComponent.getClass()).build(this);
        } else {
            this.behaviour = getExecutableBehaviour(bipComponent.getClass()).build(this);
        }

    }

    public Behaviour getBehaviour() {
        return behaviour;
    }

    private BehaviourBuilder getExecutableBehaviour(Class<?> componentClass) throws BIPException {

        Method[] componentMethods = componentClass.getMethods();
        for (Method method : componentMethods) {
            Annotation[] annotations = method.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation instanceof org.javabip.annotations.ExecutableBehaviour) {
                    Class<?> returnType = method.getReturnType();
                    if (!BehaviourBuilder.class.isAssignableFrom(returnType)) {
                        throw new BIPException("Method " + method.getName()
                                + "annotated with @ExecutableBehaviour should have a return type BehaviourBuilder");
                    }
                    try {
                        if (method.getParameterTypes().length != 0) {
                            throw new BIPException("The method " + method.getName()
                                    + " for getting executable behaviour for component "
                                    + bipComponent.getClass().getName() + "must have no arguments.");
                        }
                        return (BehaviourBuilder) method.invoke(bipComponent);
                    } catch (Exception e) {
                        throw new BIPException(
                                "Method annotated with ExecutableBehavior annotation threw exception upon execution", e);
                    }
                }
            }
        }

        throw new BIPException("No annotation ExecutableBehaviour found in class " + componentClass.getCanonicalName());

    }

    private BehaviourBuilder parseAnnotations(Class<?> componentClass) throws BIPException {
        BehaviourBuilder builder = new BehaviourBuilder(bipComponent);
        String specType = "";
        String initialState;

        // get component name and type
        Annotation classAnnotation = componentClass.getAnnotation(ComponentType.class);
        if (classAnnotation != null) {
            ComponentType componentTypeAnnotation = (ComponentType) classAnnotation;
            builder.setComponentType(componentTypeAnnotation.name());
            specType = componentTypeAnnotation.name();
            initialState = componentTypeAnnotation.initial();
            builder.setInitialState(initialState);
        } else {
            throw new BIPException("ComponentType annotation is not specified.");
        }

        // get ports
        classAnnotation = componentClass.getAnnotation(Ports.class);
        if (classAnnotation != null) {
            Ports ports = (Ports) classAnnotation;
            org.javabip.annotations.Port[] portArray = ports.value();
            for (org.javabip.annotations.Port bipPortAnnotation : portArray) {

                if (bipPortAnnotation != null)
                    addPort(bipPortAnnotation, specType, builder);

            }
        } else {
            throw new BIPException("Port information for the BIP component is not specified.");
        }

        //create a list of space predicates annotations, can be empty
        ArrayList<StatePredicate> statePredicatesList = new ArrayList<>();
        StatePredicates statePredicates = componentClass.getAnnotation(StatePredicates.class);
        if (statePredicates != null) {
            statePredicatesList.addAll(Arrays.asList(statePredicates.value()));
        }
        StatePredicate statePredicate = componentClass.getAnnotation(StatePredicate.class);
        if (statePredicate != null) {
            statePredicatesList.add(statePredicate);
        }


        //part related to using vercors parser results
        VerCorsReportParser vp = new VerCorsReportParser();
        ArrayList<ComponentResult> verificationResults = new ArrayList<>();
        try {
            verificationResults.addAll(vp.parseVerCorsResults());
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        boolean usingVRP = false;
        ComponentResult componentResult = null;
        //if verification report is present we check each predicate result and build/not build the corresponding predicate depending if the result is false/true
        if (!verificationResults.isEmpty()) {
            Optional<ComponentResult> componentResultOptional = verificationResults.stream().filter(it -> it.getComponentName().equals(componentClass.getName())).findFirst();
            if (componentResultOptional.isPresent()) {
                usingVRP = true;
                componentResult = componentResultOptional.get();
            }
        }

        //dealing with class invariant and state predicate
        if (usingVRP) {
            ConstructorReport constructorResults = componentResult.getConstructorResults();
            Boolean componentInvariant = constructorResults.getComponentInvariant();
            //see if class invariant needs to be checked
            if (!componentInvariant) {
                parseInvariantAnnotation(componentClass, builder);
            }

            //see if state invariant for the initial state need to be checked
            Boolean statePredicateConstructorProven = constructorResults.getStatePredicate();
            if (!statePredicateConstructorProven) {
                Optional<StatePredicate> statePredicateConstructorOptional = statePredicatesList.stream().filter(e -> e.state().equals(initialState)).findFirst();
                if (statePredicateConstructorOptional.isPresent()) {
                    StatePredicate sp = statePredicateConstructorOptional.get();
                    builder.buildStatePredicate(sp.expr(), sp.state());
                } else {
                    logger.info("VerCors report indicates that there is a state predicate that was not proven for the initial state, but JavaBip was not able to find this predicate");
                }
            }
        } else {
            //if we do not use vercors report, let's build all state predicates
            parseInvariantAnnotation(componentClass, builder);
            statePredicatesList.forEach(sp -> builder.buildStatePredicate(sp.expr(), sp.state()));
        }

        //dealing with methods
        Method[] componentMethods = componentClass.getMethods();
        // get transitions & guards & data
        for (Method method : componentMethods) {
            Annotation[] annotations = method.getAnnotations();
            for (Annotation annotation : annotations) {

                //create a list of transition annotations, can be empty
                ArrayList<Transition> transitionList = new ArrayList<>();
                if (annotation instanceof org.javabip.annotations.Transition) {
                    transitionList.add((Transition) annotation);
                } else if (annotation instanceof Transitions) {
                    Transitions transitionsAnnotation = (Transitions) annotation;
                    transitionList.addAll(Arrays.asList(transitionsAnnotation.value()));
                }

                //process transition(s)
                if (!transitionList.isEmpty()) {
                    for (Transition transition : transitionList) {
                        if (usingVRP) {
                            ArrayList<TransactionReport> transactionsReport = componentResult.getTransactionResults();
                            Optional<TransactionReport> transactionReportOptional = transactionsReport.stream().filter(tr -> tr.equalsTransition(transition)).findFirst();
                            if (transactionReportOptional.isEmpty()) {
                                logger.info("JavaBip was not able to find any data in the parsed vercors report for this transition");
                            } else {
                                TransactionReport transactionReport = transactionReportOptional.get();

                                //check invariant
                                //TODO in fact, state invariant can be checked not only at every step but taking into account if it was proven or not for the transition

                                //check state predicate
                                if (!transactionReport.getStatePredicate()) {
                                    Optional<StatePredicate> statePredicateTransitionOptional = statePredicatesList.stream().filter(st -> st.state().equals(transition.source())).findFirst();
                                    if (statePredicateTransitionOptional.isEmpty()) {
                                        logger.info("JavaBip was not able to parse a state predicate in the JavaBip specification");
                                    } else {
                                        StatePredicate sp = statePredicateTransitionOptional.get();
                                        builder.buildStatePredicate(sp.expr(), sp.state());
                                    }
                                }

                                //check pre and post conditions
                                String requires = transactionReport.getPreCondition() ? "" : transition.requires();
                                String ensures = transactionReport.getPostCondition() ? "" : transition.ensures();
                                builder.addTransitionAndStates(transition.name(), transition.source(),
                                        transition.target(), transition.guard(), requires, ensures, method);
                            }
                        } else {
                            addTransitionAndStates(method, transition, builder);
                        }
                    }
                }

                //process guard
                else if (annotation instanceof org.javabip.annotations.Guard) {
                    addGuard(method, (org.javabip.annotations.Guard) annotation, builder);

                }

                //process data (out)
                else if (annotation instanceof Data) {
                    addData(method, (Data) annotation, builder);
                }

                //process pure
                else if (annotation instanceof Pure) {
                    //throw new UnsupportedOperationException();
                }
            }
        }

        return builder;
    }

    private void parseInvariantAnnotation(Class<?> componentClass, BehaviourBuilder builder) {
        Invariant invariant = componentClass.getAnnotation(Invariant.class);
        if (invariant != null) {
            builder.buildComponentInvariant(invariant.value());
        }
    }

    private void addGuard(Method method, org.javabip.annotations.Guard annotation, BehaviourBuilder builder)
            throws BIPException {

        Class<?> returnType = method.getReturnType();
        if (!Boolean.class.isAssignableFrom(returnType) && !boolean.class.isAssignableFrom(returnType)) {
            throw new BIPException("Guard method " + method.getName() + " should be a boolean function");
        }

        builder.addGuard(annotation.name(), method, ReflectionHelper.parseDataAnnotations(method));

    }

    private void addData(Method method, Data annotation, BehaviourBuilder builder) {

        builder.addDataOut(method, annotation);

    }

    private void addTransitionAndStates(Method method, org.javabip.annotations.Transition transitionAnnotation,
                                        BehaviourBuilder builder) {

        builder.addTransitionAndStates(transitionAnnotation.name(), transitionAnnotation.source(),
                transitionAnnotation.target(), transitionAnnotation.guard(), transitionAnnotation.requires(), transitionAnnotation.ensures(), method);

    }

    private void addPort(org.javabip.annotations.Port portAnnotation, Class<?> componentClass, BehaviourBuilder
            builder) {

        builder.addPort(portAnnotation.name(), portAnnotation.type(), componentClass);

    }

    private void addPort(org.javabip.annotations.Port portAnnotation, String specType, BehaviourBuilder builder) {
        builder.addPort(portAnnotation.name(), portAnnotation.type(), specType);

    }

}
