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
import org.bip.annotations.Data;
import org.bip.annotations.Port;
import org.bip.annotations.Ports;
import org.bip.annotations.Transition;
import org.bip.api.DataOut.AccessType;
import org.bip.api.PortType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Ports({ @Port(name = "log", type = PortType.enforceable), @Port(name = "broadcast", type = PortType.enforceable) })
@ComponentType(initial = "zero", name = "org.bip.spec.Tracker")
public class Tracker {

	public int noOfTransitions = 0;

	Logger logger = LoggerFactory.getLogger(Tracker.class);
	private int trackerId;

	public Tracker(int id) {
		trackerId = id;
	}

	@Transition(name = "log", source = "zero", target = "zero")
	public void logging() {
		// System.out.println("Peer has updates his status");
		noOfTransitions++;
	}

	@Transition(name = "broadcast", source = "zero", target = "zero")
	public void broadcasting() {
		noOfTransitions++;
		// System.out.println("Broadcasting " + trackerId);
	}

	@Data(name = "trackerId", accessTypePort = AccessType.any)
	public int trackerId() {
		return trackerId;
	}

}
