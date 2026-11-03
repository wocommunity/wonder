package er.directtoweb.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WPage;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.directtoweb.delegates.ERDBranchDelegate;
import er.directtoweb.delegates.ERDBranchDelegateInterface;
import er.directtoweb.delegates.ERDBranchInterface;
import er.extensions.security.ERXAccessPermission;

/**
 * Displays a set of buttons and calls the enclosing page's branch delegate with it.
 * Its usable as an item in a repetition.
 *
 * @binding d2wContext the context for this component 
 *
 * @author ak on Sun Jan 26 2003
 */
public class ERDActionBar extends ERDCustomEditComponent implements ERDBranchInterface {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    private static final Logger log = LoggerFactory.getLogger(ERDActionBar.class);

    /**
     * Public constructor
     * @param context the context
     */
    public ERDActionBar(WOContext context) {
        super(context);
    }

    /** component does not synchronize it's variables */
    @Override
    public boolean synchronizesVariablesWithBindings() { return false; }
    @Override
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

    @Override
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
     * @param value branch choosen by user.
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
     * 
     * @return if the current delegate supports branch choices.
     */
    public boolean hasBranchChoices() {
        return branchDelegate() != null && branchChoices().count() > 0;
    }

    @Override
    public void validationFailedWithException(Throwable theException,Object theValue, String theKeyPath) {
        parent().validationFailedWithException(theException, theValue, theKeyPath);
        if(log.isInfoEnabled())
          log.info("" + theException + theValue + theKeyPath);
    }
    
    /**
     * <span class="en">
     * Before Display the Button it will check the Permission. If no Permission are set it
     * will return true, and Display the Button. (Default)
     * 
     * Sample:
     * <code>public WOComponent copyOnlineToWork(WOComponent sender)</code>
     * 
     * Access Permission Key : <code>Delegate.copyOnlineToWork</code>
     * </span>
     * 
     * <span class="ja">
     * ボタンを表示する前にアクセス権限をチェックします。アクセス権限がなければ、そのままで true として実行します。
     * 
     * 例：
     * <code>public WOComponent copyOnlineToWork(WOComponent sender)</code>
     * 
     * アクセス権限キー： <code>Delegate.copyOnlineToWork</code>
     * </span>
     * 
     * @author ishimoto
     */
    public boolean isDelegateAllowed() {
      return ERXAccessPermission.instance().canWithDefault("Delegate." + branchName(), true);
    }
}
