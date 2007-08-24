/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* ERXNavigation.java created by max on Thu 27-Jul-2000 */
package er.extensions;

import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSRange;

// FIXME: Alot of this needs to move up as it is specific to our navigation structure.
/**
 * Not very generic right now, but will be in the future. Nice for mantaining a
 * stack based navigation system, ie drilling down pushes nav state onto the
 * stack and backing up pops state off the stack.<br />
 * Please read "Documentation/Navigation.html" to fnd out how to use the
 * navigation components.
 * 
 */
public class ERXNavigation {

    /** logging support */
    public static Logger log = Logger.getLogger("er.navigation.extensions.ERXNavigation");

    protected NSArray _additionalNavigationState, _navigationState;
    protected boolean isDisabled;
    protected boolean _shouldDisplayHeaderTitle;

    // Methods for keeping track of current state
    public NSArray navigationState() {
        NSArray navigationState = (_navigationState == null) ? new NSArray() : _navigationState;
        if (_additionalNavigationState != null) {
            NSMutableArray combinedNavigationState = new NSMutableArray(_navigationState);
            combinedNavigationState.addObjectsFromArray(_additionalNavigationState);
            navigationState = combinedNavigationState;
        }
        return navigationState;
    }

    public void setAdditionalNavigationState(NSArray value) {
        if (log.isDebugEnabled())
            log.debug("Setting additional navigation state: " + value);
        _additionalNavigationState = value;
    }
    
    // Not used.
    public String sectionTitleString(int currentNavigationLevel) {
        String sectionTitle = "";
        int navCount = navigationState() != null ? navigationState().count() : 0;
        if (navCount > 0 && navCount <= currentNavigationLevel) {
            sectionTitle = (String)navigationState().lastObject();
        } else if (navCount != 0 && currentNavigationLevel < navCount) {
            NSRange range = new NSRange(currentNavigationLevel - 1, navCount - currentNavigationLevel + 1);
            sectionTitle = (navigationState().subarrayWithRange(range)).componentsJoinedByString(" > ");            
        }
        return sectionTitle;
    }

    // This will return all of the nav items to be shown for a current level, ie two nav items are light up
    // but we have four items in the nav array, this would return the last three.
    public NSArray navigationItemsToBeShownForLevel(int currentNavigationLevel) {
        NSArray itmesToBeShown = null;
        int navCount = navigationState() != null ? navigationState().count() : 0;
        if (currentNavigationLevel == 0) {
            itmesToBeShown = navigationState();
        } else if (navCount != 0 && navCount <= currentNavigationLevel) {
            itmesToBeShown = new NSArray(navigationState().lastObject());
        } else if (navCount != 0 && currentNavigationLevel < navCount) {
            int index = currentNavigationLevel - 1;
            int length = navCount - currentNavigationLevel + 1;
            NSRange range = new NSRange(index, length);
            log.debug("Range: " + range + " current: " + currentNavigationLevel + " navCount: " + navCount);
            itmesToBeShown = navigationState().subarrayWithRange(range);
            
        }
        log.debug("Nav state: " + navigationState() + " current nav level: " + currentNavigationLevel + " items: " + itmesToBeShown);
        return itmesToBeShown != null ? itmesToBeShown : NSArray.EmptyArray;
    }
    
    // Anytime we are setting the absolute we reset the relative.
    public void setNavigationState(NSArray navigationState) {
        if (log.isDebugEnabled())
            log.debug("Setting Navigation State: " + navigationState);
        _navigationState = navigationState;
        _additionalNavigationState = null;
    }

    public void setNavigationStateWithString(String navigationStateString) {
        if (navigationStateString != null) {
            NSMutableArray navigationState = new NSMutableArray();
            StringTokenizer navigationStateTokenizer = new StringTokenizer(navigationStateString, ".");
            while (navigationStateTokenizer.hasMoreTokens()) {
                navigationState.addObject(navigationStateTokenizer.nextToken());
            }
            setNavigationState(navigationState);
        }
    }

    public String navigationStateString() {
        if (navigationState() != null) {
            return navigationState().componentsJoinedByString(".");
        }
        return null;
    }

    public void disableAllComponents() { setIsDisabled(true); }
    public void enableAllComponents() { setIsDisabled(false); }
    
    public boolean isDisabled() { return isDisabled; }
    public void setIsDisabled(boolean newIsDisabled) { isDisabled = newIsDisabled; }
    
    public boolean shouldDisplayHeaderTitle() { return _shouldDisplayHeaderTitle; }
    public void setShouldDisplayHeaderTitle(boolean newShouldDisplayHeaderTitle) {
        _shouldDisplayHeaderTitle = newShouldDisplayHeaderTitle;
    }

    public String firstLevel() { return level(0); }
    public String secondLevel() { return level(1); }
    public String thirdLevel() { return level(2); }

    public void setNavigationLevel(int level, String state) {
        if (level > 0) {
            NSMutableArray navTemp = new NSMutableArray(navigationState());
            if (navTemp.count() >= level)
                navTemp.replaceObjectAtIndex(state, level - 1);
            else
                navTemp.addObject(state);
            setNavigationState(navTemp);
        }
    }

    public String level(int i) {
        return navigationState() != null && i < navigationState().count() ? (String)navigationState().objectAtIndex(i) : "";
    }
}
