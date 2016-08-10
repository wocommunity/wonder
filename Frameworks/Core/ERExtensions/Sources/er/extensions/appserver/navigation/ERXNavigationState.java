//
//  ERXNavigationState.java
//  ERExtensions
//
//  Created by Max Muller on Wed Oct 30 2002.
//
package er.extensions.appserver.navigation;

import java.io.Serializable;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSRange;

import er.extensions.foundation.ERXValueUtilities;

/** Please read "Documentation/Navigation.html" to fnd out how to use the navigation components.*/
public class ERXNavigationState implements Serializable {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(ERXNavigationState.class);
    
    protected NSArray _additionalState;
    protected NSArray _state;
    protected boolean isDisabled;
    
    public ERXNavigationState() {
        super();
    }

    public NSArray state() {
        NSArray state = _state == null ? NSArray.EmptyArray : _state;
        if (_additionalState != null) {
            NSMutableArray combinedState = new NSMutableArray(state);
            combinedState.addObjectsFromArray(_additionalState);
            state = combinedState;
        }
        return state;
    }

    public void setAdditionalState(NSArray value) {
        log.debug("Setting additional navigation state: {}", value);
        _additionalState = value;
    }

    public NSArray navigationItemsToBeShownForLevel(int currentNavigationLevel) {
        NSArray itmesToBeShown = null;
        int navCount = state() != null ? state().count() : 0;
        if (currentNavigationLevel == 0) {
            itmesToBeShown = state();
        } else if (navCount != 0 && navCount <= currentNavigationLevel) {
            itmesToBeShown = new NSArray(state().lastObject());
        } else if (navCount != 0 && currentNavigationLevel < navCount) {
            int index = currentNavigationLevel - 1;
            int length = navCount - currentNavigationLevel + 1;
            NSRange range = new NSRange(index, length);
            log.debug("Range: {} current: {} navCount: {}", range, currentNavigationLevel, navCount);
            itmesToBeShown = state().subarrayWithRange(range);

        }
        log.debug("Nav state: {} current nav level: {} items: {}", state(), currentNavigationLevel, itmesToBeShown);
        return itmesToBeShown != null ? itmesToBeShown : NSArray.EmptyArray;
    }

    // Anytime we are setting the absolute we reset the relative.
    public void setState(NSArray navigationState) {
        log.debug("Setting Navigation State: {}", navigationState);
        _state = navigationState;
        _additionalState = null;
    }
    public void setStateWithString(String navigationStateString) {
        if (navigationStateString != null) {            
            setState(NSArray.componentsSeparatedByString(navigationStateString, "."));
        }
    }
    public String stateAsString() {
        if (state() != null) {
            return state().componentsJoinedByString(".");
        }
        return null;
    }

    public boolean isDisabled() { return isDisabled; }
    public void setIsDisabled(boolean newIsDisabled) { isDisabled = newIsDisabled; }

    public void setStateForLevel(String state, int level) {
        if (level > 0) {
            NSMutableArray navState = new NSMutableArray(state());
            if (navState.count() >= level)
                navState.replaceObjectAtIndex(state, level - 1);
            else
                navState.addObject(state);
            setState(navState);
        } else {
            log.error("Attempting to set the state: {} for a negative level: {}", state, level);
        }
    }

    public String level(int i) {
        return state() != null && i < state().count() ? (String)state().objectAtIndex(i) : "";
    }

    public NSArray navigationItemsForLevel(int level, NSKeyValueCodingAdditions context) {
        ERXNavigationItem levelRoot = null;
        if (level == 1) {
            levelRoot = ERXNavigationManager.manager().rootNavigationItem();
        } else if (state().count() > level - 2) {
            levelRoot = ERXNavigationManager.manager().navigationItemForName(level(level - 2));
            log.debug("Root name for level: {} state: {} root: {}", level - 2, state(),
                      (levelRoot != null ? levelRoot.name() : "<NULL>"));
        }
        NSArray children = null;
        if (levelRoot != null) {
            boolean hasChildrenConditions = levelRoot.childrenConditions().count() != 0;
            boolean meetsChildrenConditions = true;
            if (hasChildrenConditions) {
                for (Enumeration e = levelRoot.childrenConditions().objectEnumerator(); e.hasMoreElements();) {
                    String aCondition = (String)e.nextElement();
                    meetsChildrenConditions = ERXValueUtilities.booleanValue(context.valueForKeyPath(aCondition));
                    if (!meetsChildrenConditions)
                        break;
                }
            }
            if (meetsChildrenConditions) {// only want to do this if childrenConditions are met, or if there aren't any children conditions
                if (levelRoot.children() != null)
                    children = levelRoot.children();
                else if (levelRoot.childrenBinding() != null) {
                    Object o = context.valueForKeyPath(levelRoot.childrenBinding());
                    if (o != null && o instanceof NSArray)
                        children = (NSArray)o;
                    else if (o != null && o instanceof String) {
                        children = (NSArray)levelRoot.childrenChoices().objectForKey(o);
                        if (children == null)
                            log.warn("For nav core object: {} and child binding: {} couldn't find children for choice key: {}",
                                    levelRoot, levelRoot.childrenBinding(), o);
                    } else if (o instanceof Boolean) {
                    	String s = Boolean.toString((Boolean)o);
                    	children = (NSArray)levelRoot.childrenChoices().objectForKey(s);
                    } else {
                        log.warn("For nav core object: {} and child binding: {} recieved binding object: {}",
                                levelRoot, levelRoot.childrenBinding(), o);
                    }
                }
            }
        }
        if (children == null)
            children = NSArray.EmptyArray;
        if (children.count() > 0) {
            NSMutableArray childNavItems = new NSMutableArray();
            for (Enumeration e = children.objectEnumerator(); e.hasMoreElements();) {
                String childName = (String)e.nextElement();
                ERXNavigationItem item = ERXNavigationManager.manager().navigationItemForName(childName);
                if (item != null)
                    childNavItems.addObject(item);
                else
                    log.warn("Unable to find navigation item for name: {}", childName);
            }
            children = childNavItems;
        }
        return children;
    }

    @Override
    public String toString() {
        return "\"" + stateAsString() + "\"";
    }
}
