/*
 * Copyright (c) 2012 Crossing-Tech TM Switzerland. All right reserved.
 * Copyright (c) 2012, RiSD Laboratory, EPFL, Switzerland.
 *
 * Author: Simon Bliudze, Alina Zolotukhina, Anastasia Mavridou, and Radoslaw Szymanek
 * Date: 10/15/12
 */

package org.javabip.executor;

import org.javabip.api.ComponentProvider;
import org.javabip.api.OrchestratedExecutor;
import org.javabip.api.Port;
import org.javabip.exceptions.BIPException;

import java.util.Map;
import java.util.Set;

/**
 * The Kernel Executor which performs the execution of the corresponding BIP Component via its Behaviour. It is not a
 * multi-thread safe executor kernel therefore it should never be directly used. It needs to be proxied to protect it
 * from multi-thread access by for example Akka actor approach.
 * 
 * At each execution cycle, the executor checks for the enabled internal, spontaneous and enforceable transition and
 * then either performs a transition or notifies the engine of the disabled ports.
 * 
 * @author Alina Zolotukhina
 * 
 */
public class ExecutorKernelRV extends ExecutorKernel implements OrchestratedExecutor, ComponentProvider {
	/**
	 * Creates a new executor instance.
	 *
	 * @param bipComponent the BIP Component to which the executor corresponds
	 * @param id           the executor id
	 * @param useSpec      true, if the annotations are to be used, false otherwise
	 * @throws BIPException
	 */
	public ExecutorKernelRV(Object bipComponent, String id, boolean useSpec) throws BIPException {
		super(bipComponent, useSpec, true);
		this.id = id;
	}

	/**
	 * 
	 * Defines one cycle step of the executor. If no engine is registered it will exit immediately.
	 * 
	 * @return true if the next step can be immediately executed, false if a spontaneous event must happen to have
	 *         reason to execute next step again.
	 * @throws BIPException
	 */
	@Override
	public void step() throws BIPException {
		// if the actor was deregistered then it no longer does any steps.
		if (!registered)
			return;

		dataEvaluation.clear();

		guardToValue = behaviour.computeGuardsWithoutData(behaviour.getCurrentState());

		//check invariant before a transition
		logger.debug("Invariant check at the beginning of each step {}", id);
		((BehaviourImplRV) behaviour).checkInvariant();


		// we have to compute this in order to be able to raise an exception
		boolean existInternalTransition = behaviour.existEnabledInternal(guardToValue);

		if (existInternalTransition) {
			logger.debug("About to execute internal transition for component {}", id);

			behaviour.executeInternal(guardToValue);
			logger.debug("Issuing next step message for component {}", id);

			//informing engine
			engine.informInteral(proxy, behaviour.getCurrentState());

			// Scheduling the next execution step.
			proxy.step();
			logger.debug("Finishing current step that has executed an internal transition for component {}", id);

			//check invariant after transition
			logger.debug("Invariant check after an internal transition {}", id);
			((BehaviourImplRV) behaviour).checkInvariant();

			return;
		}

		boolean existSpontaneousTransition = behaviour.existInCurrentStateAndEnabledSpontaneous(guardToValue);

		if (existSpontaneousTransition && !notifiers.isEmpty()) {

			for (int i = 0; i < notifiers.size(); i++) {

				String port = notifiers.get(i);

				if (behaviour.hasEnabledTransitionFromCurrentState(port, guardToValue)) {
					logger.debug("About to execute spontaneous transition {} for component {}", port, id);

					notifiers.remove(i);
					Map<String, ?> data = notifiersData.remove(i);

					// Both notifiers and notifiersData should be LinkedList to perform efficient removal from the
					// middle.
					if (data == null) {
						behaviour.executePort(port);
					} else {
						behaviour.execute(port, data);
					}

					//informing engine
					engine.informSpontaneous(proxy, behaviour.getCurrentState());
					logger.debug("Issuing next step message for component {}", id);

					// Scheduling the next execution step.
					proxy.step();
					logger.debug("Finishing current step that has executed a spontaneous transition for component {}",
							id);

					//check invariant after transition
					System.out.println("Invariant check after a spontaneous transition.");
					((BehaviourImplRV) behaviour).checkInvariant();

					return;
				}
			}

		}

		boolean existEnforceableTransition = behaviour
				.existInCurrentStateAndEnabledEnforceableWithoutData(guardToValue)
				|| behaviour.existInCurrentStateAndEnforceableWithData();

		Set<Port> globallyDisabledPorts = behaviour
				.getGloballyDisabledEnforceablePortsWithoutDataTransfer(guardToValue);

		if (existEnforceableTransition) {
			logger.debug("About to execute engine inform for component {}", id);
			engine.inform(proxy, behaviour.getCurrentState(), globallyDisabledPorts);
			// Next step will be invoked upon finishing treatment of the message execute.

			//TODO not sure it is a good place, since the transition might be still not executed
			//check invariant after transition
			//System.out.println("Invariant check after an enforceable transition.");
			//checkInvariant();

			return;
		}

		/*
		 * existSpontaneous transition exists but spontaneous event has not happened yet, thus a follow step should be
		 * postponed until any spontaneous event is received. 
		 * TODO: Tell the engine that I am waiting (send all disabled ports in the inform)
		 */
		if (existSpontaneousTransition) {
			logger.debug("Finishing current step for component {} doing nothing due no spontaneous events.", id);
			/*
			 * TODO for Natassa: Change the design of the engine to not continuously expect to be informed by all
			 * components even though there are no spontaneous transitions. So, if the component sends once that there
			 * are no transitions store this information and use it for the next cycles till you get something
			 * different. When this is done uncomment the next line.
			 */
			waitingForSpontaneous = true;
			// engine.inform(proxy, behaviour.getCurrentState(), globallyDisabledPorts);
			// Next step will be invoked upon receiving a spontaneous event.
			return;
		}

		// throw new BIPException("No transition of known type from state "
		// + behaviour.getCurrentState() + " in component "
		// + this.getId());

	}
}
