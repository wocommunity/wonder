package er.rest;

import java.util.HashMap;
import java.util.Map;

public class ERXRestNameRegistry {
	private static ERXRestNameRegistry _registry = new ERXRestNameRegistry();

	private Map<String, String> _actualNameForDisplayName;
	private Map<String, String> _displayNameForActualName;

	public static ERXRestNameRegistry registry() {
		return _registry;
	}

	public ERXRestNameRegistry() {
		_actualNameForDisplayName = new HashMap<String, String>();
		_displayNameForActualName = new HashMap<String, String>();
	}

	public void setDisplayNameForActualName(String displayName, String actualName) {
		_displayNameForActualName.put(actualName, displayName);
		_actualNameForDisplayName.put(displayName, actualName);
	}

	public void setDisplayNameForActualName(String displayType, String displayKey, String actualType, String actualKey) {
		_displayNameForActualName.put(actualKey, displayType + "." + displayKey);
		_actualNameForDisplayName.put(displayKey, actualType + "." + actualKey);
	}

	public String displayNameForActualName(String type, String key) {
		return displayNameForActualName(type + "." + key);
	}

	public String displayNameForActualName(String actualName) {
		String displayName = _displayNameForActualName.get(actualName);
		if (displayName == null) {
			displayName = actualName;
		}
		return displayName;
	}

	public String actualNameForDisplayName(String type, String key) {
		return actualNameForDisplayName(type + "." + key);
	}

	public String actualNameForDisplayName(String displayName) {
		String actualName = _actualNameForDisplayName.get(displayName);
		if (actualName == null) {
			actualName = displayName;
		}
		return actualName;
	}
}
