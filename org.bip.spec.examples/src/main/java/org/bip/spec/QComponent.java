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

@Ports({ @Port(name = "s", type = PortType.spontaneous), @Port(name = "q", type = PortType.enforceable) })
@ComponentType(initial = "start", name = "org.bip.spec.QComponent")
public class QComponent {

    Logger logger = LoggerFactory.getLogger(QComponent.class);

    public int qCounter = 0;

    boolean qEnabled = false;

    @Transition(name = "q", source = "start", target = "start", guard = "isQEnabled")
    public void enforceableQ() {
        logger.debug("Q transition is being executed.");
        qCounter++;
        qEnabled = false;
    }

    @Guard(name = "isQEnabled")
    public boolean isQEnabled() {
        return qEnabled;
    }

    @Transition(name = "s", source = "start", target = "start", guard = "!isQEnabled")
    public void enableQ() {
        qEnabled = true;
    }

}