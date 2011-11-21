package com.webobjects.eodistribution.common;

import java.util.Arrays;

public class ERDistributionUtils {

	private ERDistributionUtils() {}
	
	public static String invocationToString(_EOServerInvocation invocation) {
		String targetStr = (invocation._pathToTarget == null) ? "" : "target=" + invocation._pathToTarget + ".";
		return 
			targetStr + 
			invocation._method + 
			", arguments=" + Arrays.toString(invocation._arguments);
	}
	
	public static String target(_EOServerInvocation invocation) {
		return invocation._pathToTarget;
	}
	
	public static String method(_EOServerInvocation invocation) {
		return invocation._method;
	}
	
	public static Object[] arguments(_EOServerInvocation invocation) {
		return invocation._arguments;
	}
	
	/**
	 * See EODistributionContext._throwOptimisticLockingFailureForGlobalIDIfNecessary
	 * @return true if the error message indicates that an EO is locked by EODistributionContext on the server because it was changed by another session.
	 */
	public static boolean isTemporaryLockingFailure(Throwable e) {
		return e.getMessage() != null && e.getMessage().contains("Optimistic locking failure") && e.getMessage().contains("has been changed by another client");
	}
}
