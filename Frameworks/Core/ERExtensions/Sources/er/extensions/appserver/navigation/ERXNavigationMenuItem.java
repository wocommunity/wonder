//
// ERXNavigationMenuItem.java: Class file for WO Component 'ERXNavigationMenuItem'
// Project ERExtensions
//
// Created by max on Wed Oct 30 2002
//
package er.extensions.appserver.navigation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORedirect;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.appserver.ERXDirectAction;
import er.extensions.components.ERXStatelessComponent;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXValueUtilities;
import er.extensions.localization.ERXLocalizer;

/** Please read "Documentation/Navigation.html" to fnd out how to use the navigation components.*/

public class ERXNavigationMenuItem extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    public static final Logger log = Logger.getLogger(ERXNavigationMenuItem.class);
    
    protected ERXNavigationItem _navigationItem;
    protected ERXNavigationState _navigationState;

    protected boolean _linkDirectlyToDirectActions = true;
    
    protected int _level=-1;
    protected Boolean _isDisabled;
    protected Boolean _meetsDisplayConditions;
    protected Boolean _isSelected;
    protected Boolean _hasActivity;
    protected Boolean _omitLabelSpanTag;
    protected WOComponent _redirect;
    
    public ERXNavigationMenuItem(WOContext context) {
        super(context);
    }

    public String navigationItemWidth() {
    	if(navigationItem().width() > 0) {
    		return "" + navigationItem().width();
    	}
    	return null;
    }

    public String navigationItemID() {
    	if(navigationItem().uniqueID() != null) {
    		return navigationItem().uniqueID();
    	}
    	return null;
    }
    
    @Override
    public void reset() {
        _navigationItem = null;
        _navigationState = null;
        _meetsDisplayConditions=null;
        _level=-1;
        _hasActivity=null;
        _isDisabled=null;
        _isSelected=null;
        _omitLabelSpanTag=null;
        super.reset();
    }

    public ERXNavigationState navigationState() {
        if (_navigationState == null)
            _navigationState = ERXNavigationManager.manager().navigationStateForSession(session());
        return _navigationState;
    }

    /** AK This is only an experiment: when calling up a DA, we use a component action and redirect to the actual DA  */
    public WOComponent directActionRedirect() {
        WOComponent page = pageWithName("WORedirect");
        String url = context().directActionURLForActionNamed(navigationItem().directActionName(), navigationItem().queryBindings());
        ((WORedirect)page).setUrl(url);
        
        return page;
    }
    
    public String contextComponentActionURL() {
        // If the navigation should be disabled return null
        if (navigationState().isDisabled() || meetsDisplayConditions() == false) {
            return null;
        }

        // hrefs take precedence over actions.
        if (navigationItem().href() != null && !"".equals(navigationItem().href().trim())) {
        	return navigationItem().href();
        }
        
        // If the user specified an action or pageName, return the source URL
        if ((navigationItem().action() != null) || (navigationItem().pageName() != null)) {
            // Return the URL to the action or page placed in the context by invokeAction
            return context().componentActionURL();
        }
        if (navigationItem().directActionName() != null) {
        	if(_linkDirectlyToDirectActions) {
        		NSMutableDictionary bindings = navigationItem().queryBindings().mutableClone();
        		bindings.setObjectForKey(context().contextID(), "__cid");
        		return context().directActionURLForActionNamed(navigationItem().directActionName(), bindings);
        	}
        	return context().componentActionURL();
        }

        // If the user specified some javascript, put that into the HREF and return it
        if (canGetValueForBinding("javascriptFunction")) {

            // Make sure there are no extra quotations marks - replace them with apostrophes
            String theFunction = (String)valueForBinding("javascriptFunction");
            return StringUtils.replace(theFunction, "\"", "'");
        }

        return null;
    }

    public WOComponent menuItemSelected() {
        WOComponent anActionResult = null;

        if (!ERXStringUtilities.stringIsNullOrEmpty(navigationItem().action())) {
            anActionResult = (WOComponent)valueForKeyPath(navigationItem().action());
        } else if (!ERXStringUtilities.stringIsNullOrEmpty(navigationItem().pageName())) {
            anActionResult = pageWithName(navigationItem().pageName());
        } else if (!ERXStringUtilities.stringIsNullOrEmpty(navigationItem().directActionName())) {
            // FIXME: Need to support directAction classes
            if(_linkDirectlyToDirectActions) {
                ERXDirectAction da = new ERXDirectAction(context().request());
                anActionResult = (WOComponent)(da.performActionNamed(navigationItem().directActionName()));
            } else {
                anActionResult = (WOComponent)valueForKeyPath("directActionRedirect");
            }
        }
        return anActionResult;
    }

    public boolean meetsDisplayConditions() {
    	if (_meetsDisplayConditions == null) {
    		if(navigationItem() != null) {
    			_meetsDisplayConditions = navigationItem().meetsDisplayConditionsInComponent(this) ? Boolean.TRUE :  Boolean.FALSE;
    		} else {
    			_meetsDisplayConditions = Boolean.FALSE;
            }
        }
        return _meetsDisplayConditions.booleanValue();
    }

    public ERXNavigationItem navigationItem() {
        if (_navigationItem==null) {
            _navigationItem = (ERXNavigationItem)valueForBinding("navigationItem");
            if(_navigationItem == null) {
                String name = (String)valueForBinding("navigationItemName");
                if(name != null) {
                    _navigationItem = ERXNavigationManager.manager().navigationItemForName(name);
                } else {
                    log.warn("Navigation unset: " + name);
                    _navigationItem = ERXNavigationManager.manager().newNavigationItem(new NSDictionary(name, "name"));
                }
            }
        }
        return _navigationItem;
    }

    public boolean isDisabled() {
        if (_isDisabled == null) {
            _isDisabled=navigationState().isDisabled() || !meetsDisplayConditions() ? Boolean.TRUE : Boolean.FALSE;
        }
        return _isDisabled.booleanValue();
    }

    public boolean isSelected() {
        if (_isSelected == null) {
            NSArray navigationState = navigationState().state();
            _isSelected=!isDisabled() && navigationState != null && navigationState.containsObject(navigationItem().name()) ? Boolean.TRUE : Boolean.FALSE;

        }
        return _isSelected.booleanValue();
    }

    public int level() {
        if (_level==-1) {
            Integer l=(Integer)valueForBinding("level");
            _level=l!=null ? l.intValue() : 0;
        }
        return _level;
    }

    public String linkClass() {
        if(level() == 0) {
            return "";
        }
        return "Nav" + level() + (isSelected() ? "Selected" : (isDisabled() ? "Disabled" : ""));
    }

    private final static String[] COLOR=new String[] { "", "#EEEEEE", "#111111", "#EEEEEE", "#111111" };
    private final static String[] TD_BGCOLOR=new String[] { "", "#003366", "#d0d0d0", "#ff6600", "#ff6600" };
    private final static String[] DISABLED_TD_BGCOLOR=new String[] { "", "#003366", "#EFEFEF", "#ff9966", "#ff9966" };

    public String tdColor() {
        return !isDisabled()  ? TD_BGCOLOR[level()+(isSelected()? 1 : 0)] : DISABLED_TD_BGCOLOR[level()];
    }

    public Object resolveValue(String key) {
    	if(key != null && key.startsWith("^")) {
    		return valueForKeyPath(key.substring(1));
    	}
		return key;
	}

    public boolean hasActivity() {
        if (_hasActivity == null)
            _hasActivity = ERXValueUtilities.booleanValue(resolveValue(navigationItem().hasActivity())) ?
                Boolean.TRUE : Boolean.FALSE;
        return _hasActivity.booleanValue();
    }

    public boolean hasActivityAndIsEnabled(){
        return hasActivity() && !isDisabled();
    }

    public String displayName() {
    	String name = (String) resolveValue(navigationItem().displayName());
    	if(name != null) {
    		if(ERXProperties.booleanForKey("er.extensions.ERXNavigationManager.localizeDisplayKeys")) {
    			String localizerKey = "Nav." + name;
    			String localizedValue = ERXLocalizer.currentLocalizer().localizedStringForKey(localizerKey);
    			if(localizedValue == null) {
    				localizedValue = ERXLocalizer.currentLocalizer().localizedStringForKey(name);
    				if(localizedValue != null) {
    					log.info("Found old-style entry: " + localizerKey + "->" + localizedValue);
    					ERXLocalizer.currentLocalizer().takeValueForKey(localizedValue, localizerKey);
    					name = localizedValue;
    				}
    			} else {
    				name = localizedValue;
    			}
    		}
    	}
		return name;
	}

	public boolean omitLabelSpanTag() {
		if (_omitLabelSpanTag == null) {
			_omitLabelSpanTag = Boolean.valueOf(!ERXProperties.booleanForKeyWithDefault("er.extensions.ERXNavigationManager.includeLabelSpanTag", false));
		}
		return _omitLabelSpanTag;
	}
    
}
