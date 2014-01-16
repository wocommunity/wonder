package er.extensions.components;

import java.util.Stack;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.appserver.ERXWOContext;

/**
 * ERXDynamicElement provides a common base class for dynamic elements.
 * <p>
 * All subclasses of ERXDynamicElement have to be thread safe! The WOAssociation objects
 * are thread safe as they don't hold a specific value but are used to retrieve the correct
 * value from the current parent component. If you want or have to store values in instance
 * variables use the inner class {@link ContextData} as value holder. Those objects will
 * store values indirectly within the current context which means you can declare them as
 * static.
 * 
 * @author jw
 */
public abstract class ERXDynamicElement extends WODynamicGroup {
	protected Logger log = Logger.getLogger(getClass());
	private final NSDictionary<String, WOAssociation> _associations;

	public ERXDynamicElement(String name, NSDictionary<String, WOAssociation> associations, WOElement template) {
		super(name, associations, template);
		_associations = associations;
	}
	
	public ERXDynamicElement(String name, NSDictionary<String, WOAssociation> associations, NSMutableArray<WOElement> children) {
		super(name, associations, children);
		_associations = associations;
	}

	/**
	 * Returns the element's association dictionary.
	 * 
	 * @return the element's association dictionary
	 */
	public NSDictionary<String, WOAssociation> associations() {
		return _associations;
	}
	
	/**
	 * Return the value of the id binding if it exists or a safe identifier
	 * otherwise.
	 * 
	 * @param context context of the transaction
	 * @return id string for this component
	 */
	public String id(WOContext context) {
		String id = stringValueForBinding("id", context.component());
		if (id == null) {
			id = ERXWOContext.safeIdentifierName(context, false);
		}
		return id;
	}

	/**
	 * Returns the name of this element within the given context. This
	 * corresponds to the elementID.
	 * 
	 * @param context context of the transaction
	 * @return elementID
	 */
	protected String nameInContext(WOContext context) {
		return context.elementID();
	}
	
	/**
	 * Checks if we are in secure mode by checking the secure binding or the
	 * context's secure mode as fallback.
	 * 
	 * @param context context of the transaction
	 * @return <code>true</code> if in secure mode
	 */
	public boolean secureInContext(WOContext context) {
		if (hasBinding("secure")) {
			return booleanValueForBinding("secure", false, context.component());
		}
		return context.secureMode();
	}
	
	/**
	 * Checks if there is an association for a binding with the given name.
	 * 
	 * @param name binding name
	 * @return <code>true</code> if the association exists
	 */
	public boolean hasBinding(String name) {
		return ERXComponentUtilities.hasBinding(name, associations());
	}

	/**
	 * Returns the association for a binding with the given name. If there is
	 * no such association <code>null</code> will be returned.
	 * 
	 * @param name binding name
	 * @return association for given binding or <code>null</code>
	 */
	public WOAssociation bindingNamed(String name) {
		return ERXComponentUtilities.bindingNamed(name, associations());
	}
	
	/**
	 * Checks if the association for a binding with the given name can assign
	 * values at runtime.
	 * 
	 * @param name binding name
	 * @return <code>true</code> if binding is settable
	 */
	public boolean bindingIsSettable(String name) {
		return ERXComponentUtilities.bindingIsSettable(name, associations());
	}

	/**
	 * Will try to set the given binding in the component to the passed value.
	 * 
	 * @param value new value for the binding
	 * @param name binding name
	 * @param component component to set the value in
	 */
	public void setValueForBinding(Object value, String name, WOComponent component) {
		ERXComponentUtilities.setValueForBinding(value, name, associations(), component);
	}

	/**
	 * Retrieves the current value of the given binding from the component. If there
	 * is no such binding or its value evaluates to <code>null</code> the default
	 * value will be returned.
	 * 
	 * @param name binding name
	 * @param defaultValue default value
	 * @param component component to get value from
	 * @return retrieved value or default value
	 */
	public Object valueForBinding(String name, Object defaultValue, WOComponent component) {
		return ERXComponentUtilities.valueForBinding(name, defaultValue, associations(), component);
	}

	/**
	 * Retrieves the current value of the given binding from the component. If there
	 * is no such binding <code>null</code> will be returned.
	 * 
	 * @param name binding name
	 * @param component component to get value from
	 * @return retrieved value or <code>null</code>
	 */
	public Object valueForBinding(String name, WOComponent component) {
		return ERXComponentUtilities.valueForBinding(name, associations(), component);
	}
	
