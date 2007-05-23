//
//  ERXNavigationItem.java
//  ERExtensions
//
//  Created by Max Muller on Wed Oct 30 2002.
//
package er.extensions;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

/** Please read "Documentation/Navigation.html" to fnd out how to use the navigation components.*/
public class ERXNavigationItem {

    private static int counter = 0;

    /** logging supprt */
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

    protected int _height;
    protected int _width;

    public ERXNavigationItem(NSDictionary values) {
        //set uniqueID
        _uniqueID=new String("id" + counter);
        counter++;
        if (values!=null) {
            if (log.isDebugEnabled())
                log.debug("ERXNavigationItem " + uniqueID() + "assigned these values at creation:\n" + values);
            _action=(String)values.valueForKey("action");
            _conditions = NSArray.EmptyArray;
            Object o = values.valueForKey("conditions");
            if (o != null) {
                if(o instanceof NSArray) {
                    _conditions = (NSArray)o;
                } else if(o instanceof String) {
                    _conditions=NSArray.componentsSeparatedByString((String)o,",");
                }
            }
            o = values.valueForKey("qualifier");
            if (o instanceof String && ((String)o).trim().length() > 0) {
                _qualifier = EOQualifier.qualifierWithQualifierFormat((String)o, null);
            }
            _directActionName=(String)values.valueForKey("directActionName");
            _directActionClass=(String)values.valueForKey("directActionClass");
            if (values.valueForKey("height")!=null)
                _height=Integer.valueOf((String)values.valueForKey("height")).intValue();
            if (values.valueForKey("width")!=null)
                _width=Integer.valueOf((String)values.valueForKey("width")).intValue();
            _name=(String)values.valueForKey("name");
            _displayName=(String)values.valueForKey("displayName");
            if (_displayName==null || _displayName.length()==0) _displayName=_name;
            _pageName=(String)values.valueForKey("pageName");
            _hasActivity=(String)values.valueForKey("hasActivity");
            if (values.valueForKey("children") != null && values.valueForKey("children") instanceof NSArray) {
                _children = (NSArray)values.valueForKey("children");
            } else if (values.valueForKey("children") != null && values.valueForKey("children") instanceof String) {
                _childrenBinding = (String)values.valueForKey("children");
            } else {
                _children = NSArray.EmptyArray;
            }
            if (values.valueForKey("childrenChoices") != null) {
                _childrenChoices = (NSDictionary)values.valueForKey("childrenChoices");
            } else {
                _childrenChoices = NSDictionary.EmptyDictionary;
            }
            if (values.valueForKey("queryBindings") != null) {
                _queryBindings = (NSDictionary)values.valueForKey("queryBindings");
            } else {
                _queryBindings = NSDictionary.EmptyDictionary;
            }
            if (values.valueForKey("childrenConditions") == null || ((String)values.valueForKey("childrenConditions")).equals("")) {
                _childrenConditions = NSArray.EmptyArray;
            } else {
                _childrenConditions=NSArray.componentsSeparatedByString((String)values.valueForKey("childrenConditions"),",");
            }
        } else {
            log.warn("Constructing a ERXNavigationItem with a null dictionary!");
        }
    }

    /**
     * Decides whether the item gets displayed at all.
     * This is done by evaluating the boolean value of a "conditions" array in the definition file.
     * eg: conditions = ("session.user.canEditThisStuff", "session.user.isEditor")
     * will display the item only if the user can edit this stuff *and* is an editor. You can set OR conditions with
     * conditions = (("session.user.canEditThisStuff", "session.user.isEditor"))
     * @param context context to evaluate visibility in
     */

    public boolean meetsDisplayConditionsInComponent(WOComponent component) {
    	Boolean meetsDisplayConditions = Boolean.TRUE;
    	if (conditions().count() != 0) {
    		Enumeration enumerator = conditions().objectEnumerator();
    		while (enumerator.hasMoreElements()) {
    			Object possibleKey = enumerator.nextElement();
    			if (possibleKey instanceof String) {
    				String anObject = (String) possibleKey;
    				Object value = component.valueForKeyPath(anObject);
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
    					Object value = component.valueForKeyPath(key);
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

	public String directActionName() {
		return directActionClass() == null ? _directActionName : directActionClass() + "/" + _directActionName;
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

	public String toString() {
		return "< " + name() + " >";
	}
}
