//
// ERXNavigationMenu.java: Class file for WO Component 'ERXNavigationMenu'
// Project ERExtensions
//
// Created by max on Wed Oct 30 2002
//
package er.extensions;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;

/** Please read "Documentation/Navigation.html" to find out how to use the navigation components.*/

public class ERXNavigationMenu extends ERXStatelessComponent {

    /** logging support */
    public static final Logger log = Logger.getLogger(ERXNavigationMenu.class);
    
    public ERXNavigationItem aNavigationItem;

    protected ERXNavigationState _navigationState;
    protected NSKeyValueCoding _navigationContext;
    
    protected NSArray _level1Items;
    protected NSArray _level2Items;
    protected NSArray _level3Items;
    public int _level1SpacerWidth=0;
    public int _level2SpacerWidth=0;
    public int _level3SpacerWidth=0;
    public int _renderLevelCount;
    protected int l2Colspan;

    protected boolean _menuIsSetUp=false;    

    public ERXNavigationMenu(WOContext context) {
        super(context);
    }

    public void reset() {
        _level1Items=null;
        _level2Items=null;
        _level3Items=null;
        _menuIsSetUp=false;
        _renderLevelCount=0;
        aNavigationItem = null;
        _navigationState = null;
        _navigationContext = null;
        super.reset();
    }

