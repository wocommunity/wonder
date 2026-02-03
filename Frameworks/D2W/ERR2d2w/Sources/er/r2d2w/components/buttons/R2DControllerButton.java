package er.r2d2w.components.buttons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.directtoweb.components.buttons.ERDActionButton;
import er.directtoweb.delegates.ERDBranchDelegate;
import er.directtoweb.delegates.ERDBranchDelegateInterface;
import er.directtoweb.delegates.ERDBranchInterface;
import er.directtoweb.pages.ERD2WPage;

public abstract class R2DControllerButton extends ERDActionButton implements ERDBranchInterface {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(R2DControllerButton.class);

	/** holds the chosen branch */
	protected NSDictionary<String,String> branch;
	protected NSArray<NSDictionary<String,String>> branchChoices;
	protected ERDBranchDelegateInterface branchDelegate;

	public R2DControllerButton(WOContext context) {
		super(context);
	}

	/** find the page controller of the closest D2WPage in the component tree */
	public ERDBranchDelegateInterface branchDelegate() {
		if (branchDelegate == null) {
			WOComponent current = parent();
			while (current != null && branchDelegate == null) {
				if (current instanceof ERD2WPage) {
					ERD2WPage page = (ERD2WPage) current;
					branchDelegate = page.pageController();
				}
				current = current.parent();
			}
		}
		return branchDelegate;
	}

	public WOComponent nextPageFromParent() {
		if (branchDelegate() == null) {
			return null;
		}
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

	/**
	 * Cover method for getting the chosen branch.
	 * 
	 * @return user chosen branch.
	 */
	public NSDictionary<String,String> branch() {
		return branch;
	}

	/**
	 * Sets the user chosen branch.
	 * 
	 * @param value
	 *            branch chosen by user.
	 */
	public void setBranch(NSDictionary<String,String> value) {
		branch = value;
		d2wContext().takeValueForKey(value, "branch");
	}

	/**
	 * Implementation of the {@link ERDBranchDelegate ERDBranchDelegate}. Gets
	 * the user selected branch name.
	 * 
	 * @return user selected branch name.
	 */
	public String branchName() {
		return branch().objectForKey(ERDBranchDelegate.BRANCH_NAME);
	}

	/**
	 * Implementation of the {@link ERDBranchDelegate ERDBranchDelegate}. Gets
	 * the user selected branch name.
	 * 
	 * @return user selected branch name.
	 */
	public String branchButtonLabel() {
		return branch().objectForKey(ERDBranchDelegate.BRANCH_LABEL);
	}

	/**
	 * Calculates the branch choices for the current page. This method is just
	 * a cover for calling the method <code>branchChoicesForContext</code> on
	 * the current {@link ERDBranchDelegate ERDBranchDelegate}.
	 * 
	 * @return array of branch choices
	 */
	@SuppressWarnings("unchecked")
	public NSArray<NSDictionary<String,String>> branchChoices() {
		if (branchDelegate() != null) {
			branchChoices = branchDelegate().branchChoicesForContext(d2wContext());
		} else {
			branchChoices = NSArray.emptyArray();
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

	public void validationFailedWithException(Throwable theException, Object theValue, String theKeyPath) {
		parent().validationFailedWithException(theException, theValue, theKeyPath);
		log.info("" + theException + theValue + theKeyPath);
	}

}
