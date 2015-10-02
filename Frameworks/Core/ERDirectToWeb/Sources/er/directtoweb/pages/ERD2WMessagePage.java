/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.pages;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOMessage;
import com.webobjects.directtoweb.NextPageDelegate;

import er.directtoweb.interfaces.ERDErrorPageInterface;
import er.directtoweb.interfaces.ERDMessagePageInterface;

/**
 * Superclass for all message pages.
 * <p>
 * If the key <code>explanationComponentName</code> resolves to non-empty, then
 * this component will get shown in the page and wired up with a 
 * <code>object</code>, <code>dataSource</code> and <code>pageConfiguration</code> binding.
 * @d2wKey displayNamePageConfiguration
 * @d2wKey messageTitleForPage
 * @d2wKey explanationComponentName
 */
public abstract class ERD2WMessagePage extends ERD2WPage implements ERDMessagePageInterface, ERDErrorPageInterface {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    public final static Logger log = Logger.getLogger(ERD2WMessagePage.class);
    
    protected String _message;
    protected String _title;
    protected WOComponent _cancelPage;
    protected NextPageDelegate _cancelDelegate;
    protected Exception _exception;

    /**
     * Public constructor
     * @param c current context
     */
    public ERD2WMessagePage(WOContext c) {
        super(c);
    }
    
	public WOComponent cancelAction() {
		return (cancelDelegate() != null)
				? cancelDelegate().nextPage(this)
				: cancelPage();
	}
	public WOComponent confirmAction() {
		return errorMessages.count() == 0 ? nextPageAction() : null;
	}
	public WOComponent nextPageAction() {
        WOComponent result = nextPageFromDelegate();
    	if(result == null) {
    		result = nextPage();
    	}
        return result;
	}
	public void setException(Exception exception) {
		_exception = exception;
	}
	public Exception exception() {
		return _exception;
	}
	public void setMessage(String message) {
		_message = message;
	}
	public String message() {
		return _message;
	}
	public String formattedMessage() {
		return WOMessage.stringByEscapingHTMLString(message());
	}
    
    public String title() {
        if(_title == null) {
            _title = (String) d2wContext().valueForKey("displayNameForPageConfiguration");
        }
        return _title;
    }
    public void setTitle(String title) {
        _title = title;
    }
    
	public void setCancelPage(WOComponent cancelPage) {
		_cancelPage = cancelPage;
	}
	public WOComponent cancelPage() {
		return _cancelPage;
	}
	public void setCancelDelegate(NextPageDelegate cancelDelegate) {
		_cancelDelegate = cancelDelegate;
	}
	public NextPageDelegate cancelDelegate() {
		return _cancelDelegate;
	}
	public void setConfirmPage(WOComponent confirmPage) {
		setNextPage(confirmPage);
	}
	public WOComponent confirmPage() {
		return nextPage();
	}
	public void setConfirmDelegate(NextPageDelegate confirmPageDelegate) {
		setNextPageDelegate(confirmPageDelegate);
	}
	public NextPageDelegate confirmDelegate() {
		return nextPageDelegate();
	}
    
    // CHECKME ak: do we really need this? It's never referenced in the templates? 
    public String titleForPage() {
    	String title = (String)d2wContext().valueForKey("messageTitleForPage");
    	return title != null ? title : title();
    }

    public boolean hasNextPage() {
        return !(nextPage() == null && nextPageDelegate() == null);
    }
    public boolean hasCancelPage() {
        return !(cancelPage() == null && cancelDelegate() == null);
    }
    
    public boolean showExplanationComponent() {
    	// AK: this is needed because RuleEditor won't save NULL keys anymore
    	String name = (String)d2wContext().valueForKey("explanationComponentName");
    	boolean result = name != null && name.length() > 0;
    	// CHECKME: AK could be extended to check if object() or dataSource() are bound...
    	return result;
    }
    
}
