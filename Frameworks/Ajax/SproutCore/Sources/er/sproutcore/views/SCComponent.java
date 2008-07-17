package er.sproutcore.views;

import java.util.Enumeration;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.ajax.AjaxOption;
import er.extensions.components.ERXNonSynchronizingComponent;
import er.extensions.foundation.ERXStringUtilities;
import er.sproutcore.SCItem;

/**
 * Superclass for your own components that are actual views, so you don't have
 * to write dynamic elements.
 * 
 * @author ak
 * 
 */
public class SCComponent extends ERXNonSynchronizingComponent implements ISCView {
	private NSMutableDictionary<String, SCProperty> _properties;
	private NSMutableDictionary<String, SCBinding> _bindings;
	private String _className;

	public SCComponent(WOContext context) {
		super(context);
		setClassName(SCView.defaultClassName(getClass()));
	}

	protected void addProperties() {
		_properties = new NSMutableDictionary<String, SCProperty>();
		_bindings = new NSMutableDictionary<String, SCBinding>();
		SCView.addDefaultProperties(this);
	}

	public String containerID() {
		SCItem item = SCItem.currentItem();
		return (item.isRoot()) ? item.id() : null;
	}

	public String nextID() {
		return "id_" + SCItem.nextId();
	}

	public String id() {
		return stringValueForBinding("id");
	}

	public String outlet() {
		return stringValueForBinding("outlet", id());
	}

	public boolean root() {
		return booleanValueForBinding("root", false);
	}

	public boolean hasProperty(String string) {
		return _properties.containsKey(string);
	}

	protected void addBinding(String bindingName) {
		addBinding(bindingName, bindingName);
	}

	protected void addBinding(String associationName, String bindingName) {
		addBinding(new SCBinding(bindingName, _associationWithName("?" + associationName)));
	}

	protected void addBinding(SCBinding binding) {
		_bindings.setObjectForKey(binding, binding.name());
	}

	public void addProperty(String propertyName) {
		addProperty(propertyName, propertyName, null, AjaxOption.DEFAULT, true);
	}

	public NSDictionary<Object, Object> animate() {
		return null;
	}

	public String paneDef() {
		return null;
	}

	public void addPropertyWithDefault(String propertyName, Object defaultValue) {
		addProperty(propertyName, propertyName, defaultValue, AjaxOption.DEFAULT, true);
	}

	public void addPropertyWithDefault(String associationName, String propertyName, Object defaultValue) {
		addProperty(associationName, propertyName, defaultValue, AjaxOption.DEFAULT, true);
	}

	public void addProperty(String associationName, String propertyName) {
		addProperty(associationName, propertyName, null, AjaxOption.DEFAULT, true);
	}

	public void addProperty(String associationName, String propertyName, boolean skipIfNull) {
		addProperty(associationName, propertyName, null, AjaxOption.DEFAULT, skipIfNull);
	}

	public void addProperty(String propertyName, AjaxOption.Type type) {
		addProperty(propertyName, propertyName, null, type, true);
	}

	public void addProperty(String associationName, String propertyName, AjaxOption.Type type) {
		addProperty(associationName, propertyName, null, type, true);
	}

	public void addProperty(String associationName, String propertyName, Object defaultValue, AjaxOption.Type type, boolean skipIfNull) {
		addProperty(new SCProperty(propertyName, _associationWithName(ERXStringUtilities.underscoreToCamelCase(associationName, false)), defaultValue, type, skipIfNull));
	}

	public void addProperty(SCProperty property) {
		_properties.setObjectForKey(property, property.name());
	}

	public SCProperty propertyNamed(String propertyName) {
		return _properties.objectForKey(propertyName);
	}

	protected NSDictionary<String, SCProperty> properties() {
		return _properties;
	}

	public String containerClass() {
		StringBuffer css = new StringBuffer();
		if (!booleanValueForBinding("enabled", true)) {
			css.append("disabled");
		}
		// css.append(" ");
		// css.append(SCUtilities.defaultCssName(getClass()));
		css.append(" ");
		css.append(containerID());
		return css.toString();
	}
	
	@Override
	public void pullValuesFromParent() {
		super.pullValuesFromParent();
	}

	@Override
	public final void appendToResponse(WOResponse response, WOContext context) {
		SCItem item = SCItem.pushItem(id(), className(), outlet(), root());
		addProperties();

		for (SCBinding binding : _bindings.allValues()) {
			Object value = binding.association().valueInComponent(this);
			item.addBinding(binding, value);
		}

		for (Enumeration e = bindingKeys().objectEnumerator(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			WOAssociation association = _associationWithName(key);
			if (key.startsWith("?")) {
				SCBinding binding = new SCBinding(key.substring(1), association);
				item.addBinding(binding, binding.association().valueInComponent(this));
			}
		}

		for (SCProperty property : _properties.allValues()) {
			Object value = property.association().valueInComponent(this);
			item.addProperty(property, value);
		}

		String id = containerID();
		String elementName = elementName();
		response._appendContentAsciiString("<" + elementName);

		if (id != null) {
			response._appendContentAsciiString(" id=\"" + containerID() + "\"");
		}
		String containerClass = containerClass();
		if (containerClass != null) {
			response._appendContentAsciiString(" class=\"" + containerClass + "\"");
		}

		String style = style();
		if (style != null) {
			response._appendContentAsciiString(" style=\"" + style + "\"");
		}

		response._appendContentAsciiString(">");

		doAppendToResponse(response, context);
		SCItem.popItem();
		response._appendContentAsciiString("</" + elementName + ">");
	}

	protected void doAppendToResponse(WOResponse response, WOContext context) {
		super.appendToResponse(response, context);
	}

	protected String elementName() {
		return "div";
	}

	protected String style() {
		return stringValueForBinding("style");
	}

	/**
	 * Returns a nice class name like MyApp.MyView.
	 * 
	 * @return
	 */
	protected String className() {
		return _className;
	}

	protected void setClassName(String name) {
		_className = name;
	}
}
