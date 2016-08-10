/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

/**
 * Abstract non-synchronizing component used as the super class for a number of
 * components within the ER frameworks. Adds a number of nice binding resolution
 * methods.
 */
public abstract class ERXNonSynchronizingComponent extends ERXComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public ERXNonSynchronizingComponent(WOContext context) {
		super(context);
	}

	/**
	 * Component does not synchronize variables.
	 * 
	 * @return <code>false</code>
	 */
	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	/**
	 * Calls {@link #resetCachedBindingsInStatefulComponent} prior to super.takeValuesFromRequest.
	 * @param request from which the values will be taken
	 * @param context of the request
	 */
	@Override
	public void takeValuesFromRequest(WORequest request, WOContext context) {
		if (!synchronizesVariablesWithBindings() && !isStateless()) {
			resetCachedBindingsInStatefulComponent();
		}
		super.takeValuesFromRequest(request, context);
	}

	/**
	 * Calls {@link #resetCachedBindingsInStatefulComponent} prior to super.invokeAction.
	 * @param request for which the action is invoked
	 * @param context of the request
	 * @return the result of invoking the action
	 */
	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		if (!synchronizesVariablesWithBindings() && !isStateless()) {
			resetCachedBindingsInStatefulComponent();
		}
		return super.invokeAction(request, context);
	}

	/**
	 * Calls {@link #resetCachedBindingsInStatefulComponent} prior to super.appendToResponse.
	 * @param response to which we are appending
	 * @param context context of the response
	 */
	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		if (!synchronizesVariablesWithBindings() && !isStateless()) {
			resetCachedBindingsInStatefulComponent();
		}
		super.appendToResponse(response, context);
	}

	/**
	 * Implements a {@link WOComponent#reset() reset-like} hook for stateful, but non-synchronizing 
	 * components.  This method is called at the beginning of takeValuesFromRequest, invokeAction 
	 * and appendToResponse if the component subclass is non-synchronized but stateful.  If it is
	 * non-synchronized, but stateless, use {@link WOComponent#reset()}.
	 */
	public void resetCachedBindingsInStatefulComponent() {
		if (_dynamicBindings != null) {
			_dynamicBindings.removeAllObjects();
		}
	}

}
