package er.r2d2w.pages;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.directtoweb.interfaces.ERDPickPageInterface;
import er.directtoweb.pages.ERD2WListPage;
import er.extensions.foundation.ERXValueUtilities;

public class R2D2WListPage extends ERD2WListPage implements ERDPickPageInterface {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;
	
	private WOComponent cancelPage;

    public R2D2WListPage(WOContext context) {
        super(context);
    }

	@Override
	public WOComponent cancelPage() {
		return cancelPage;
	}

	@Override
	public void setCancelPage(WOComponent cancelPage) {
		this.cancelPage = cancelPage;
	}

	@Override
	public void setChoices(NSArray choices) {
		displayGroup().setObjectArray(choices);
	}

	public boolean disableForm() {
		boolean hasForm = ERXValueUtilities.booleanValue(d2wContext().valueForKey("hasForm"));
		return context().isInForm() || !hasForm;
	}
}