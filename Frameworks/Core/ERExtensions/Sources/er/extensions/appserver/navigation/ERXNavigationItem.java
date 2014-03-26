//
//  ERXNavigationItem.java
//  ERExtensions
//
//  Created by Max Muller on Wed Oct 30 2002.
//
package er.extensions.appserver.navigation;

import java.io.Serializable;
import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.foundation.ERXValueUtilities;

/**
 * A "backing store" for the properties of a single navigation item in the tree
 * of navigation items. Configured by the {@link ERXNavigationManager
 * ERXNavigationManager} from a dictionary stored in the navigation menu plist
 * file.
 * 
 * Please read "Documentation/Navigation.html" to fnd out how to use the
 * navigation components.
 * 
 * @see ERXNavigationManager
 * @see ERXNavigationMenuItem
 */
public class ERXNavigationItem implements Serializable {
	/**
	 * Do I need to update serialVersionUID? See section 5.6 <cite>Type Changes
	 * Affecting Serialization</cite> on page 51 of the <a
	 * href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object
	 * Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private static int counter = 0;

	/** logging support */
	public static final Logger log = Logger.getLogger(ERXNavigationItem.class);

	public String _uniqueID;

	protected String _action;
	protected NSArray _conditions;
	protected EOQualifier _qualifier;
	protected String _directActionName;
	protected String _directActionClass;
	protected String _name;
	protected String _pageName;
	protected String _displayName;
	protected String _hasActivity;
	protected NSArray _children, _childrenConditions;
	protected String _childrenBinding;
	protected NSDictionary _childrenChoices;
	protected NSDictionary _queryBindings;
	protected String _href;
	protected String _secure;
	protected ERXNavigationItem _parent;

	protected int _height;
	protected int _width;

	public ERXNavigationItem(NSDictionary values) {
		// set uniqueID
		_uniqueID = "id" + counter;
		counter++;
		if (values != null) {
			if (log.isDebugEnabled())
				log.debug("ERXNavigationItem " + uniqueID() + "assigned these values at creation:\n" + values);
			_action = (String) values.valueForKey("action");
			_conditions = NSArray.EmptyArray;
			Object o = values.valueForKey("conditions");
			if (o != null) {
				if (o instanceof NSArray) {
					_conditions = (NSArray) o;
				}
				else if (o instanceof String) {
					_conditions = NSArray.componentsSeparatedByString((String) o, ",");
				}
			}
			o = values.valueForKey("qualifier");
			if (o instanceof String && ((String) o).trim().length() > 0) {
				_qualifier = EOQualifier.qualifierWithQualifierFormat((String) o, null);
			}
			_href = (String) values.valueForKey("href");
			_directActionName = (String) values.valueForKey("directActionName");
			_directActionClass = (String) values.valueForKey("directActionClass");
			if (values.valueForKey("height") != null)
				_height = Integer.valueOf((String) values.valueForKey("height")).intValue();
			if (values.valueForKey("width") != null)
				_width = Integer.valueOf((String) values.valueForKey("width")).intValue();
			_name = (String) values.valueForKey("name");
			_displayName = (String) values.valueForKey("displayName");
			if (_displayName == null || _displayName.length() == 0)
				_displayName = _name;
			_pageName = (String) values.valueForKey("pageName");
			_secure = (String) values.valueForKey("secure");
			_hasActivity = (String) values.valueForKey("hasActivity");
			if (values.valueForKey("children") != null && values.valueForKey("children") instanceof NSArray) {
				_children = (NSArray) values.valueForKey("children");
			}
			else if (values.valueForKey("children") != null && values.valueForKey("children") instanceof String) {
				_childrenBinding = (String) values.valueForKey("children");
			}
			else {
				_children = NSArray.EmptyArray;
			}
			if (values.valueForKey("childrenChoices") != null) {
				_childrenChoices = (NSDictionary) values.valueForKey("childrenChoices");
			}
			else {
				_childrenChoices = NSDictionary.EmptyDictionary;
			}
			if (values.valueForKey("queryBindings") != null) {
				_queryBindings = (NSDictionary) values.valueForKey("queryBindings");
			}
			else {
				_queryBindings = NSDictionary.EmptyDictionary;
			}
			if (values.valueForKey("childrenConditions") == null || ((String) values.valueForKey("childrenConditions")).equals("")) {
				_childrenConditions = NSArray.EmptyArray;
			}
			else {
				_childrenConditions = NSArray.componentsSeparatedByString((String) values.valueForKey("childrenConditions"), ",");
			}
		}
		else {
			log.warn("Constructing a ERXNavigationItem with a null dictionary!");
		}
	}

