package er.directtoweb;

import java.util.Enumeration;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.directtoweb.D2WComponent;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.directtoweb.ERD2WContextDictionary.Configuration;
import er.extensions.ERXWOContext;

/**
 * Given a configured D2WContext ((entity+task or pageconfig) and propertyKey) and
 * - depending on task - d2wContext.object or displayGroup displays all the
 * available components together with their configuration info. As this info is pulled from
 * the d2wclientConfiguration.plist, it is of high importance that you keep these files up to date.
 * They are also used by the D2WAssistant and ERD2Ws component debugging features.
 * 
 * @author ak
 *
 */
public class ERD2WCheatSheet extends D2WComponent {

	public static Configuration configuration;

	public String currentComponentName;

	public String currentEditorKey;

	public ERD2WCheatSheet(WOContext context) {
		super(context);
		if (configuration == null) {
			configuration = new Configuration();
		}
	}

	public boolean synchronizesVariablesWithBindings() {
		return false;
	}
	
	public D2WContext localContext() {
		_localContext = (D2WContext) valueForBinding("localContext");
        return _localContext;
    }
	
	public D2WContext d2wContext() {
        return localContext();
    }

	public EOEnterpriseObject object() {
		return (EOEnterpriseObject) d2wContext().valueForKey("object");
	}

	public WODisplayGroup displayGroup() {
		return (WODisplayGroup) valueForBinding("displayGroup");
	}

	public NSArray keys() {
		NSMutableArray result = new NSMutableArray(d2wContext().componentsAvailable().toArray());
		result.remove("ERD2WCustomComponentWithArgs");
		result.remove("ERD2WStatelessCustomComponentWithArgs");
		result.remove("D2WCustomComponent");
		result.remove("D2WCustomQueryComponent");
		//result.remove("ERDEditStringWithChoices");
		//result.remove("ERDDisplayYearsMonths");
		result = (NSMutableArray) result.valueForKey("@unique");
		return result;
	}

	public NSDictionary editors() {
		NSMutableDictionary result = new NSMutableDictionary();
		NSArray editors = (NSArray) componentConfiguration().objectForKey("editors");
		if (editors != null) {
			for (Enumeration iter = editors.objectEnumerator(); iter.hasMoreElements();) {
				String key = (String) iter.nextElement();
				NSDictionary dict = (NSDictionary) configuration.editors().objectForKey(key);
				if (dict != null) {
					result.setObjectForKey(dict, key);
				}
			}
		}
		return result;
	}

	public NSDictionary currentEditor() {
		return (NSDictionary) editors().objectForKey(currentEditorKey);
	}

	public Object currentContextValue() {
		return d2wContext().valueForKey(currentEditorKey);
	}

	public String componentName() {
		String result = currentComponentName;
		return d2wContext().componentName();
	}

	public NSDictionary componentConfiguration() {
		return (NSDictionary) configuration.components().objectForKey(componentName());
	}

	public void setComponentName(String value) {
		currentComponentName = value;
		d2wContext().takeValueForKey(currentComponentName, "componentName");
		d2wContext().takeValueForKey(currentComponentName, "displayNameForProperty");
		String key = "contextDictionary." + d2wContext().dynamicPage();
		ERXWOContext.contextDictionary().removeObjectForKey(key);
	}

	public boolean useObject() {
		String task = d2wContext().task();
		return "inspect".equals(task) || "edit".equals(task);
	}

	public boolean useDisplayGroup() {
		String task = d2wContext().task();
		return "list".equals(task) || "query".equals(task) || "select".equals(task);
	}
}