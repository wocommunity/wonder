//
//  ERXNavigationManager.java
//  ERExtensions
//
//  Created by Max Muller on Wed Oct 30 2002.
//
package er.extensions;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOSession;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation._NSUtilities;

/** Please read "Documentation/Navigation.html" to find out how to use the navigation components.*/
public class ERXNavigationManager {

    /** logging support */
    public static final Logger log = Logger.getLogger(ERXNavigationManager.class);
    
    protected static ERXNavigationManager manager;

    /**
     * Gets the shared instance of the ERXNavigationManager.
     * @return the ERXNavigationManager shared instance.
     */
    public static ERXNavigationManager manager() {
        if (manager == null)
            manager = new ERXNavigationManager();
        return manager;
    }

    protected NSDictionary navigationItemsByName = NSDictionary.EmptyDictionary;
    protected ERXNavigationItem rootNavigationItem;
    protected String navigationMenuFileName;
    protected boolean hasRegistered = false;

    /**
     * Gets the navigation state for the provided session.
     * @param session from which the navigation state is read
     * @return an ERXNavigationState object reflecting the navigation state of the session.
     */
    public ERXNavigationState navigationStateForSession(WOSession session) {
        ERXNavigationState state = (ERXNavigationState)session.objectForKey(navigationStateSessionKey());
        if (state == null) {
            state = new ERXNavigationState();
            session.setObjectForKey(state, navigationStateSessionKey());
        }
        return state;
    }

    /**
     * Gets the name of the member variable that holds the session's navigation state: <code>NavigationState</code>.
     * This is the key name that will be invoked when the navigation manager reads or sets the state for the session.
     * Your session MUST have this key in order to work with ERXNavigation classes.
     * @return the navigation state key name
     */
    public String navigationStateSessionKey() {
        return "NavigationState";
    }

    /**
     * Gets the file name of the file containing the navigation menu plist.  The name is determined by reading the
     * System property: <code>er.extensions.ERXNavigationManager.NavigationMenuFileName</code>
     * @return the file name
     */
    public String navigationMenuFileName() {
        if (navigationMenuFileName == null) {
            navigationMenuFileName = System.getProperty("er.extensions.ERXNavigationManager.NavigationMenuFileName");
        }
        return navigationMenuFileName;
    }

    /**
     * Sets the name of the file holding containing the navigation menu plist.
     * @param name of the file holding the menu plist
     */
    public void setNavigationMenuFileName(String name) {
        navigationMenuFileName = name;
    }

    /**
     * Gets a dictionary of the navigation menu items, keyed by navigation menu item name
     * @return a dictionary of menu items, keyed by name
     */
    public NSDictionary navigationItemsByName() {
        return navigationItemsByName;
    }

    /**
     * Gets the <code>root</code> navigation menu item.  Note that thhe root item is never actually rendered into the
     * menu, but serves as the root of the tree of navigation menu items.
     * @return the root navigation menu item.
     */
    public ERXNavigationItem rootNavigationItem() {
        return rootNavigationItem;
    }

    /**
     * Gets the navigation item whose name matches the provided <code>name</code> argument.
     * @param name of the item to retrieve
     * @return the navigation item with the provided name, if found.
     */
    public ERXNavigationItem navigationItemForName(String name) {
        return (ERXNavigationItem)navigationItemsByName.objectForKey(name);
    }

    /**
     * Sets the navigation items in the navigation menu.  Will create the menu of items from the provided items array.
     * Items are stored in a dictionary, keyed by the name of the item.  In case of collision, the first entry is
     * preserved.
     * @param items to use in the menu
     */
    protected void setNavigationItems(NSArray items) {
        NSMutableDictionary itemsByName = new NSMutableDictionary();
        if (items != null && items.count() > 0) {
            for (Enumeration e = items.objectEnumerator(); e.hasMoreElements();) {
                ERXNavigationItem item = (ERXNavigationItem)e.nextElement();
                if (itemsByName.objectForKey(item.name()) != null) {
                    log.warn("Attempting to register multiple navigation items for the same name: " + item.name());
                } else {
                    itemsByName.setObjectForKey(item, item.name());
                    if (item.name().equals("Root"))
                        rootNavigationItem = item;
                }
            }
        }
        if (rootNavigationItem == null)
            log.warn("No root navigation item set. You need one.");
        navigationItemsByName = itemsByName.immutableClone();
    }