	/**
	 * Decides whether the item gets displayed at all. This is done by
	 * evaluating the boolean value of a "conditions" array in the definition
	 * file. eg: conditions = ("session.user.canEditThisStuff",
	 * "session.user.isEditor") will display the item only if the user can edit
	 * this stuff *and* is an editor. You can set OR conditions with conditions
	 * = (("session.user.canEditThisStuff", "session.user.isEditor"))
	 * 
	 * @param context
	 *            in which to evaluate visibility
	 * @return true if the item meets display conditions
	 */
	public boolean meetsDisplayConditionsInComponent(NSKeyValueCodingAdditions context) {
		Boolean meetsDisplayConditions = Boolean.TRUE;
		if (conditions().count() != 0) {
			Enumeration enumerator = conditions().objectEnumerator();
			while (enumerator.hasMoreElements()) {
				Object possibleKey = enumerator.nextElement();
				if (possibleKey instanceof String) {
					String anObject = (String) possibleKey;
					Object value = context.valueForKeyPath(anObject);
					meetsDisplayConditions = ERXValueUtilities.booleanValue(value) ? Boolean.TRUE : Boolean.FALSE;
					if (log.isDebugEnabled()) {
						log.debug(name() + " testing display condition: " + anObject + " --> " + value + ":" + meetsDisplayConditions);
					}
					if (!meetsDisplayConditions.booleanValue()) {
						break;
					}
				}
				else {
					boolean temp = false;
					Enumeration e2 = ((NSArray) possibleKey).objectEnumerator();
					while (e2.hasMoreElements()) {
						String key = (String) e2.nextElement();
						Object value = context.valueForKeyPath(key);
						temp |= ERXValueUtilities.booleanValue(value);
						if (temp) {
							break;
						}
						if (log.isDebugEnabled()) {
							log.debug(name() + " testing display condition: " + key + " --> " + value + ":" + meetsDisplayConditions);
						}
					}
					meetsDisplayConditions = temp ? Boolean.TRUE : Boolean.FALSE;
					if (!meetsDisplayConditions.booleanValue()) {
						break;
					}
				}
			}
			if (meetsDisplayConditions.booleanValue() && qualifier() != null) {
				meetsDisplayConditions = qualifier().evaluateWithObject(this) ? Boolean.TRUE : Boolean.FALSE;
			}
		}
		return meetsDisplayConditions.booleanValue();
	}

