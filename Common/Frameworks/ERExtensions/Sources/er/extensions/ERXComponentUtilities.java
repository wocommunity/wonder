package er.extensions;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResourceManager;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * ERXComponentUtilities contains WOComponent/WOElement-related utility methods.
 * 
 * @author mschrag
 */
public class ERXComponentUtilities {
	/**
	 * Returns a query parameter dictionary from a set of ?key=association
	 * WOAssociation dictionary.
	 * 
	 * @param associations
	 *            the set of associations
	 * @param component
	 *            the component to evaluate their values within
	 * @return a dictionary of key=value query parameters
	 */
	public static NSMutableDictionary queryParametersInComponent(NSDictionary associations, WOComponent component) {
		NSMutableDictionary queryParameterAssociations = ERXComponentUtilities.queryParameterAssociations(associations);
		return _queryParametersInComponent(queryParameterAssociations, component);
	}

	/**
	 * Returns a query parameter dictionary from a set of ?key=association
	 * WOAssociation dictionary.
	 * 
	 * @param associations
	 *            the set of associations
	 * @param component
	 *            the component to evaluate their values within
	 * @param removeQueryParameterAssociations
	 *            should the entries be removed from the passed-in dictionary?
	 * @return a dictionary of key=value query parameters
	 */
	public static NSMutableDictionary queryParametersInComponent(NSMutableDictionary associations, WOComponent component, boolean removeQueryParametersAssociations) {
		NSMutableDictionary queryParameterAssociations = ERXComponentUtilities.queryParameterAssociations(associations, removeQueryParametersAssociations);
		return _queryParametersInComponent(queryParameterAssociations, component);
	}

	public static NSMutableDictionary _queryParametersInComponent(NSMutableDictionary associations, WOComponent component) {
		NSMutableDictionary queryParameters = new NSMutableDictionary();
		Enumeration keyEnum = associations.keyEnumerator();
		while (keyEnum.hasMoreElements()) {
			String key = (String) keyEnum.nextElement();
			WOAssociation association = (WOAssociation) associations.valueForKey(key);
			Object associationValue = association.valueInComponent(component);
			if (associationValue != null) {
				queryParameters.setObjectForKey(associationValue, key.substring(1));
			}
		}
		return queryParameters;
	}

	/**
	 * Returns the set of ?key=value associations from an associations
	 * dictionary.
	 * 
	 * @param associations
	 *            the associations to enumerate
	 */
	public static NSMutableDictionary queryParameterAssociations(NSDictionary associations) {
		return ERXComponentUtilities._queryParameterAssociations(associations, false);
	}

	/**
	 * Returns the set of ?key=value associations from an associations
	 * dictionary. If removeQueryParameterAssociations is true, the
	 * corresponding entries will be removed from the associations dictionary
	 * that was passed in.
	 * 
	 * @param associations
	 *            the associations to enumerate
	 * @param removeQueryParameterAssociations
	 *            should the entries be removed from the passed-in dictionary?
	 */
	public static NSMutableDictionary queryParameterAssociations(NSMutableDictionary associations, boolean removeQueryParameterAssociations) {
		return ERXComponentUtilities._queryParameterAssociations(associations, removeQueryParameterAssociations);
	}

	public static NSMutableDictionary _queryParameterAssociations(NSDictionary associations, boolean removeQueryParameterAssociations) {
		NSMutableDictionary mutableAssociations = null;
		if (removeQueryParameterAssociations) {
			mutableAssociations = (NSMutableDictionary) associations;
		}
		NSMutableDictionary queryParameterAssociations = new NSMutableDictionary();
		Enumeration keyEnum = associations.keyEnumerator();
		while (keyEnum.hasMoreElements()) {
			String key = (String) keyEnum.nextElement();
			if (key.startsWith("?")) {
				WOAssociation association = (WOAssociation) associations.valueForKey(key);
				if (mutableAssociations != null) {
					mutableAssociations.removeObjectForKey(key);
				}
				queryParameterAssociations.setObjectForKey(association, key);
			}
		}
		return queryParameterAssociations;
	}

	/**
	 * Returns the boolean value of a binding.
	 * 
	 * @param component
	 *            the component
	 * @param bindingName
	 *            the name of the boolean binding
	 * @return a boolean
	 */
	public static boolean booleanValueForBinding(WOComponent component, String bindingName) {
		return ERXComponentUtilities.booleanValueForBinding(component, bindingName, false);
	}

	/**
	 * Returns the boolean value of a binding.
	 * 
	 * @param component
	 *            the component
	 * @param bindingName
	 *            the name of the boolean binding
	 * @param defaultValue
	 *            the default value if the binding is null
	 * @return a boolean
	 */
	public static boolean booleanValueForBinding(WOComponent component, String bindingName, boolean defaultValue) {
		Boolean valueBoolean = (Boolean) component.valueForBinding(bindingName);
		boolean value;
		if (valueBoolean == null) {
			value = defaultValue;
		}
		else {
			value = valueBoolean.booleanValue();
		}
		return value;
	}

	/**
	 * Returns the URL of the html template for the given component name.
	 * 
	 * @param componentName
	 *            the name of the component to load a template for (without the
	 *            .wo)
	 * @param languages
	 *            the list of languages to use for finding components
	 * @return the URL to the html template (or null if there isn't one)
	 */
	public static URL htmlTemplateUrl(String componentName, NSArray languages) {
		return ERXComponentUtilities.templateUrl(componentName, "html", languages);
	}