    protected NSKeyValueCoding navigationContext() {
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

            // init numOfLevels
            int numOfLevels = menuLevelsToShow();
            
            if (log.isDebugEnabled())
                log.debug("Number of levels: " + numOfLevels);
            
            //set the values in the arrays
            setLevel1Items(itemsForLevel(1));
            setLevel1SpacerWidth(setupLevel1SpacerWidth());
            if (numOfLevels >= 2) {
                setLevel2Items(itemsForLevel(2));
                setLevel2SpacerWidth(setupLevel2SpacerWidth());
                // For additional nav situation
                if (numOfLevels >= 3) {
                    setLevel3Items(itemsForLevel(3));
                    setLevel3SpacerWidth(setupLevel3SpacerWidth());
                }
            }
            _menuIsSetUp=true;
        }
    }

    public ERXNavigationState navigationState() {
        if (_navigationState == null)
            _navigationState = ERXNavigationManager.manager().navigationStateForSession(session());
        return _navigationState;
    }
    
    public NSArray itemsForLevel(int level) {
        NSArray children = navigationState().navigationItemsForLevel(level, this);
        if (log.isDebugEnabled())
            log.debug("Children: " + children.count() + " for level: " + level);
        if (children.count() > 0)
            _renderLevelCount++;
        return children;
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
        WOActionResults results = null;
        setUpMenu();
        try {
            results = super.invokeAction(r,c);
        } catch (RuntimeException e) {
            // Might need to ignore rapid clicks, catch some type of IllegalArgumentException
            throw e;
        }
        return results;
    }

    public int setupLevel2SpacerWidth() {
        //String tmpString = new String();
        int totalWidth = 0;
        int lev1Width = level1Width();
        int lev2Width = level2Width();
        int i;

        for (i=0; i < level1Items().count(); i++) {
            ERXNavigationItem anObject = (ERXNavigationItem)level1Items().objectAtIndex(i);
            /* code to act on each element */
            //tmpString = (String)anObject.componentPath();
            int tmpInt = anObject.width();
            totalWidth = totalWidth + tmpInt;

            if (navigationState().state().containsObject(anObject.name())) {
                // we need to stop going through the objects here
                if (lev2Width <= (totalWidth + level1SpacerWidth())) {

                    // it will fit aligned right
                    if ((i + 1) == level1Items().count()) {
                        return 0;
                    }
                    return (lev1Width - totalWidth);
                }
                if (lev2Width < (lev1Width - (totalWidth - tmpInt))) {
                    //it will fit aligned left
                    totalWidth = totalWidth - tmpInt;
                    return ((lev1Width - totalWidth) - lev2Width);
                }
            }
        }
        return (lev1Width - lev2Width);
    }

    public int setupLevel3SpacerWidth() {
        //String tmpString = new String();
        int totalWidth = 0;
        int lev2Width = level2SpacerWidth() + level2Width();
        int lev3Width = level3Width();
        int i;

        for (i=0; i < _level2Items.count(); i++) {
            ERXNavigationItem anObject = (ERXNavigationItem)_level2Items.objectAtIndex(i);
            /* code to act on each element */
            //tmpString = (String)anObject.componentPath();
            int tmpInt=anObject.width();
            totalWidth = totalWidth + tmpInt;
            if (navigationState().state().containsObject(anObject.name())) {
                // we need to stop going through the objects here
                //for aligning left, which is our first choice here
                totalWidth = totalWidth - tmpInt;
                if (lev3Width <= (lev2Width - totalWidth)) {
                    return (lev2Width - totalWidth) - lev3Width;
                }
                // for aligning right
                if (lev3Width < (level1Width() - (totalWidth + tmpInt))) {
                    totalWidth = totalWidth + tmpInt;
                    return (lev2Width - totalWidth);
                }
            }
        }
        return 0;
    }

    public boolean showLevel2() {
        return navigationState().stateAsString() != null  ?
            (navigationState().state().count()>=1 && _level2Items != null && _level2Items.count()>0) : false;
    }

    public boolean showLevel3() {
        // In theory we could have other stuff at level 3, but for now we don't..
        //  return _level3Items != null && _level3Items.count()>0;
        return navigationState().stateAsString() != null &&
            navigationState().state().count()>=2 &&
            _level3Items != null &&
            _level3Items.count()>0;
    }

    public int menuLevelsToShow() {
        NSArray tmpArray = navigationState().state();
        int result = tmpArray.count() + (tmpArray.count() <= 2 ? 1 : 0);
        return result;
    }

    public NSArray level1Items() { return _level1Items; }

    public int level1SpacerWidth() { return _level1SpacerWidth; }

    public int level1Width() {
        int totalWidth = 0;
        if (_level1Items != null) {
            for (int i=0; i < _level1Items.count(); i++) {
                totalWidth += ((ERXNavigationItem)_level1Items.objectAtIndex(i)).width();
            }
        }
        return totalWidth;
    }

    public NSArray level2Items() { return _level2Items; }

    public int level2SpacerWidth() { return _level2SpacerWidth; }

    public int level2Width() {
        int totalWidth = 0;
        if (_level2Items != null) {
            for (int i=0; i < _level2Items.count(); i++) {
                totalWidth += ((ERXNavigationItem)_level2Items.objectAtIndex(i)).width();
            }
        }
        return totalWidth;
    }

    public NSArray level3Items() { return _level3Items; }

    public int level3SpacerWidth() { return _level3SpacerWidth; }

    public int level3Width() {
        int totalWidth = 0;
        if (_level3Items != null) {
            for (int i=0; i < _level3Items.count(); i++) {
                totalWidth += ((ERXNavigationItem)_level3Items.objectAtIndex(i)).width();
            }
        }
        return totalWidth;
    }

    public int setupLevel1SpacerWidth() {
        int level1Width = level1Width();
        // If we're not using widths in our items, the width will be 0, and we don't want to force a minimum width.
        return level1Width == 0 ? 0 : navItemsTableWidth() - level1Width;
    }

    public void setLevel1Items(NSArray newLevel1Items) { _level1Items = newLevel1Items; }
    public void setLevel1SpacerWidth(int newLevel1SpacerWidth) { _level1SpacerWidth = newLevel1SpacerWidth; }
    public void setLevel2Items(NSArray newLevel2Items) { _level2Items = newLevel2Items; }
    public void setLevel2SpacerWidth(int newLevel2SpacerWidth) { _level2SpacerWidth = newLevel2SpacerWidth; }
    public void setLevel3Items(NSArray newLevel3Items) { _level3Items = newLevel3Items; }
    public void setLevel3SpacerWidth(int newLevel3SpacerWidth) { _level3SpacerWidth = newLevel3SpacerWidth; }

    public int navItemsTableWidth() {
        int level1Width=level1Width();
        return level1Width < 200 ? 200 : level1Width;
    }

    public String paddingWidth() { return ((ERXSession)session()).browser().isNetscape() ? "width=\"100%\"" : ""; }

}
