package ognl.helperfunction;

import java.util.Enumeration;
import java.util.StringTokenizer;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver._private.WOBundle;
import com.webobjects.appserver._private.WOComponentDefinition;
import com.webobjects.appserver._private.WOComponentReference;
import com.webobjects.appserver._private.WODeclaration;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.appserver._private.WOGenerationSupport;
import com.webobjects.appserver._private.WOHTMLBareString;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation._NSUtilities;

public class WOHTMLWebObjectTag {
	private String _name;
	private WOHTMLWebObjectTag _parent;
	private NSMutableArray _children;

	private void extractName(String s) throws WOHelperFunctionHTMLFormatException {

		StringTokenizer stringtokenizer = new StringTokenizer(s, "=");
		if (stringtokenizer.countTokens() != 2) {
			throw new WOHelperFunctionHTMLFormatException("<WOHTMLWebObjectTag cannot initialize WebObject tag " + s + "> . It has no NAME=... parameter");
		}

		stringtokenizer.nextToken();
		String s1 = stringtokenizer.nextToken();

		int i = s1.indexOf('"');
		if (i != -1) {
			int j = s1.lastIndexOf('"');
			if (j > i) {
				_name = s1.substring(i + 1, j);
			}
		}
		else {
			StringTokenizer stringtokenizer1 = new StringTokenizer(s1);
			_name = stringtokenizer1.nextToken();
		}

		if (_name == null) {
			throw new WOHelperFunctionHTMLFormatException("<WOHTMLWebObjectTag cannot initialize WebObject tag " + s + "> . Failed parsing NAME parameter");
		}
	}

	public WOHTMLWebObjectTag() {
		_name = null;
	}

	public WOHTMLWebObjectTag(String s, WOHTMLWebObjectTag wohtmlwebobjecttag) throws WOHelperFunctionHTMLFormatException {
		_parent = wohtmlwebobjecttag;
		extractName(s);
	}

	public String name() {
		return _name;
	}

	public WOHTMLWebObjectTag parentTag() {
		return _parent;
	}

	public WOElement template() {
		NSMutableArray nsmutablearray = null;
		if (_children == null) {
			return null;
		}
		Enumeration enumeration = _children.objectEnumerator();
		if (enumeration != null) {
			nsmutablearray = new NSMutableArray(_children.count());
			StringBuilder stringbuffer = new StringBuilder(128);
			while (enumeration.hasMoreElements()) {
				Object obj1 = enumeration.nextElement();
				if (obj1 instanceof String) {
					stringbuffer.append((String) obj1);
				}
				else {
					if (stringbuffer.length() > 0) {
						WOHTMLBareString wohtmlbarestring1 = new WOHTMLBareString(stringbuffer.toString());
						nsmutablearray.addObject(wohtmlbarestring1);
						stringbuffer.setLength(0);
					}
					nsmutablearray.addObject(obj1);
				}
			}
			if (stringbuffer.length() > 0) {
				WOHTMLBareString wohtmlbarestring = new WOHTMLBareString(stringbuffer.toString());
				stringbuffer.setLength(0);
				nsmutablearray.addObject(wohtmlbarestring);
			}
		}
		WOElement obj = null;
		if (nsmutablearray != null && nsmutablearray.count() == 1) {
			Object obj2 = nsmutablearray.objectAtIndex(0);
			if (obj2 instanceof WOComponentReference) {
				obj = new WODynamicGroup(_name, null, (WOElement) obj2);
			}
			else {
				obj = (WOElement) obj2;
			}
		}
		else {
			obj = new WODynamicGroup(_name, null, nsmutablearray);
		}
		return obj;
	}

	public void addChildElement(Object obj) {
		if (_children == null) {
			_children = new NSMutableArray();
		}
		_children.addObject(obj);
	}

	public WOElement dynamicElement(NSDictionary nsdictionary, NSArray nsarray) throws WOHelperFunctionDeclarationFormatException, ClassNotFoundException {
		String s = name();
		WOElement woelement = template();
		WODeclaration wodeclaration = (WODeclaration) nsdictionary.objectForKey(s);
		return _elementWithDeclaration(wodeclaration, s, woelement, nsarray);
	}

