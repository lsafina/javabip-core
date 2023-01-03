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
 * Date: 15.10.12
 */

package org.javabip.api;

import java.util.Map;
import java.util.Set;

/**
 * The Interface BIPEngine.
 */
public interface BIPEngine {

	/**
	 * It specifies the BIP glue to be enforced by BIP engine when choosing interactions.
	 * 
	 * @param glue
	 *            the BIP glue.
	 */
	void specifyGlue(BIPGlue glue);

	/**
	 * It registers a given component with its behavior within the engine to be managed by the engine.
	 * 
	 * @param component
	 *            the component to be managed by the engine.
	 * @param id
	 *            the id of the component.
	 * @param useAnnotations
	 *            a boolean indicating whether the component state machine is specified with annotations or not.
	 * @return the BIP Actor wrapper of the component generated by the engine.
	 */
	BIPActor register(Object component, String id, boolean useAnnotations);

	/**
	 * The same as the base register but with checking properties in the runtime
	 *
	 * @param component
	 *            the component to be managed by the engine.
	 * @param id
	 *            the id of the component.
	 * @param useAnnotations
	 *            a boolean indicating whether the component state machine is specified with annotations or not.
	 * @param doRuntimeVerification
	 * 			  a boolean indicating whether the invariants of the component are being checked during the runtime or not.
	 * @return the BIP Actor wrapper of the component generated by the engine.
	 */
	//BIPActor register(Object component, String id, boolean useAnnotations, boolean doRuntimeVerification, boolean useVerCorsReport);

	/**
	 * It informs the BIP engine about the state of the BIP component. This function only specifies ports which are
	 * disabled regardless of data transfers. More ports can be disabled because of data transfers, but this information
	 * must be obtained by querying the BIPexecutor with the help of checkEnabledness method.
	 * 
	 * @param component
	 *            the component for which the information about the state is provided.
	 * @param currentState
	 *            the current state of the component for which the information is provided.
	 * @param disabledPorts
	 *            the disabled ports of the component for which the information is provided. Empty if no permanently
	 *            disabled ports.
	 */
	void inform(BIPComponent component, String currentState, Set<Port> disabledPorts);

	/**
	 * InformSpecific is called multiple times for each component instance. It allows for the deciding component to
	 * specify for each deciding port the disabled combinations for each potential collaborator (another component
	 * providing data) of deciding component.
	 * 
	 * @param decidingComponent
	 *            the component which decides if it can collaborate with other components based on the guards of its
	 *            ports.
	 * @param decidingPort
	 *            the deciding port whose guard checks the possibility of the collaboration.
	 * @param disabledCombinations
	 *            the disabled combinations of components and their ports which are not accepted for collaboration.
	 */
	void informSpecific(BIPComponent decidingComponent, Port decidingPort,
			Map<BIPComponent, Set<Port>> disabledCombinations);

	/**
	 * informInternal is served for notifying the engine on the internal transition happened on the component
	 * needed for the monitoring
	 * @param decidingComponent
	 * @param currentState
	 */
	void informInteral(BIPComponent decidingComponent, String currentState);

	/**
	 * informSpontaneous is served for notifying the engine on the spontaneous transition happened on the component
	 * needed for the monitoring
	 * @param decidingComponent
	 * @param currentState
	 */
	void informSpontaneous(BIPComponent decidingComponent, String currentState);

	/**
	 * It starts the BIP engine thread.
	 */
	void start();

	/**
	 * It stops the BIP engine thread.
	 */
	void stop();

	/**
	 * It starts the execution of BIP engine cycles for the registered BIP components.
	 */
	void execute();

	/**
	 * It initializes the BIP engine.
	 */
	void initialize();
}
