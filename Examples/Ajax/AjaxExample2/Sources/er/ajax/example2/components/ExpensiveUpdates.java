package er.ajax.example2.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSMutableArray;

import er.ajax.example2.model.Comment;
import er.extensions.components.ERXLoremIpsumGenerator;

public class ExpensiveUpdates extends AjaxWOWODCPage {
  private NSMutableArray<Comment> _comments;
  public Comment _repetitionComment;
  public String _newComment;

  public ExpensiveUpdates(WOContext context) {
    super(context);
    _comments = new NSMutableArray<>();
    for (int i = 0; i < 5; i++) {
      Comment c = new Comment();
      c.setText(ERXLoremIpsumGenerator.sentences(1));
      _comments.addObject(c);
    }
  }

  @Override
  protected boolean useDefaultComponentCSS() {
    return true;
  }

  public NSMutableArray<Comment> comments() {
    return _comments;
  }

  public Object cacheKey() {
    return Integer.valueOf(_comments.count());
  }

  public WOActionResults addComment() {
    Comment comment = new Comment();
    comment.setText(_newComment);
    _newComment = null;
    _comments.addObject(comment);
    return null;
  }
}