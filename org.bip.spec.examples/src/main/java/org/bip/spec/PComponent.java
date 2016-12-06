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

package org.bip.spec;

import org.bip.annotations.ComponentType;
import org.bip.annotations.Guard;
import org.bip.annotations.Port;
import org.bip.annotations.Ports;
import org.bip.annotations.Transition;
import org.bip.api.PortType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Ports({ @Port(name = "s", type = PortType.spontaneous), @Port(name = "p", type = PortType.enforceable) })
@ComponentType(initial = "start", name = "org.bip.spec.PComponent")
public class PComponent {

	Logger logger = LoggerFactory.getLogger(PComponent.class);

	public int pCounter = 0;

	boolean pEnabled = false;

	boolean needExternalEnable;

	public PComponent(boolean needExternalEnable) {

		this.needExternalEnable = needExternalEnable;
	}

	@Transition(name = "p", source = "start", target = "start", guard = "isPEnabled")
	public void enforceableP() {
		logger.debug("P transition is being executed.");
		pCounter++;
		pEnabled = false;
	}

	@Guard(name = "isPEnabled")
	public boolean isPEnabled() {
		return !needExternalEnable || pEnabled;
	}

	@Transition(name = "s", source = "start", target = "start", guard = "!isPEnabled")
	public void enableP() {
		pEnabled = true;
	}

}