	/**
	 * Retrieves the current string value of the given binding from the component. If there
	 * is no such binding or its value evaluates to <code>null</code> the default
	 * value will be returned.
	 * 
	 * @param name binding name
	 * @param defaultValue default value
	 * @param component component to get value from
	 * @return retrieved string value or default value
	 */
	public String stringValueForBinding(String name, String defaultValue, WOComponent component) {
		return ERXComponentUtilities.stringValueForBinding(name, defaultValue, associations(), component);
	}

	/**
	 * Retrieves the current string value of the given binding from the component. If there
	 * is no such binding <code>null</code> will be returned.
	 * 
	 * @param name binding name
	 * @param component component to get value from
	 * @return retrieved string value or <code>null</code>
	 */
	public String stringValueForBinding(String name, WOComponent component) {
		return ERXComponentUtilities.stringValueForBinding(name, associations(), component);
	}

	/**
	 * Retrieves the current boolean value of the given binding from the component. If there
	 * is no such binding the default value will be returned.
	 * 
	 * @param name binding name
	 * @param defaultValue default value
	 * @param component component to get value from
	 * @return retrieved boolean value or default value
	 */
	public boolean booleanValueForBinding(String name, boolean defaultValue, WOComponent component) {
		return ERXComponentUtilities.booleanValueForBinding(name, defaultValue, associations(), component);
	}
	
	/**
	 * Retrieves the current boolean value of the given binding from the component. If there
	 * is no such binding <code>false</code> will be returned.
	 * 
	 * @param name binding name
	 * @param component component to get value from
	 * @return retrieved boolean value or <code>false</code>
	 */
	public boolean booleanValueForBinding(String name, WOComponent component) {
		return ERXComponentUtilities.booleanValueForBinding(name, associations(), component);
	}

	/**
	 * Retrieves the current int value of the given binding from the component. If there
	 * is no such binding the default value will be returned.
	 * 
	 * @param name binding name
	 * @param defaultValue default value
	 * @param component component to get value from
	 * @return retrieved int value or default value
	 */
	public int integerValueForBinding(String name, int defaultValue, WOComponent component) {
		return ERXComponentUtilities.integerValueForBinding(name, defaultValue, associations(), component);
	}
	
	/**
	 * Resolves a given binding as an NSArray object.
	 * 
	 * @param name binding name
	 * @param component component to get value from
	 * @return retrieved array value or <code>null</code>
	 */
	public <T> NSArray<T> arrayValueForBinding(String name, WOComponent component) {
		return ERXComponentUtilities.arrayValueForBinding(name, associations(), component);
	}

	/**
	 * Resolves a given binding as an NSArray object.
	 * 
	 * @param name binding name
	 * @param defaultValue default value
	 * @param component component to get value from
	 * @return retrieved array value or default value
	 */
	public <T> NSArray<T> arrayValueForBinding(String name, NSArray<T> defaultValue, WOComponent component) {
		return ERXComponentUtilities.arrayValueForBinding(name, defaultValue, associations(), component);
	}
	
	/**
	 * Appends the attribute to the response. If the value is <code>null</code>
	 * the appending is skipped.
	 * 
	 * @param response the current response
	 * @param name the attribute name
	 * @param value the attribute value
	 */
	protected void appendTagAttributeToResponse(WOResponse response, String name, Object value) {
		if (value != null) {
			response._appendTagAttributeAndValue(name, value.toString(), true);
		}
	}
	
	/**
	 * Convenience method to override if you want to store values in ContextData object. Here you can call
	 * the {@link ContextData#begin(WOContext, Object)} on your ContextData objects. This method should then
	 * be called at the beginning of the {@link #appendToResponse(WOResponse, WOContext)},
	 * {@link #takeValuesFromRequest(WORequest, WOContext)} and {@link #invokeAction(WORequest, WOContext)}.
	 * 
	 * @param context context of the transaction
	 */
	protected void beforeProcessing(WOContext context) {
	}

	/**
	 * Convenience method to override if you want to store values in ContextData object. Here you can call
	 * the {@link ContextData#end(WOContext)} on your ContextData objects. This method should then
	 * be called at the end of the {@link #appendToResponse(WOResponse, WOContext)},
	 * {@link #takeValuesFromRequest(WORequest, WOContext)} and {@link #invokeAction(WORequest, WOContext)}.
	 * 
	 * @param context context of the transaction
	 */
	protected void afterProcessing(WOContext context) {
	}
	
