package er.r2d2w.components;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WPage;

import er.directtoweb.components.ERD2WStatelessComponent;

public class R2DBreadCrumbTrail extends ERD2WStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public R2DBreadCrumbTrail(WOContext context) {
        super(context);
    }

	public Boolean showTrail() {
		WOComponent page = (WOComponent) valueForBinding("page");
		if (page instanceof D2WPage) {
			D2WPage d2wPage = (D2WPage) page;
			return (d2wPage.nextPage() != null);
		}
		return false;
	}
}