import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSTimestamp;

import er.ajax.AjaxUtils;

public class AjaxTabbedPanelDemo extends WOComponent {

    private boolean _isRefreshingContentSelected;
    public String formValueA;
    public String formValueB;
    public String formValueC;
    
	public AjaxTabbedPanelDemo(WOContext context) {
        super(context);
    }


    public boolean isSlowLoadingContentSelected() {
    	return "SlowLoadingContent".equals(theSession().selectedTab);
    }

    public void setIsSlowLoadingContentSelected(boolean isSelected) {
    	if (isSelected) theSession().selectedTab = "SlowLoadingContent";
    }

    public boolean isInlineContentSelected() {
    	return "InlineContent".equals(theSession().selectedTab);
    }

    public void setIsInlineContentSelected(boolean isSelected) {
    	if (isSelected) theSession().selectedTab = "InlineContent";
    }


    public boolean isRefreshingContentSelected() {
    	return "RefreshingContent".equals(theSession().selectedTab);
    }

    public void setIsRefreshingContentSelected(boolean isSelected) {
    	if (isSelected) theSession().selectedTab = "RefreshingContent";
    }

    public Session theSession() {
    	return (Session)session();
    }

    /**
     * Add script used in component used on tab to page.  If only there was a better way to do this.
     *
     * @see com.webobjects.appserver.WOComponent#appendToResponse(com.webobjects.appserver.WOResponse, com.webobjects.appserver.WOContext)
     *
     * @param response {@link WOResponse} being appended to
     * @param context {@link WOContext} of the response
     */
    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
    	// effects.js and dragdrop.js are not used by AjaxTabbedPanel but are used by the components on SlowLoadingComponent
    	// BUT as SlowLoadingComponent is not rendered when the page is first shown, the Ajax framework has no
    	// way to know that the scripts for the Ajax components that will be used on that tab need to be loaded.
    	// Those objects don't even exist yet, so there is no way for them to tell what is needed.
    	// Note also that only dragging works, I am not sure yet what is wrong with the dropping.
    	AjaxUtils.addScriptResourceInHead(context, response, "effects.js");
    	AjaxUtils.addScriptResourceInHead(context, response, "dragdrop.js");

    	super.appendToResponse(response, context);
    }

    public NSTimestamp now() {
    	return new NSTimestamp();
    }

    public WOComponent save() {
    	System.out.println("formValueA " + formValueA);
    	System.out.println("formValueB " + formValueB);
    	System.out.println("formValueB " + formValueC);
    	return context().page();
    }
}