	/**
	 * This class is used to store values that cannot be stored in instance variables as
	 * dynamic elements have to be thread-safe. Internally the userInfo of the current context is used
	 * to store the values, so all information is only valid for one request-response cycle. A stack
	 * is used per key so multiple nested components of the same type can use the same key safely.
	 * <p>
	 * You have to use <code>begin</code> before first usage of the field and <code>end</code> at
	 * the end of the cycle to clean up the corresponding stack. Without balanced calls of
	 * <code>begin</code> and <code>end</code> very bad things will happen. Thus it is advisable
	 * to use a try-finally (see example below). As you have to make these calls during <code>appendToResponse</code>,
	 * <code>taleValuesFromRequest</code> and <code>invokeAction</code> you should put those calls into
	 * overridden methods of <code>beforeProcessing</code> and <code>afterProcessing</code> respectively
	 * to avoid code duplication.
	 * <p>
	 * It is advisable too to choose a unique <code>key</code>, as different component types
	 * will use the same userInfo. It is suggested to use a key composed by the component
	 * and field name (e.g. <i>MyDynamicElement.myField</i>).
	 * The ContextData field itself can and should be declared static as it doesn't store the
	 * value itself, it is only used as an accessor to the value stored within the context.
	 * <p>
	 * Example:
	 * <pre>
	 * public static final ContextData<String> myField = new ContextData<String>("MyClass.myField");
	 * 
	 * public void appendToResponse(WOResponse response, WOContext context) {
	 *   beforeProcessing(context);
	 *   try {
	 *     response.appendContentString("Current value: " + myField.value(context));
	 *   } finally {
	 *     afterProcessing(context);
	 *   }
	 * }
	 * 
	 * public void takeValuesFromRequest(WORequest request, WOContext context) {
	 *   beforeProcessing(context);
	 *   try {
	 *     …
	 *   } finally {
	 *     afterProcessing(context);
	 *   }
	 * }
	 * 
	 * public WOActionResults invokeAction(WORequest request, WOContext context) {
	 *   beforeProcessing(context);
	 *   try {
	 *     …
	 *   } finally {
	 *     afterProcessing(context);
	 *   }
	 * }
	 * 
	 * protected void beforeProcessing(WOContext context) {
	 *   myField.begin(context, "myValue");
	 * }
	 * 
	 * protected void afterProcessing(WOContext context) {
	 *   myField.end(context);
	 * }
	 * </pre>
	 * 
	 * @param <T> type of data to store 
	 * @author sgaertner
	 */
	protected static class ContextData<T> {
		private final String _key;

		public ContextData(String key) {
			_key = key;
		}

		/**
		 * The key this object uses to store values in the context's
		 * userInfo.
		 * 
		 * @return the key
		 */
		public String key() {
			return _key;
		}

		protected Stack<T> stack(WOContext context) {
			Stack<T> stack = (Stack<T>) context.userInfoForKey(_key);
			if (stack == null) {
				stack = new Stack<T>();
				context.setUserInfoForKey(stack, _key);
			}
			return stack;
		}

		/**
		 * Push a constant value onto the stack.
		 * 
		 * @param context context of the transaction
		 * @param value constant value
		 */
		public void begin(WOContext context, T value) {
			stack(context).push(value);
		}

		/**
		 * Push a value from a specific binding onto the stack.
		 * 
		 * @param context context of the transaction
		 * @param component the current dynamic element
		 * @param name binding name
		 */
		public void begin(WOContext context, ERXDynamicElement component, String name) {
			stack(context).push((T) component.valueForBinding(name, context.component()));
		}

		/**
		 * Replace the current value on the stack with a new value.
		 * 
		 * @param context context of the transaction
		 * @param value constant value
		 */
		public void setValue(WOContext context, T value) {
			stack(context).pop();
			stack(context).push(value);
		}

		/**
		 * Returns the current value.
		 * 
		 * @param context context of the transaction
		 * @return current value
		 */
		public T value(WOContext context) {
			return stack(context).peek();
		}

		/**
		 * Removes the current value from the stack. If you pushed multiple values onto
		 * the stack by calling begin() several times you have to call end() the same
		 * number of times.
		 *  
		 * @param context context of the transaction
		 */
		public void end(WOContext context) {
			stack(context).pop();
		}

		@Override
		public String toString() {
			return new StringBuilder().append('<').append(getClass().getName()).append(" key: ").append(_key).append('>')
					.toString();
		}
	}
}
