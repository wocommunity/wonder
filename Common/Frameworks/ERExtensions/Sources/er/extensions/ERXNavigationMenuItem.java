//
// ERXNavigationMenuItem.java: Class file for WO Component 'ERXNavigationMenuItem'
// Project ERExtensions
//
// Created by max on Wed Oct 30 2002
//
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;

import java.util.Enumeration;

public class ERXNavigationMenuItem extends ERXStatelessComponent {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERXNavigationMenuItem.class);
    
    protected ERXNavigationItem _navigationItem;
    protected ERXNavigationState _navigationState;

    protected int _level=-1;
    protected boolean _isDisabled;
    protected boolean _isDisabledComputed;
    protected boolean _meetsDisplayConditions=false;
    protected boolean _meetsDisplayConditionsComputed=false;
    protected boolean _isSelected;
    protected boolean _isSelectedComputed;
    protected Boolean _hasActivity;
    
    public ERXNavigationMenuItem(WOContext context) {
        super(context);
    }

    public void reset() {
        _navigationItem = null;
        _navigationState = null;
        _meetsDisplayConditionsComputed=false;
        _level=-1;
        _hasActivity=null;
        _isDisabledComputed=false;
        _isSelectedComputed=false;
        super.reset();
    }

    public ERXNavigationState navigationState() {
        if (_navigationState == null)
            _navigationState = ERXNavigationManager.manager().navigationStateForSession(session());
        return _navigationState;
    }
    
    public String contextComponentActionURL() {
        // If the navigation should be disabled return null
        if (navigationState().isDisabled() || meetsDisplayConditions() == false) {
            return null;
        }

        // If the user specified an action or pageName, return the source URL
        if ((navigationItem().action() != null) || (navigationItem().pageName() != null)) {
            // Return the URL to the action or page placed in the context by invokeAction
            return context().componentActionURL();
        }
        if (navigationItem().directActionName() != null) {
            return context().directActionURLForActionNamed(navigationItem().directActionName(),null);
        }

        // If the user specified some javascript, put that into the HREF and return it
        if (canGetValueForBinding("javascriptFunction")) {

            // Make sure there are no extra quotations marks - replace them with apostrophes
            String theFunction = (String)valueForBinding("javascriptFunction");
            return ERXStringUtilities.replaceStringByStringInString("\"", "'", theFunction);
        }

        return null;
    }

    public WOComponent menuItemSelected() {
        WOComponent anActionResult = null;

        if ((navigationItem().action() != null) && (navigationItem().action() != "")) {
            anActionResult = (WOComponent)valueForKeyPath(navigationItem().action());
        } else if ((navigationItem().pageName() != null) && (navigationItem().pageName() != "")) {
            anActionResult = (WOComponent)(pageWithName(navigationItem().pageName()));
        } else if ((navigationItem().directActionName() != null) && (navigationItem().directActionName() != "")) {
            // FIXME: Need to support directAction classes
            ERXDirectAction da = new ERXDirectAction(context().request());
            anActionResult = (WOComponent)(da.performActionNamed(navigationItem().directActionName()));
        }
        return anActionResult;
    }

    public boolean meetsDisplayConditions() {
        if (!_meetsDisplayConditionsComputed) {
            _meetsDisplayConditions = true;
            if (navigationItem().conditions().count() != 0) {
                Enumeration enumerator = navigationItem().conditions().objectEnumerator();
                while (enumerator.hasMoreElements()) {
                    String anObject = (String)enumerator.nextElement();
                    if (log.isDebugEnabled())
                        log.debug(navigationItem().name() + " testing display condition: "+ anObject + " --> " + ((Number)valueForKeyPath(anObject) != null ? ((Number)valueForKeyPath(anObject)).intValue()!=0 : false));
                    Number i = (Number)valueForKeyPath(anObject);
                    _meetsDisplayConditions =i!=null ? i.intValue()!=0 : false;
                    if (!_meetsDisplayConditions) break;
                }
            }
        }

        _meetsDisplayConditionsComputed = true;
        return _meetsDisplayConditions;
    }

    public ERXNavigationItem navigationItem() {
        if (_navigationItem==null) {
            _navigationItem = (ERXNavigationItem)valueForBinding("navigationItem");
        }
        return _navigationItem;
    }

    public boolean isDisabled() {
        if (!_isDisabledComputed) {
            _isDisabled=navigationState().isDisabled() || !meetsDisplayConditions();
            _isDisabledComputed=true;
        }
        return _isDisabled;
    }

    public boolean isSelected() {
        if (!_isSelectedComputed) {
            NSArray navigationState = navigationState().state();
            _isSelected=!isDisabled() && navigationState != null && navigationState.containsObject(navigationItem().name());
            _isSelectedComputed=true;
        }
        return _isSelected;
    }

    public int level() {
        if (_level==-1) {
            Integer l=(Integer)valueForBinding("level");
            _level=l!=null ? l.intValue() : 0;
        }
        return _level;
    }

    private final static String[] LINK_CLASS=new String[] { "", "Nav1", "Nav2", "Nav3" };
    private final static String[] LINK_CLASS_SELECTED=new String[] { "", "Nav1Selected", "Nav2Selected", "Nav3Selected" };
    private final static String[] LINK_CLASS_DISABLED=new String[] { "", "Nav1", "Nav2Disabled", "Nav3Disabled" };
    public String linkClass() {
        return isSelected() ? LINK_CLASS_SELECTED[level()] : isDisabled() ? LINK_CLASS_DISABLED[level()] : LINK_CLASS[level()];
    }

    private final static String[] COLOR=new String[] { "", "#EEEEEE", "#111111", "#EEEEEE", "#111111" };
    private final static String[] TD_BGCOLOR=new String[] { "", "#003366", "#d0d0d0", "#ff6600", "#ff6600" };
    private final static String[] DISABLED_TD_BGCOLOR=new String[] { "", "#003366", "#EFEFEF", "#ff9966", "#ff9966" };

    public String tdColor() {
        return !isDisabled()  ? TD_BGCOLOR[level()+(isSelected()? 1 : 0)] : DISABLED_TD_BGCOLOR[level()];
    }

    public Object resolveValue(String key) {
        return key!=null && key.startsWith("^") ? valueForKeyPath(key.substring(1)) : key;
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

    public String displayName() { return (String)resolveValue(navigationItem().displayName()); }
    
}
