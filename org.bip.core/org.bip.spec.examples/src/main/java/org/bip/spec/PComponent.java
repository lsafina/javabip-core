/*
 * Copyright (c) 2012 Crossing-Tech TM Switzerland. All right reserved.
 * Copyright (c) 2012, RiSD Laboratory, EPFL, Switzerland.
 *
 * Author: Simon Bliudze, Alina Zolotukhina, Anastasia Mavridou, and Radoslaw Szymanek
 * Date: 10/15/12
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
