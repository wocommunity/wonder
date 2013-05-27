package er.ajax;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

import er.extensions.appserver.ERXWOContext;

public class AjaxAccordionTab extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

  public AjaxAccordionTab(WOContext context) {
    super(context);
  }

  @Override
  public boolean isStateless() {
    return true;
  }

  public String tabID() {
    String id = (String) valueForBinding("id");
    if (id == null) {
      id = ERXWOContext.safeIdentifierName(context(), false) + "Tab";
    }
    return id;
  }

  public String headerID() {
    return ERXWOContext.safeIdentifierName(context(), false) + "Header";
  }

  public String contentID() {
    return ERXWOContext.safeIdentifierName(context(), false) + "Content";
  }
}
