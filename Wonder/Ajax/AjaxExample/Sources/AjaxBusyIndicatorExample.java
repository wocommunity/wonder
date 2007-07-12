import com.webobjects.appserver.*;

public class AjaxBusyIndicatorExample extends WOComponent {
	public int numRequests = 0;

	public AjaxBusyIndicatorExample(WOContext context) {
        super(context);
    }
    
    public WOActionResults longRunningAction() throws InterruptedException{
    	System.out.println("AjaxBusyIndicatorExample.longRunningAction()");
    	Thread.sleep(3000);
    	numRequests+=1;
    	return null;
    }
}
