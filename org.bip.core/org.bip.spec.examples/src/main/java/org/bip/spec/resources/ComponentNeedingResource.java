package org.bip.spec.resources;

import org.bip.annotations.ComponentType;
import org.bip.annotations.Port;
import org.bip.annotations.Ports;
import org.bip.annotations.ResourceUtility;
import org.bip.annotations.ResourcesRequired;
import org.bip.annotations.Transition;
import org.bip.annotations.ResourceRequired;
import org.bip.api.PortType;
import org.bip.api.ResourceType;

@Ports({ @Port(name = "a", type = PortType.enforceable), @Port(name = "b", type = PortType.spontaneous) })
@ComponentType(initial = "0", name = "org.bip.spec.aComponent")
public class ComponentNeedingResource {

	@Transition(name = "a", source = "0", target = "0", guard = "")
	@ResourcesRequired({ @ResourceRequired(label = "p1", type = ResourceType.processor, utility = "pFunc"),
			@ResourceRequired(label = "m1", type = ResourceType.memory, utility = "mFunc") })
	@ResourceUtility(utility="p1=1 & m1=128")
	public void aTransition() {
		System.out.println("Doing transition requiring resources");
	}

}