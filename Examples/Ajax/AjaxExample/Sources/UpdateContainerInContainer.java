import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSTimestamp;

public class UpdateContainerInContainer extends WOComponent {

	public UpdateContainerInContainer(WOContext context) {
		super(context);
	}

	public NSTimestamp now() {
		return new NSTimestamp();
	}
}
