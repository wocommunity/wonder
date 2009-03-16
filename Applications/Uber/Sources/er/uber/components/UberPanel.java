package er.uber.components;

import com.webobjects.appserver.WOContext;

public class UberPanel extends UberComponent {
  public UberPanel(WOContext context) {
    super(context);
  }

  @Override
  public boolean synchronizesVariablesWithBindings() {
    return false;
  }
}