package er.sproutcore.views;

import com.webobjects.foundation.NSDictionary;

import er.ajax.AjaxOption;

public interface ISCView {
	public NSDictionary<Object, Object> animate();

	public String paneDef();

	public void addProperty(String propertyName);

	public void addPropertyWithDefault(String propertyName, Object defaultValue);

	public void addPropertyWithDefault(String associationName, String propertyName, Object defaultValue);

	public void addProperty(String associationName, String propertyName);

	public void addProperty(String associationName, String propertyName, boolean skipIfNull);

	public void addProperty(String propertyName, AjaxOption.Type type);

	public void addProperty(String associationName, String propertyName, AjaxOption.Type type);

	public void addProperty(String associationName, String propertyName, Object defaultValue, AjaxOption.Type type, boolean skipIfNull);

	public void addProperty(SCProperty property);
}
