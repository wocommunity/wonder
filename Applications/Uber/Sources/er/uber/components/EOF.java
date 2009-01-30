package er.uber.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;

public class EOF extends UberComponent {
  public EOF(WOContext context) {
    super(context);
  }

  public WOActionResults reload() {
    return null;
  }

  public WOActionResults lock() {
    editingContext().lock();
    return null;
  }
}