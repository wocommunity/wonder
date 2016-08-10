import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSTimestamp;

public class HelloAjaxWorld extends WOComponent {
	private NSTimestamp _timestamp;
	private String _helloWorld;
	private String _helloAjaxWorld;
	
	public HelloAjaxWorld(WOContext context) {
		super(context);
		_helloWorld = "Hello WOrld.";
		_helloAjaxWorld = "Hello Ajax WOrld!";
	}

	@Override
	public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
		_timestamp = new NSTimestamp();
		super.appendToResponse(woresponse, wocontext);
	}

	public NSTimestamp timestamp() {
		return _timestamp;
	}
	
	public String helloWorld() {
		return _helloWorld;
	}

	public String helloAjaxWorld() {
		return _helloAjaxWorld;
	}

	public WOActionResults updateHelloWorld() {
		_helloWorld = _helloWorld + " <i>Boring.</i>";
		return null;
	}

	public WOActionResults updateHelloAjaxWorld() {
		_helloAjaxWorld = _helloAjaxWorld + " <b>Fun!</b>";
		return null;
	}
}
