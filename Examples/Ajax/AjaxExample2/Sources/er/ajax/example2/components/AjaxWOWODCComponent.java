package er.ajax.example2.components;

import com.webobjects.appserver.WOContext;

import er.ajax.example2.Session;
import er.extensions.components.ERXComponent;

public class AjaxWOWODCComponent extends ERXComponent {
  public AjaxWOWODCComponent(WOContext context) {
    super(context);
  }

  @Override
  protected boolean isPageAccessAllowed() {
    return false;
  }

  @Override
  public Session session() {
    return (Session) super.session();
  }
}