	/**
	 * Returns the URL of the template for the given component name.
	 * 
	 * @param componentName
	 *            the name of the component to load a template for (without the
	 *            .wo)
	 * @param extension
	 *            the file extension of the template (without the dot -- i.e.
	 *            "html")
	 * @param languages
	 *            the list of languages to use for finding components
	 * @return the URL to the template (or null if there isn't one)
	 */
	public static URL templateUrl(String componentName, String extension, NSArray languages) {
		String htmlPathName = componentName + ".wo/" + componentName + "." + extension;
		WOResourceManager resourcemanager = WOApplication.application().resourceManager();
		URL templateUrl = resourcemanager.pathURLForResourceNamed(htmlPathName, null, languages);
		if (templateUrl == null) {
			NSArray frameworkBundles = NSBundle.frameworkBundles();
			if (frameworkBundles != null) {
				Enumeration frameworksEnum = frameworkBundles.objectEnumerator();
				while (templateUrl == null && frameworksEnum.hasMoreElements()) {
					NSBundle frameworkBundle = (NSBundle) frameworksEnum.nextElement();
					templateUrl = resourcemanager.pathURLForResourceNamed(htmlPathName, frameworkBundle.name(), languages);
				}
			}
		}
		return templateUrl;
	}

	/**
	 * Returns the contents of the html template for the given component name as
	 * a string.
	 * 
	 * @param componentName
	 *            the name of the component to load a template for (without the
	 *            .wo)
	 * @param languages
	 *            the list of languages to use for finding components
	 * @return the string contents of the html template (or null if there isn't
	 *         one)
	 */
	public static String htmlTemplate(String componentName, NSArray languages) throws IOException {
		return ERXComponentUtilities.template(componentName, "html", languages);
	}

	/**
	 * Returns the contents of the template for the given component name as a
	 * string.
	 * 
	 * @param componentName
	 *            the name of the component to load a template for (without the
	 *            .wo)
	 * @param extension
	 *            the file extension of the template (without the dot -- i.e.
	 *            "html")
	 * @param languages
	 *            the list of languages to use for finding components
	 * @return the string contents of the template (or null if there isn't one)
	 */
	public static String template(String componentName, String extension, NSArray languages) throws IOException {
		String template;
		URL templateUrl = ERXComponentUtilities.templateUrl(componentName, extension, languages);
		if (templateUrl == null) {
			template = null;
		}
		else {
			template = ERXStringUtilities.stringFromURL(templateUrl);
		}
		return template;
	}

	/**
	 * Returns an array of the current component names.
	 * 
	 * @return
	 */
	public static NSArray componentTree() {
		WOContext context = ERXWOContext.currentContext();
		NSMutableArray<String> result = new NSMutableArray<String>();
		if (context != null) {
			WOComponent c = context.component();
			while (c != null) {
				result.addObject(c.name());
				c = c.parent();
			}
		}
		return result;
	}

	/**
	 * Appends a dictionary of associations as HTML attributes.
	 * 
	 * @param associations
	 *            the associations dictionary
	 * @param response
	 *            the response to write to
	 * @param context
	 *            the context
	 */
	public static void appendHtmlAttributes(NSDictionary<String, WOAssociation> associations, WOResponse response, WOContext context) {
		WOComponent component = context.component();
		ERXComponentUtilities.appendHtmlAttributes(associations, response, component);
	}

	/**
	 * Appends a dictionary of associations as HTML attributes.
	 * 
	 * @param associations
	 *            the associations dictionary
	 * @param response
	 *            the response to write to
	 * @param component
	 *            the component to evaluate the associations within
	 */
	public static void appendHtmlAttributes(NSDictionary<String, WOAssociation> associations, WOResponse response, WOComponent component) {
		for (String key : associations.allKeys()) {
			WOAssociation association = associations.objectForKey(key);
			ERXComponentUtilities.appendHtmlAttribute(key, association, response, component);
		}
	}

	/**
	 * Appends an association as an HTML attribute.
	 * 
	 * @param key
	 *            the key to append
	 * @param association
	 *            the association
	 * @param response
	 *            the response to write to
	 * @param component
	 *            the component to evaluate the association within
	 */
	public static void appendHtmlAttribute(String key, WOAssociation association, WOResponse response, WOComponent component) {
		Object value = association.valueInComponent(component);
		if (value != null) {
			response.appendContentString(" ");
			response.appendContentString(key);
			response.appendContentString("=\"");
			response.appendContentHTMLAttributeValue(value.toString());
			response.appendContentString("\"");
		}
	}

	/**
	 * Returns the component for the given class without having to cast. For
	 * example: MyPage page = ERXComponentUtilities.pageWithName(MyPage.class,
	 * context);
	 * 
	 * @param <T>
	 *            the type of component to
	 * @param componentClass
	 *            the component class to lookup
	 * @param context
	 *            the context
	 * @return the created component
	 */
	@SuppressWarnings("unchecked")
	public static <T extends WOComponent> T pageWithName(Class<T> componentClass, WOContext context) {
		return (T) ERXApplication.erxApplication().pageWithName(componentClass, context);
	}

	/**
	 * Calls pageWithName with ERXWOContext.currentContext() for the current
	 * thread.
	 * 
	 * @param <T>
	 *            the type of component to
	 * @param componentClass
	 *            the component class to lookup
	 * @return the created component
	 */
	@SuppressWarnings("unchecked")
	public static <T extends WOComponent> T pageWithName(Class<T> componentClass) {
		return (T) ERXApplication.erxApplication().pageWithName(componentClass);
	}

}
