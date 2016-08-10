package er.directtoweb.components.buttons;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.directtoweb.components.ERDActionBar;
import er.directtoweb.delegates.ERDBranchDelegate;
import er.directtoweb.delegates.ERDBranchDelegateInterface;
import er.directtoweb.delegates.ERDBranchInterface;
import er.directtoweb.pages.ERD2WPage;
import er.extensions.components._private.ERXSubmitButton;
/**
 * Action button that looks for the inner-most page with a pageController (which must be
 * a ERDBranchDelegateInterface), collects all the actions from there 
 * and displays them as a menu with an activation button.
 * @author ak
 */
public class ERDControllerButton extends ERDActionButton implements ERDBranchInterface {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    private static final Logger log = Logger.getLogger(ERDActionBar.class);

    public ERDControllerButton(WOContext context) {
        super(context);
    }

    public boolean isFlyOver() {
        return !("linkList".equals(valueForBinding("controllerButtonUIStyle")) || isButton());
    }
    public boolean isButton() {
        return "buttonList".equals(valueForBinding("controllerButtonUIStyle"));
    }
    
    public String cssForChoice() {
    	String css = (String) branch.objectForKey("branchClass");
    	if(css == null) {
    		css = (String)valueForBinding("branchClass");
    		css = css != null ? css  : "";
    	}
    	css += " " + ERXSubmitButton.STYLE_PREFIX + branch.objectForKey("branchName");
    	if(css.length() ==0 ) {
    		css = null;
    	}
    	return css;
    }
    
    /** find the page controller of the closest D2WPage in the component tree */
    public ERDBranchDelegateInterface branchDelegate() {
        if(branchDelegate == null) {
            WOComponent current = parent();
            while(current != null) {
                if(current instanceof ERD2WPage) {
                    ERD2WPage page = (ERD2WPage)current;
                    branchDelegate = page.pageController();
                    return branchDelegate;
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
     * Sets the user chosen branch.
     * @param value branch chosen by user.
     */
    public void setBranch(NSDictionary value) {
        branch = value;
    }

    /**
     * Implementation of the {@link ERDBranchDelegate ERDBranchDelegate}.
     * Gets the user selected branch name.
     * @return user selected branch name.
     */
    public String branchName() { return (String)branch().valueForKey(ERDBranchDelegate.BRANCH_NAME); }

    /**
     * Implementation of the {@link ERDBranchDelegate ERDBranchDelegate}.
     * Gets the user selected branch name.
     * @return user selected branch name.
     */
    public String branchButtonLabel() { return (String)branch().valueForKey(ERDBranchDelegate.BRANCH_LABEL); }

    /**
     * Calculates the branch choices for the current
     * page. This method is just a cover for calling
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

    @Override
    public void validationFailedWithException(Throwable theException,Object theValue, String theKeyPath) {
        parent().validationFailedWithException(theException, theValue, theKeyPath);
        log.info("" + theException + theValue + theKeyPath);
    }

    public String imageName() {
        return hasBranchChoices() ?  "controller.gif" : "controller_disabled.gif";
    }
}
