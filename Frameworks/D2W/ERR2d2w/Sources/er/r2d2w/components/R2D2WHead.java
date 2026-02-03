package er.r2d2w.components;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.ERD2WContext;
import com.webobjects.foundation.NSSelector;

import er.directtoweb.ERD2WFactory;
import er.directtoweb.components.ERD2WStatelessComponent;
import er.extensions.eof.ERXKey;

public class R2D2WHead extends ERD2WStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	protected static final D2WContext _d2wContext = 
			ERD2WContext.newContext();
	
	private static final NSSelector<D2WContext> _sel = 
		new NSSelector<D2WContext>("d2wContext");
	private static final ERXKey<D2WContext> _d2wContextKey = 
		new ERXKey<D2WContext>("d2wContext");
	private static final ERXKey<String> _displayNameForPageConfigurationKey = 
		new ERXKey<String>("displayNameForPageConfiguration");

	public R2D2WHead(WOContext context) {
		super(context);
	}

	public String displayNameForPageConfiguration() {
		WOComponent page = context().page();
		if (_sel.implementedByObject(page)) {
			D2WContext context = _d2wContextKey.valueInObject(page);
			return _displayNameForPageConfigurationKey.valueInObject(context);
		} else {
			synchronized (_d2wContext) {
				String dynamicPage = ERD2WFactory.pageConfigurationFromPage(page);
				_d2wContext.setDynamicPage(dynamicPage);
				return _displayNameForPageConfigurationKey.valueInObject(_d2wContext);
			}
		}
	}

}