	public NSArray childItemsInContext(NSKeyValueCodingAdditions context) {
		NSArray children = null;

		NSArray childrenConditions = childrenConditions();
		boolean hasChildrenConditions = childrenConditions.count() != 0;
		boolean meetsChildrenConditions = true;
		if (hasChildrenConditions) {
			for (Enumeration e = childrenConditions.objectEnumerator(); e.hasMoreElements();) {
				String aCondition = (String) e.nextElement();
				meetsChildrenConditions = ERXValueUtilities.booleanValue(context.valueForKeyPath(aCondition));
				if (!meetsChildrenConditions)
					break;
			}
		}

		if (meetsChildrenConditions) {
			/*
			 * only want to do this if childrenConditions are met, or if there
			 * aren't any children conditions
			 */
			if (children() != null) {
				children = children();
			}
			else if (childrenBinding() != null) {
				Object o = context.valueForKeyPath(childrenBinding());
				if (o != null && o instanceof NSArray) {
					children = (NSArray) o;
				}
				else if (o != null && o instanceof String) {
					children = (NSArray) childrenChoices().objectForKey(o);
					if (children == null) {
						log.warn("For nav core object: " + this + " and child binding: " + childrenBinding() + " couldn't find children for choice key: " + o);
					}
				}
				else {
					log.warn("For nav core object: " + this + " and child binding: " + childrenBinding() + " recieved binding object: " + o);
				}
			}
		}

		if (children == null) {
			children = NSArray.EmptyArray;
		}
		if (children.count() > 0) {
			NSMutableArray childNavItems = new NSMutableArray();
			for (Enumeration e = children.objectEnumerator(); e.hasMoreElements();) {
				String childName = (String) e.nextElement();
				ERXNavigationItem item = ERXNavigationManager.manager().navigationItemForName(childName);
				if (item != null) {
					/*
					 * since same child node can be shared by multiple parents
					 * setParent is differed until now. every time children are
					 * asked for, 'this' parent is set on them.
					 */
					item.setParent(this);
					childNavItems.addObject(item);
				}
				else {
					log.warn("Unable to find navigation item for name: " + childName);
				}
			}
			children = childNavItems;
		}
		return children;
	}
	
	public Boolean secureInContext(NSKeyValueCodingAdditions context) {
		if(_secure == null) {
			return null;
		}
		Object value = _secure;
		if(_secure.indexOf('.') > -1) {
			value = context.valueForKeyPath(_secure);
		}
		return ERXValueUtilities.BooleanValueWithDefault(value, null);
	}

	public boolean isRootNode() {
		return this == ERXNavigationManager.manager().rootNavigationItem();
	}

	public NSArray children() {
		return _children;
	}

	public EOQualifier qualifier() {
		return _qualifier;
	}

	public String childrenBinding() {
		return _childrenBinding;
	}

	public NSArray childrenConditions() {
		return _childrenConditions;
	}

	public NSDictionary childrenChoices() {
		return _childrenChoices;
	}

	public NSDictionary queryBindings() {
		return _queryBindings;
	}

	public String action() {
		return _action;
	}

	public NSArray conditions() {
		return _conditions;
	}

	public String href() {
		return _href;
	}

	public String directActionName() {
		return directActionClass() == null ? _directActionName : directActionClass() + "/" + _directActionName;
	}

	public String uneditedDirectActionName() {
		return _directActionName;
	}

	public String directActionClass() {
		return _directActionClass;
	}

	public int height() {
		return _height;
	}

	public int width() {
		return _width;
	}

	public String name() {
		return _name;
	}

	public String pageName() {
		return _pageName;
	}

	public String uniqueID() {
		return _uniqueID;
	}

	public String displayName() {
		return _displayName;
	}

	public String hasActivity() {
		return _hasActivity;
	}

	@Override
	public String toString() {
		return "< " + name() + " >";
	}

	public ERXNavigationItem parent() {
		return _parent;
	}

	public void setParent(ERXNavigationItem item) {
		_parent = item;
	}

	/**
	 * Returns path of this navigationMenuItem starting from the top menu except
	 * the root navigation item separated by /. ex:
	 * topMenuItem/secondlevelmenuitem/thirdlevelnavItem NOTE: navigationPath
	 * doesn't include rootNavigationItem.
	 * 
	 * @return {@link String} navigationPath
	 */
	public String navigationPath() {
		StringBuilder result = new StringBuilder();

		// local variable to keep track of the navItem in the loop
		ERXNavigationItem navItem = this;
		result.append(navItem.name());
		while (navItem.parent() != null && navItem.parent() != ERXNavigationManager.manager().rootNavigationItem()) {
			navItem = navItem.parent();
			result.insert(0, navItem.name() + "/");

		}

		return result.toString();
	}
}
