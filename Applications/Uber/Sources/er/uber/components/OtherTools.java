package er.uber.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;

import er.chronic.Chronic;
import er.chronic.utils.Span;
import er.extensions.foundation.ERXThreadStorage;

public class OtherTools extends UberComponent {
  public Span _dateSpan;
  public String _dateString;

  public OtherTools(WOContext context) {
    super(context);
  }

  public WOActionResults resetThreadValue() {
    ERXThreadStorage.takeValueForKey("A Thread Value", "initialValue");
    return null;
  }

  public String threadStorageValue() {
    return (String) ERXThreadStorage.valueForKey("initialValue");
  }

  public WOActionResults reload() {
    return null;
  }

  public WOActionResults chronic() {
    _dateSpan = Chronic.parse(_dateString);
    return null;
  }
}