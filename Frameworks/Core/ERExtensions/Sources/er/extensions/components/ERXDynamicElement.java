package er.extensions.components;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.appserver.ERXWOContext;

/**
 * ERXDynamicElement provides a common base class for dynamic elements.
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
}
