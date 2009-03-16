package er.uber.components;

import com.webobjects.appserver.WOContext;

import er.extensions.foundation.ERXExceptionUtilities;

public class WOExceptionPage extends com.webobjects.woextensions.WOExceptionPage {
  public WOExceptionPage(WOContext context) {
    super(context);
  }

  public String exceptionParagraph() {
    return ERXExceptionUtilities.toParagraph(exception);
  }
}