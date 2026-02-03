package er.r2d2w.components.buttons;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.directtoweb.components.ERDCustomComponent;
import er.directtoweb.delegates.ERDBranchDelegate;
import er.directtoweb.delegates.ERDBranchDelegateInterface;
import er.directtoweb.delegates.ERDBranchInterface;
import er.directtoweb.pages.ERD2WPage;
import er.extensions.foundation.ERXStringUtilities;

public class R2DControllerPopupButton extends ERDCustomComponent implements ERDBranchInterface {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    protected NSArray<NSDictionary<String, Object>> branchChoices;
    protected ERDBranchDelegateInterface branchDelegate;
	protected NSDictionary<String, Object> selectedBranch;
	private String labelID;
	
	public R2DControllerPopupButton(WOContext context) {
        super(context);
    }
	
	/**
	 * @return the selectedBranch
	 */
	public NSDictionary<String, Object> selectedBranch() {
		return selectedBranch;
	}

	/**
	 * @param selectedBranch the selectedBranch to set
	 */
	public void setSelectedBranch(NSDictionary<String, Object> selectedBranch) {
		this.selectedBranch = selectedBranch;
	}
	
    /**
     * Implementation of the {@link ERDBranchDelegate ERDBranchDelegate}.
     * Gets the user selected branch name.
     * @return user selected branch name.
     */
    public String branchName() {
    	if(selectedBranch() == null) { return null; }
    	return (String)selectedBranch().valueForKey(ERDBranchDelegate.BRANCH_NAME);
    }

    /** find the next non-null NextPageDelegate in the component tree, break if there is a D2WPage found beforehand */
    public ERDBranchDelegateInterface branchDelegate() {
        if(branchDelegate == null) {
            WOComponent current = parent();
            while(current != null && branchDelegate == null) {
                if(current instanceof ERD2WPage) {
                    ERD2WPage page = (ERD2WPage)current;
                    branchDelegate = page.pageController();
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

    /**
     * Calculates the branch choices for the current
     * page. This method is just a cover for calling
     * the method <code>branchChoicesForContext</code>
     * on the current {@link ERDBranchDelegate ERDBranchDelegate}.
     * @return array of branch choices
     */
    @SuppressWarnings("unchecked")
	public NSArray<NSDictionary<String, Object>> branchChoices() {
        if (branchDelegate() != null) {
            branchChoices = branchDelegate().branchChoicesForContext(d2wContext());
        } else {
            branchChoices = NSArray.emptyArray();
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

	/**
	 * @return the labelID
	 */
	public String labelID() {
		if(labelID == null) {
			labelID = ERXStringUtilities.safeIdentifierName(context().elementID(), "id");
		}
		return labelID;
	}

    public boolean synchronizesVariablesWithBindings() { return false; }

}