import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

public class LivePreview extends WOComponent {
	private String _comment;

	public LivePreview(WOContext context) {
		super(context);
	}

	public void setComment(String comment) {
		_comment = comment;
	}

	public String getComment() {
		return _comment;
	}

	public WOActionResults preview() {
		return null;
	}

	public WOActionResults save() {
		_comment = null;
		return null;
	}
}
