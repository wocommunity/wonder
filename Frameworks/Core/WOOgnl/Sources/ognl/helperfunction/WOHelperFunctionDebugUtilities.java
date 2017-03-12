package ognl.helperfunction;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;

/**
 * Utilities for binding debug support.
 * 
 * @author mschrag
 */
public class WOHelperFunctionDebugUtilities {
	private static final Logger log = LoggerFactory.getLogger(WOHelperFunctionDebugUtilities.class);
	private static boolean _resolvedMethods;
	private static Method _debugEnabledForComponentMethod;

	protected static void resolveMethods() {
		if (!_resolvedMethods) {
			_resolvedMethods = true;
			try {
				_debugEnabledForComponentMethod = WOApplication.application().getClass().getMethod("debugEnabledForComponent", String.class);
			}
			catch (Throwable e) {
				log.error("Binding debugging is not available because your application does not implement debugEnabledForComponent(WOComponent).", e);
			}
		}
	}

	/**
	 * Returns whether or not debug is enabled for the given component.
	 * 
	 * @param component the component to check
	 * @return whether or not debug is enabled for the given component
	 */
	public static boolean debugEnabledForComponent(WOComponent component) {
		try {
			WOHelperFunctionDebugUtilities.resolveMethods();
			Boolean debugEnabled = Boolean.FALSE;
			if (_debugEnabledForComponentMethod != null) {
				debugEnabled = (Boolean) _debugEnabledForComponentMethod.invoke(WOApplication.application(), component.name());
			}
			return debugEnabled.booleanValue();
		}
		catch (Exception e) {
			log.warn("Binding debugging is not available because debugEnabledForComponent(WOComponent) failed.", e);
			return false;
		}
	}

	/**
	 * Sets the debug flag on WOAssociations for the component based on whether or not debug is enabled.
	 * 
	 * @param association the association
	 * @param component the component
	 */
	public static void setDebugEnabled(WOAssociation association, WOComponent component) {
		if (WOHelperFunctionParser._debugSupport) {
			boolean debugEnabled = WOHelperFunctionDebugUtilities.debugEnabledForComponent(component) || WOHelperFunctionDebugUtilities.debugEnabledForComponent(component.context().component());
			association._setDebuggingEnabled(debugEnabled);
		}
	}
}
