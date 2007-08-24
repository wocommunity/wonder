package er.extensions;

import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSArray;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.WOActionResults;

/**
 * This is an updated ERXNavigationMenu component that should simplify common usage.
 * Used in conjunction with the ERXModernNavigationMenuItem, it creates a navigation menu structure from the entries in
 * the NavigationMenu.plist configuration file.  This structure is a "standard" unordered list of (possibly nested)
 * navigation links.
 *
 * Please read "Documentation/Navigation.html" to find out how to use the navigation components.
 * 
 * @author Travis Cripps
 */
/* Note that I've purposely not extended the old class, hoping to deprecate or replace it with this one at a later date. */
public class ERXModernNavigationMenu extends ERXStatelessComponent {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERXModernNavigationMenu.class);

    public ERXNavigationItem aNavigationItem;

    protected ERXNavigationState _navigationState;
    protected NSKeyValueCoding _navigationContext;

    public int _renderLevelCount;

    protected boolean _menuIsSetUp=false;

    public ERXModernNavigationMenu(WOContext context) {
        super(context);
    }

    public void reset() {
        _menuIsSetUp = false;
        _renderLevelCount = 0;
        aNavigationItem = null;
        _navigationState = null;
        _navigationContext = null;
        super.reset();
    }

    public NSKeyValueCoding navigationContext() {
        if (_navigationContext == null) {
            _navigationContext = (NSKeyValueCoding)valueForBinding("navigationContext");
        }
        return _navigationContext;
    }

    public void setUpMenu() {
        if (!_menuIsSetUp) {
            if (navigationContext() != null) {
                Object o = navigationContext().valueForKey("navigationState");
                if(o != null) {
                    NSArray navigationState = (o instanceof NSArray ? (NSArray)o : NSArray.componentsSeparatedByString(o.toString(), "."));
                    if (navigationState != null && navigationState.count() > 0) {
                        navigationState().setState(navigationState);
                    } else {
                        o = (NSArray)navigationContext().valueForKey("additionalNavigationState");
                        o = (o == null ? NSArray.EmptyArray : o);
                        NSArray additionalNavigationState = (o instanceof NSArray ? (NSArray)o : NSArray.componentsSeparatedByString(o.toString(), "."));
                        if (additionalNavigationState != null && additionalNavigationState.count() > 0) {
                            if (additionalNavigationState != null && additionalNavigationState.count() > 0)
                                navigationState().setAdditionalState(additionalNavigationState);
                            else
                                navigationState().setAdditionalState(null);
                        } else if (ERXValueUtilities.booleanValue(navigationContext().valueForKey("shouldResetNavigationState"))) {
                            navigationState().setState(NSArray.EmptyArray);
                        }
                    }
                }
            }

            _menuIsSetUp=true;
        }
    }

    public ERXNavigationState navigationState() {
        if (_navigationState == null) {
            _navigationState = ERXNavigationManager.manager().navigationStateForSession(session());
        }
        return _navigationState;
    }

    public ERXNavigationItem rootNode() {
        return ERXNavigationManager.manager().rootNavigationItem();
    }


    public void takeValuesFromRequest(WORequest r, WOContext c) {
        setUpMenu();
        super.takeValuesFromRequest(r,c);
    }

    public void appendToResponse(WOResponse r, WOContext c) {
        setUpMenu();
        super.appendToResponse(r,c);
    }

    public WOActionResults invokeAction(WORequest r, WOContext c) {
        WOActionResults results=null;
        setUpMenu();
        try {
            results = super.invokeAction(r,c);
        } catch (RuntimeException e) {
            // Might need to ignore rapid clicks, catch some type of IllegalArgumentException
            throw e;
        }
        return results;
    }



}
