import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSTimestamp;

public class AjaxBusyIndicatorExample extends WOComponent {
	public int numRequests = 0;

	public AjaxBusyIndicatorExample(WOContext context) {
        super(context);
    }
    
	public NSTimestamp now() {
		return new NSTimestamp();
	}
	
    public WOActionResults longRunningAction() throws InterruptedException{
    	System.out.println("AjaxBusyIndicatorExample.longRunningAction()");
    	Thread.sleep(3000);
    	numRequests+=1;
    	return null;
    }
}
