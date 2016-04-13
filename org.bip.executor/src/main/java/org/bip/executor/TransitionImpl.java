/*
 * Copyright (c) 2012 Crossing-Tech TM Switzerland. All right reserved.
 * Copyright (c) 2012, RiSD Laboratory, EPFL, Switzerland.
 *
 * Author: Simon Bliudze, Alina Zolotukhina, Anastasia Mavridou, and Radoslaw Szymanek
 * Date: 01/27/14
 */

package org.bip.executor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

import org.bip.api.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TransitionImpl {
	
	protected String name;
	protected String source;
	protected String target;
	// Empty string represents that there is no guard associated to this transition.
	protected String guard;
	protected Method method;
	protected MethodHandle methodHandle;
	protected Iterable<Data<?>> dataRequired;
	
	private Logger logger = LoggerFactory.getLogger(TransitionImpl.class);

	/**
	 * Constructor to be used within a BIP Spec
	 * 
	 * @param name name of the transition.
	 * @param source source state of the transition.
	 * @param target target state of the transition.
	 * @param guard the guard for the transition that must evaluate to true for the transition to be enabled.
	 * @param method the method that is executed in order to perform the transition.
	 * @param dataRequired a list of data items that are required by the transition, parameters in the method signature.
	 */
	public TransitionImpl(String name, String source, String target, String guard, 
						  Method method, Iterable<Data<?>> dataRequired) {
		if (guard == null) guard = "";
		this.name = name;
		this.source = source;
		this.target = target;
		this.guard = guard;
		this.method = method;
		this.methodHandle = getMethodHandleForTransition();
		this.dataRequired = dataRequired;
	}
	
	
	public TransitionImpl(TransitionImpl transition) {
		this(transition.name, transition.source, transition.target, 
			 transition.guard, transition.method, transition.dataRequired);
	}

	public String name() {
		return this.name;
	}

	public String source() {
		return this.source;
	}

	public String target() {
		return this.target;
	}
		
	private MethodHandle getMethodHandleForTransition() {
		MethodType methodType;
		MethodHandle methodHandle = null;
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());// clazz - type of data being returned, method has no arguments
		try {
			methodHandle = lookup.findVirtual(method.getDeclaringClass(), method.getName(), methodType);
		} catch (NoSuchMethodException e) {
			ExceptionHelper.printExceptionTrace(logger, e);
		} catch (IllegalAccessException e) {
			ExceptionHelper.printExceptionTrace(logger, e);
		}
		return methodHandle;
	}
}