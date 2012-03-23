package er.ajax.mootools.example.components;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSTimestamp;

import er.ajax.mootools.example.app.Session;

public class MTAjaxTabbedPanelTestPage extends Main {
 
    public String formValueA;
    public String formValueB;
    public String formValueC;
	
	public MTAjaxTabbedPanelTestPage(WOContext context) {
        super(context);
    }
    
    public boolean isSlowLoadingContentSelected() {
    	System.out.println(theSession().selectedTab);
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