package er.uber.components;

import com.webobjects.appserver.WOContext;

public class UberHeaderFooter extends UberComponent {
  public UberHeaderFooter(WOContext context) {
    super(context);
  }

  @Override
  public boolean synchronizesVariablesWithBindings() {
    return false;
  }
}