package er.directtoweb.assignments.delayed;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOSession;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;

import er.extensions.appserver.navigation.ERXNavigationItem;
import er.extensions.appserver.navigation.ERXNavigationManager;

/**
 * When using ERXNavigationMenu, this automatically sets the current navigation
 * state via a low priority rule. If the user clicked a link in the navigation
 * menu, the URL can be reliably matched via a dictionary of navigation states
 * and URLs. When another link was clicked (e.g. "Find" on a query page,
 * "Cancel" while editing), it looks at the current task and entity name in
 * order to find a matching navigation item. It assumes your navigation elements
 * are named according to the following conventions:
 * <ul>
 * <li>task=list, entity.name=Movie => 'ListMovie' or 'SearchMovie'
 * <li>task=query, entity.name=Movie => 'SearchMovie'
 * </ul>
 * You may still have to define a rule for the initial navigation state after
 * login, if it cannot be resolved with the simple conventions above. <br>
 * <br>
 * While it should set the vast majority of navigation states correctly, you can
 * override the default choice via a rule such as:
 * <pre>
 * 100 : pageConfiguration = 'ListMovie' => navigationState = "Movies.ListMovie" [com.webobjects.directtoweb.Assignment]
 * </pre>
 * 
 * In the same way, you can disable it entirely, e.g. via:
 * <pre>
 * 50 : *true* => navigationState = (null) [com.webobjects.directtoweb.Assignment]
 * </pre>
 * 
 * @author fpeters
 */
public class ERDDelayedNavigationStateAssignment extends ERDDelayedAssignment {

    private static final long serialVersionUID = 1L;

    /** logging support */
    static final Logger log = Logger.getLogger(ERDDelayedNavigationStateAssignment.class);

    /**
     * Static constructor required by the EOKeyValueUnarchiver interface. If
     * this isn't implemented then the default behavior is to construct the
     * first super class that does implement this method. Very lame.
     * 
     * @param eokeyvalueunarchiver
     *            to be unarchived
     * @return decoded assignment of this class
     */
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver) {
        return new ERDDelayedNavigationStateAssignment(eokeyvalueunarchiver);
    }

    /**
     * Public constructor
     * 
     * @param u
     *            key-value unarchiver used when unarchiving from rule files.
     */
    public ERDDelayedNavigationStateAssignment(EOKeyValueUnarchiver u) {
        super(u);
    }

    /**
     * Public constructor
     * 
     * @param key
     *            context key
     * @param value
     *            of the assignment
     */
    public ERDDelayedNavigationStateAssignment(String key, Object value) {
        super(key, value);
    }

    @Override
    public Object fireNow(D2WContext c) {
        String navigationState = ERXNavigationManager.manager().navigationStateFromMap(
                (WOSession) c.valueForKey("session"));
        if (navigationState == null) {
            if (log.isDebugEnabled()) {
                log.debug("Could not determine navigation state from menu action dictionary. "
                        + "We'll now try to find a matching default.");
            }
            if ("list".equals(c.task())) {
                // we try to set a sensible default for a list page
                navigationState = "List" + c.entity().name();
                ERXNavigationItem item = ERXNavigationManager.manager()
                        .navigationItemForName(navigationState);
                // ListFoo not defined, let's try SearchFoo
                if (item == null) {
                    navigationState = "Search" + c.entity().name();
                    item = ERXNavigationManager.manager().navigationItemForName(
                            navigationState);
                }
                if (item != null) {
                    navigationState = item.navigationPath().replace('/', '.');
                }
            } else if ("query".equals(c.task())) {
                // we try to set a sensible default for a query page
                navigationState = "Search" + c.entity().name();
                ERXNavigationItem item = ERXNavigationManager.manager()
                        .navigationItemForName(navigationState);
                if (item != null) {
                    navigationState = item.navigationPath().replace('/', '.');
                }
            }
        }
        if (log.isDebugEnabled()) {
            if (navigationState == null) {
                log.debug("Failed to resolve navigation state for task " + c.task()
                        + " and entity name " + c.entity().name() + ".");
            } else {
                log.debug("Resolved navigation state to " + navigationState);
            }
        }
        return navigationState;
    }
}
