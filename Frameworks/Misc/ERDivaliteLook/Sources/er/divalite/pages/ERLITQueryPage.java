package er.divalite.pages;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WContext;

import er.directtoweb.pages.ERD2WQueryPage;

/**
 * Divalite query page
 * 
 * @author ravim
 *
 */
public class ERLITQueryPage extends ERD2WQueryPage {

	public ERLITQueryPage(WOContext context) {
		super(context);
	}

	public String contentContainerID() {
		return subContext().valueForKey("id") + "_container";
	}
    
	protected D2WContext _subContext;
	
	public D2WContext subContext() {
		return _subContext;
	}
	
	public void setSubContext(D2WContext aContext) {
		_subContext = aContext;
	}
	
	/**
	 * Gives each property its own d2wContext rather than sharing one
	 * Necessary for ajax or dyanmic D2W in embedded configs
	 */
	@Override
	public void setPropertyKey(String propertyKey) {
		_subContext = new D2WContext(d2wContext());
		_subContext.takeValueForKey(propertyKey, "propertyKey");
	}
}
