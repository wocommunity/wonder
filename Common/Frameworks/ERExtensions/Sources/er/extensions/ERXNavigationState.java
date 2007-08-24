//
//  ERXNavigationState.java
//  ERExtensions
//
//  Created by Max Muller on Wed Oct 30 2002.
//
package er.extensions;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSRange;

/** Please read "Documentation/Navigation.html" to find out how to use the navigation components.*/
public class ERXNavigationState {

    /** logging support */
    public static final Logger log = Logger.getLogger(ERXNavigationState.class);
    
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
        if (log.isDebugEnabled())
            log.debug("Setting additional navigation state: " + value);
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
            log.debug("Range: " + range + " current: " + currentNavigationLevel + " navCount: " + navCount);
            itmesToBeShown = state().subarrayWithRange(range);

        }
        log.debug("Nav state: " + state() + " current nav level: " + currentNavigationLevel + " items: " + itmesToBeShown);
        return itmesToBeShown != null ? itmesToBeShown : NSArray.EmptyArray;
    }

    // Anytime we are setting the absolute we reset the relative.
    public void setState(NSArray navigationState) {
        if (log.isDebugEnabled())
            log.debug("Setting Navigation State: " + navigationState);
        _state = navigationState;
        _additionalState = null;
    }
    public void setStateWithString(String navigationStateString) {
        if (navigationStateString != null) {            
            setState(NSArray.componentsSeparatedByString(navigationStateString, "."));
        }
    }
    public String stateAsString() {
        if (this.state() != null) {
            return this.state().componentsJoinedByString(".");
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
            log.error("Attempting to set the state: " + state + " for a negative level: " + level);
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
            if (log.isDebugEnabled())
                log.debug("Root name for level: " + (level - 2) + " state: " + state() + "root: "
                          + (levelRoot != null ? levelRoot.name() : "<NULL>"));
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
                        children = (NSArray)levelRoot.childrenChoices().objectForKey((String)o);
                        if (children == null)
                            log.warn("For nav core object: " + levelRoot + " and child binding: " + levelRoot.childrenBinding()
                                     + " couldn't find children for choice key: " + o);
                    } else {
                        log.warn("For nav core object: " + levelRoot + " and child binding: " + levelRoot.childrenBinding()
                                 + " recieved binding object: " + o);
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
                    log.warn("Unable to find navigation item for name: " + childName);
            }
            children = childNavItems;
        }
        return children;
    }
}