	private static WOElement _componentReferenceWithClassNameDeclarationAndTemplate(String s, WODeclaration wodeclaration, WOElement woelement, NSArray nsarray) throws ClassNotFoundException {
		WOComponentReference wocomponentreference = null;
		WOComponentDefinition wocomponentdefinition = WOApplication.application()._componentDefinition(s, nsarray);
		if (wocomponentdefinition != null) {
			NSDictionary nsdictionary = wodeclaration.associations();
			wocomponentreference = wocomponentdefinition.componentReferenceWithAssociations(nsdictionary, woelement);
		}
		else {
			throw new ClassNotFoundException("Cannot find class or component named \'" + s + "\" in runtime or in a loadable bundle");
		}
		return wocomponentreference;
	}

	private static WOElement _elementWithClass(Class class1, WODeclaration wodeclaration, WOElement woelement) {
		WOElement woelement1 = WOApplication.application().dynamicElementWithName(class1.getName(), wodeclaration.associations(), woelement, null);
		if (NSLog.debugLoggingAllowedForLevelAndGroups(3, 8388608L)) {
			NSLog.debug.appendln("<WOHTMLWebObjectTag> Created Dynamic Element with name :" + class1.getName());
			NSLog.debug.appendln("Declaration : " + wodeclaration);
			NSLog.debug.appendln("Element : " + woelement1.toString());
		}
		return woelement1;
	}

	private static WOElement _elementWithDeclaration(WODeclaration wodeclaration, String s, WOElement woelement, NSArray nsarray) throws ClassNotFoundException, WOHelperFunctionDeclarationFormatException {
		WOElement woelement1 = null;
		if (wodeclaration != null) {
			String s1 = wodeclaration.type();
			if (s1 != null) {
				if (NSLog.debugLoggingAllowedForLevelAndGroups(3, 8388608L)) {
					NSLog.debug.appendln("<WOHTMLWebObjectTag> will look for " + s1 + " in the java runtime.");
				}
				Class class1 = _NSUtilities.classWithName(s1);
				if (class1 == null) {
					if (NSLog.debugLoggingAllowedForLevelAndGroups(3, 8388608L)) {
						NSLog.debug.appendln("<WOHTMLWebObjectTag> will look for com.webobjects.appserver._private." + s1 + " .");
					}
					class1 = WOBundle.lookForClassInAllBundles(s1);
					if (class1 == null) {
						NSLog.err.appendln("WOBundle.lookForClassInAllBundles(" + s1 + ") failed!");
					}
					else

					if (!(com.webobjects.appserver.WODynamicElement.class).isAssignableFrom(class1)) {
						class1 = null;
					}
				}

				if (class1 != null) {
					if (NSLog.debugLoggingAllowedForLevelAndGroups(3, 8388608L)) {
						NSLog.debug.appendln("<WOHTMLWebObjectTag> Will initialize object of class " + s1);
					}
					if ((com.webobjects.appserver.WOComponent.class).isAssignableFrom(class1)) {
						if (NSLog.debugLoggingAllowedForLevelAndGroups(3, 8388608L)) {
							NSLog.debug.appendln("<WOHTMLWebObjectTag> will look for " + s1 + " in the Compiled Components.");
						}
						woelement1 = _componentReferenceWithClassNameDeclarationAndTemplate(s1, wodeclaration, woelement, nsarray);
					}
					else {
						woelement1 = _elementWithClass(class1, wodeclaration, woelement);
					}
				}
				else {
					if (NSLog.debugLoggingAllowedForLevelAndGroups(3, 8388608L)) {
						NSLog.debug.appendln("<WOHTMLWebObjectTag> will look for " + s1 + " in the Frameworks.");
					}
					woelement1 = _componentReferenceWithClassNameDeclarationAndTemplate(s1, wodeclaration, woelement, nsarray);
				}
			}
			else {
				throw new WOHelperFunctionDeclarationFormatException("<WOHTMLWebObjectTag> declaration object for dynamic element (or component) named " + s + "has no class name.");
			}
		}
		else {
			throw new WOHelperFunctionDeclarationFormatException("<WOHTMLTemplateParser> no declaration for dynamic element (or component) named " + s);
		}

		WOGenerationSupport.insertInElementsTableWithName(woelement1, s, wodeclaration.associations());

		return woelement1;
	}
}