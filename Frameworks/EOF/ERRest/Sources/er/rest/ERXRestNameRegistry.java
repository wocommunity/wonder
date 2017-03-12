package er.rest;

import java.util.HashMap;
import java.util.Map;

import er.extensions.localization.ERXLocalizer;

/**
 * ERXNameRegistry provides a registry to remap the names of entities and keys to another name. ERXRestFormat applies
 * these renames at parse and format time so that your code doesn't have to deal with it. The default routes also apply
 * these naming rules, though the route variable that you use will be the internal name, not the external name.
 * 
 * @author mschrag
 */
public class ERXRestNameRegistry {
	private static ERXRestNameRegistry _registry = new ERXRestNameRegistry();

	private Map<String, String> _internalNameForExternalName;
	private Map<String, String> _externalNameForInternalName;

	/**
	 * Returns the singleton name registry.
	 * 
	 * @return the singleton name registry
	 */
	public static ERXRestNameRegistry registry() {
		return _registry;
	}

	protected ERXRestNameRegistry() {
		_internalNameForExternalName = new HashMap<>();
		_externalNameForInternalName = new HashMap<>();
	}

	/**
	 * Sets the external name for a given internal name. For instance, if your entity is named "Person" but you want to
	 * display it as "Employee" in your service, you would setExternalNameForInternalName("Employee", "Person").
	 * 
	 * @param externalName
	 *            the name to expose in your service
	 * @param internalName
	 *            the name for use internally
	 */
	public void setExternalNameForInternalName(String externalName, String internalName) {
		_setExternalNameForInternalName(externalName, internalName);
		_setExternalNameForInternalName(ERXLocalizer.englishLocalizer().plurifiedString(externalName, 2), ERXLocalizer.englishLocalizer().plurifiedString(internalName, 2));
	}

	protected void _setExternalNameForInternalName(String externalName, String internalName) {
		_externalNameForInternalName.put(internalName, externalName);
		_internalNameForExternalName.put(externalName, internalName);

		String lowerInternalName = internalName.toLowerCase();
		String lowerExternalName = externalName.toLowerCase();
		_externalNameForInternalName.put(lowerInternalName, lowerExternalName);
		_internalNameForExternalName.put(lowerExternalName, lowerInternalName);
	}

	/**
	 * Returns the external name for the given internal name.
	 * 
	 * @param internalName
	 *            the internal name of the entity
	 * @return the external name of the entity
	 */
	public String externalNameForInternalName(String internalName) {
		String externalName = _externalNameForInternalName.get(internalName);
		if (externalName == null) {
			externalName = internalName;
		}
		return externalName;
	}

	/**
	 * Returns the internal name for the given external name.
	 * 
	 * @param externalName
	 *            the external name of the entity
	 * @return the internal name of the entity
	 */
	public String internalNameForExternalName(String externalName) {
		String internalName = _internalNameForExternalName.get(externalName);
		if (internalName == null) {
			internalName = externalName;
		}
		return internalName;
	}
}
