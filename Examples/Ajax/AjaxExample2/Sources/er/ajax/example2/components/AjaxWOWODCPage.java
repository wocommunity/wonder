package er.ajax.example2.components;

import com.webobjects.appserver.WOContext;

public class AjaxWOWODCPage extends AjaxWOWODCComponent {
  public AjaxWOWODCPage(WOContext context) {
    super(context);
  }

  @Override
  protected boolean isPageAccessAllowed() {
    return true;
  }
}
