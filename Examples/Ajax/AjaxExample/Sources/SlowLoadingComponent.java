import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

public class SlowLoadingComponent extends DragAndDropExample {
	
	public String formValueC;

    public SlowLoadingComponent(WOContext context) {
        super(context);
    }

    @Override
	public void appendToResponse(WOResponse aResponse, WOContext aContext) {
		try {
			Thread.sleep(5000);
		}
		catch (InterruptedException e) {
		}
		super.appendToResponse(aResponse, aContext);
	}
}