    /**
     * Configures the navigation. Loads the menu items and sets up the menu.
     */
    public void configureNavigation() {
        loadNavigationMenu();
        hasRegistered = true;
    }

    /**
     * Loads the navigation menu from a plist file in the application bundle and then from each of the loaded frameworks.
     * Creates the menu from the loaded items.
     */
    public void loadNavigationMenu() {
        NSMutableArray navigationMenus = new NSMutableArray();
        // First load the nav_menu from application.
        NSArray appNavigationMenu = (NSArray)ERXExtensions.readPropertyListFromFileinFramework(navigationMenuFileName(),null);
        if (appNavigationMenu != null) {
            if (log.isDebugEnabled())
                log.debug("Found navigation menu in application: " + WOApplication.application().name());
            navigationMenus.addObjectsFromArray(createNavigationItemsFromDictionaries(appNavigationMenu));
            registerObserverForFramework(null);
        }
        for (Enumeration e = ERXUtilities.allFrameworkNames().objectEnumerator(); e.hasMoreElements();) {
            String frameworkName = (String)e.nextElement();
            NSArray aNavigationMenu = (NSArray)ERXExtensions.readPropertyListFromFileinFramework(navigationMenuFileName(), frameworkName);
            if (aNavigationMenu != null && aNavigationMenu.count() > 0) {
                if (log.isDebugEnabled()) log.debug("Found navigation menu in framework: " + frameworkName);
                navigationMenus.addObjectsFromArray(createNavigationItemsFromDictionaries(aNavigationMenu));
                registerObserverForFramework(frameworkName);
            }
        }
        setNavigationItems(navigationMenus);
        if (log.isDebugEnabled())
            log.debug("Navigation Menu Configured");
    }

    /**
     * Registers a file observer for the navigation menu file in the named framework.  Use <code>null</code> to observe
     * the application bundle.  When the file changes, the navigation manager receives the <code>reloadNavigationMenu</code>
     * notification message.
     * @param frameworkName of the framework to observe
     */
    public void registerObserverForFramework(String frameworkName) {
        if (!WOApplication.application().isCachingEnabled() && !hasRegistered) {
            String filePath = ERXFileUtilities.pathForResourceNamed(navigationMenuFileName(), frameworkName, null);
            if (log.isDebugEnabled())
                log.debug("Registering observer for filePath: " + filePath);
            ERXFileNotificationCenter.defaultCenter().addObserver(this,
                                                                  new NSSelector("reloadNavigationMenu", ERXConstant.NotificationClassArray),
                                                                  filePath);
        }
    }

    /**
     * Creates a new navigation item from the provided configuration dictionary.  Use the key <code>navigationItemClassName</code>
     * to override the class that will be created for the navigation item, which defaults to {@link ERXNavigationItem}.
     * @param dict containing the configuration for the new navigation item
     * @return a navigation item
     */
    public ERXNavigationItem newNavigationItem(NSDictionary dict) {
    	String className = (String) dict.objectForKey("navigationItemClassName");
    	if(className != null) {
    		Class c = ERXPatcher.classForName(className);
    		return (ERXNavigationItem) _NSUtilities.instantiateObject(c, new Class[] {NSDictionary.class}, new Object[]{dict}, true, true);
    	}
    	return new ERXNavigationItem(dict);
    }
    
    /**
     * Creates navigation menu items from the array of dictionaries that represent the configuration of the navigation
     * menu items in the navigation configuration file.
     * @param navItems read (as NSDictionaries) from the configuration file
     * @return an array of navigation items from the provided array of dictionariesseraph
     * 
     */
    protected NSArray createNavigationItemsFromDictionaries(NSArray navItems) {
        NSMutableArray navigationItems = null;
        if (navItems != null && navItems.count() > 0) {
            navigationItems = new NSMutableArray();
            for (Enumeration e = navItems.objectEnumerator(); e.hasMoreElements();) {
                navigationItems.addObject(newNavigationItem((NSDictionary)e.nextElement()));
            }
        }
        return navigationItems != null ? navigationItems : NSArray.EmptyArray;
    }

    /**
     * Causes the navigation mangaer to reload its menu items.
     * @param notification that the menu items file has changed
     */
    public void reloadNavigationMenu(NSNotification notification) {
        log.info("Reloading Navigation Menu");
        loadNavigationMenu();
    }
}
