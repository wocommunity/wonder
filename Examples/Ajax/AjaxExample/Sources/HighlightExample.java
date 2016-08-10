import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.ajax.AjaxHighlight;
import er.ajax.example.Comment;
import er.ajax.example.ExampleDataFactory;

public class HighlightExample extends WOComponent {
	private NSMutableArray<Comment> _comments;
	private Comment _comment;
	public Comment _repetitionComment;

	public HighlightExample(WOContext context) {
		super(context);
		_comments = ExampleDataFactory.comments(4);
	}

	public NSArray comments() {
		return _comments;
	}

	public Comment comment() {
		if (_comment == null) {
			_comment = new Comment();
		}
		return _comment;
	}

	public WOActionResults save() {
		AjaxHighlight.highlight(_comment);
		_comments.addObject(_comment);
		_comment = null;
		return null;
	}
}
