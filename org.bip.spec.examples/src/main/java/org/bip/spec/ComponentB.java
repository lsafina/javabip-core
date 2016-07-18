package org.bip.spec;

import org.bip.annotations.ComponentType;
import org.bip.annotations.Data;
import org.bip.annotations.Port;
import org.bip.annotations.Ports;
import org.bip.annotations.Transition;
import org.bip.api.PortType;
import org.bip.api.DataOut.AccessType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Ports({ @Port(name = "a", type = PortType.enforceable), 
	     @Port(name = "b", type = PortType.enforceable)})
@ComponentType(initial = "zero", name = "org.bip.spec.ComponentB")
public class ComponentB {
	Logger logger = LoggerFactory.getLogger(ComponentB.class);

	private int memoryY = 200;
	
	public int counterA = 0;
	public int counterB = 0;

	public int noOfTransitions;

	@Transition(name = "a", source = "zero", target = "zero")
	public void componentBTransitionA() {
		logger.debug("Transition a of ComponentB has been performed");
		counterA++;
		noOfTransitions++;
	}

	@Transition(name = "b", source = "zero", target = "zero")
	public void componentBTransitionB() {
		logger.debug("Transition b of ComponentB has been performed");
		counterB++;
		noOfTransitions++;
	}

	@Data(name = "memoryY", accessTypePort = AccessType.allowed, ports = {"a"})
	public int memoryY() {
		return memoryY;
	}
}