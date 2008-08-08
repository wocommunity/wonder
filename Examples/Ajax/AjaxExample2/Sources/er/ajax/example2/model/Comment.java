package er.ajax.example2.model;

import com.webobjects.foundation.NSTimestamp;

public class Comment {
  private NSTimestamp _creationDate;
  private String _text;

  public Comment() {
    _creationDate = new NSTimestamp();
  }

  public NSTimestamp creationDate() {
    return _creationDate;
  }

  public void setText(String text) {
    _text = text;
  }

  public String text() {
    return _text;
  }
}
