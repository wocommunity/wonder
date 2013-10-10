package er.extensions.appserver.navigation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORedirect;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.appserver.ERXDirectAction;
import er.extensions.components.ERXStatelessComponent;
import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXValueUtilities;
import er.extensions.localization.ERXLocalizer;

/**
 * This is a menu item component that represents a single item in the tree of navigation menu items.
 * It's an updated ERXNavigationMenuItem component that should simplify common usage.  Namely, it now recurses through
 * the tree of navigation items, creating nested, unordered lists of navigation items. Just as importantly, with a very 
 * few exceptions,it forgoes declaring element style as possible, leaving positioning and styling to be defined in the
 * user's stylesheet.
 *
 * Please read "Documentation/Navigation.html" to find out how to use the navigation components.
 * 
 * @author Travis Cripps
 */
/* Note that I've purposely not extended the old class, hoping to deprecate or replace it with this one at a later date. */
public class ERXModernNavigationMenuItem extends ERXStatelessComponent {
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

    protected Boolean _isDisabled;
    protected Boolean _meetsDisplayConditions;
    protected Boolean _isSelected;
    protected Boolean _hasActivity;
    protected WOComponent _redirect;

    public ERXNavigationItem aChildItem; // used in WORepetition


    private static final String EMPTY_STRING = "";

    protected static final String STYLE_CLASS_SELECTED = "selected";
    protected static final String STYLE_CLASS_DISABLED = "disabled";
    protected static final String STYLE_CLASS_SUB = "sub";

    public static final String SHOULD_DISPLAY_DISABLED_MENU_ITEMS = "ERXModernNavigationMenuItem.shouldDisplayDisabledMenuItems";


    public ERXModernNavigationMenuItem(WOContext context) {
        super(context);
    }

	 public String navigationItemID() {
    	if (navigationItem().uniqueID() != null) {
    		return navigationItem().uniqueID();
    	}
    	return null;
    }

    @Override
    public void reset() {
        _navigationItem = null;
        _navigationState = null;
        _meetsDisplayConditions = null;
        _hasActivity = null;
        _isDisabled = null;
        _isSelected = null;
        
        super.reset();
    }

	public String navigationItemWidth() {
    	if (navigationItem().width() > 0) {
    		return "" + navigationItem().width();
    	}
    	return null;
    }

    public ERXNavigationState navigationState() {
        if (_navigationState == null) {
            return ERXNavigationManager.manager().navigationStateForSession(session());
        }
        return _navigationState;
    }

    /**
     * AK This is only an experiment: when calling up a DA, we use a component action and redirect to the actual DA
     * @return a WORedirect to the direct action URL.
     */
    public WOComponent directActionRedirect() {
        WOComponent page = pageWithName("WORedirect");
        String url = context().directActionURLForActionNamed(navigationItem().directActionName(), navigationItem().queryBindings());
        ((WORedirect)page).setUrl(url);

        return page;
    }

    public String contextComponentActionURL() {
        // If the navigation should be disabled return null
        if (navigationState().isDisabled() || !meetsDisplayConditions()) {
            return null;
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

    /**
     * Determines whether the menu item is selected, or in the path of the current navigation state.
     * @return true if the menu item is selected
     */
    public WOComponent menuItemSelected() {
        WOComponent anActionResult = null;

        if ((navigationItem().action() != null) && (!navigationItem().action().equals(EMPTY_STRING))) {
            anActionResult = (WOComponent)valueForKeyPath(navigationItem().action());
            
            // it would be nice to have the navigation state to be associated with the 
            // ERXNavigationItem during loadNavigationMenu(). But, with the current model
            // this menu system allows the same ERXNavigationItem to be a child of more than one parent items.
            // So we have to loop onItemClick until we hit the root node to 
            // get the navigationState.  --santoash
            // Note: The parent() on an item is only set when you ask for a children of a particular item.
            // @see ERXNavigationItem.childItemsInContext()
            NSMutableArray state = new NSMutableArray();
            ERXNavigationItem currentNavItem = navigationItem(); 
            do {
                state.addObject(currentNavItem.name());
                currentNavItem = currentNavItem.parent();
            } while (!currentNavItem.isRootNode());
            
            ERXNavigationManager.manager().navigationStateForSession(session()).setState(ERXArrayUtilities.reverse(state));
        } else if ((navigationItem().pageName() != null) && (!navigationItem().pageName().equals(EMPTY_STRING))) {
            anActionResult = pageWithName(navigationItem().pageName());
        } else if ((navigationItem().directActionName() != null) && (!navigationItem().directActionName().equals(EMPTY_STRING))) {
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

    /**
     * Decides whether the item gets displayed at all.
     * This is done by evaluating the boolean value of a "conditions" array in the definition file.
     * eg: conditions = ("session.user.canEditThisStuff", "session.user.isEditor")
     * will display the item only if the user can edit this stuff *and* is an editor.
     * @return true if the display conditions are met
     */
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

    /**
     * Determines if the item should be displayed in the UI, based upon the disabled status.  You may disable display of
     * items that do not meet their display conditions or are explicitly disabled.
     * @return true if the item should be displayed
     */
    public boolean shouldDisplay() {
        boolean result = true;
        if (isDisabled()) {
            // Must explicitly disable display with a property.
            result = ERXProperties.booleanForKeyWithDefault(SHOULD_DISPLAY_DISABLED_MENU_ITEMS, true);
        }
        
        return result;
    }

    /**
     * Gets the {@link ERXNavigationItem} that provides the backing store for the properties of this menu item.
     * @return the navigation item
     */
    public ERXNavigationItem navigationItem() {
        if (_navigationItem == null) {
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
            _isDisabled = navigationState().isDisabled() || !meetsDisplayConditions() ? Boolean.TRUE : Boolean.FALSE;
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



    public String itemStyleClass() {
        NSMutableArray styleClasses = new NSMutableArray();
        String result = EMPTY_STRING;

        // Check to see if the item is disabled.
        if (isDisabled()) {
            styleClasses.addObject(STYLE_CLASS_DISABLED);
        } else {
            // Check to see if this is one of the "active" locations in the navigation state.
            if (navigationState().state().containsObject(navigationItem().name())) {
                styleClasses.addObject(STYLE_CLASS_SELECTED);
            }

            if (children().count() > 0) {
                styleClasses.addObject(STYLE_CLASS_SUB);
            }
        }

        if (styleClasses.count() > 0) {
            result += styleClasses.componentsJoinedByString(" ");
        }

        return result;
    }

    public Object resolveValue(String key) {
        if (key != null && key.startsWith("^")) {
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
    
    public NSArray children() {
        return navigationItem().childItemsInContext(this);
    }


    public NSKeyValueCodingAdditions navigationContext() {
        return (NSKeyValueCodingAdditions)valueForBinding("navigationContext");
    }
}