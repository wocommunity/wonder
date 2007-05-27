package er.directtoweb;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WPage;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

/**
 * Displays a set of buttons and calls the enclosing page's branch delegate with it.
 * Its usable as an item in a repetition.
 *
 * @binding d2wContext the context for this component 
 *
 * @created ak on Sun Jan 26 2003
 * @project ERExtras
 */

public class ERDActionBar extends ERDCustomEditComponent implements ERDBranchInterface {

    /** logging support */
    private static final Logger log = Logger.getLogger(ERDActionBar.class);

    /**
     * Public constructor
     * @param context the context
     */
    public ERDActionBar(WOContext context) {
        super(context);
    }

    /** component does not synchronize it's variables */
    public boolean synchronizesVariablesWithBindings() { return false; }
    public boolean isStateless() { return true; }

    /** find the next non-null NextPageDelegate in the component tree, break if there is a D2WPage found beforehand */
    public ERDBranchDelegateInterface branchDelegate() {
        if(branchDelegate == null) {
            WOComponent current = parent();
            while(current != null) {
                if((current instanceof D2WPage) &&
                   ((D2WPage)current).nextPageDelegate() instanceof ERDBranchDelegateInterface) {
                    branchDelegate = (ERDBranchDelegateInterface)((D2WPage)current).nextPageDelegate();
                }
                current = current.parent();
            }
        }
        return branchDelegate;
    }

    public WOComponent nextPageFromParent() {
        if(branchDelegate() == null)
            return null;
        return branchDelegate().nextPage(this);
    }

    /** override this */
    public WOComponent performAction() {
        return nextPageFromParent();
    }
    public void reset() {
        super.reset();
        branch = null;
        branchChoices = null;
        branchDelegate = null;
    }

    //---------------- Branch Delegate Support --------------------//
    /** holds the chosen branch */
    protected NSDictionary branch;
    protected NSArray branchChoices;
    protected ERDBranchDelegateInterface branchDelegate;

    /**
        * Cover method for getting the choosen branch.
     * @return user choosen branch.
     */
    public NSDictionary branch() {
        return branch;
    }

    /**
        * Sets the user choosen branch.
     * @param branch choosen by user.
     */
    public void setBranch(NSDictionary value) {
        branch = value;
    }

    /**
     * Implementation of the {@link ERDBranchDelegate ERDBranchDelegate}.
     * Gets the user selected branch name.
     * @return user selected branch name.
     */
    public String branchName() { return (String)branch().valueForKey("branchName"); }

    /**
     * Implementation of the {@link ERDBranchDelegate ERDBranchDelegate}.
     * Gets the user selected branch name.
     * @return user selected branch name.
     */
    public String branchButtonLabel() { return (String)branch().valueForKey("branchButtonLabel"); }

    /**
     * Calculates the branch choices for the current
     * poage. This method is just a cover for calling
     * the method <code>branchChoicesForContext</code>
     * on the current {@link ERDBranchDelegate ERDBranchDelegate}.
     * @return array of branch choices
     */

    public NSArray branchChoices() {
        if (branchDelegate() != null) {
            branchChoices = branchDelegate().branchChoicesForContext(d2wContext());
        } else {
            branchChoices = NSArray.EmptyArray;
        }
        return branchChoices;
    }

    /**
        * Determines if this message page should display branch choices.
     * @return if the current delegate supports branch choices.
     */
    public boolean hasBranchChoices() {
        return branchDelegate() != null && branchChoices().count() > 0;
    }

    public void validationFailedWithException(Throwable theException,Object theValue, String theKeyPath) {
        parent().validationFailedWithException(theException, theValue, theKeyPath);
        log.info("" + theException + theValue + theKeyPath);
    }
}
