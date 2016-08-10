package er.ajax.example2.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;

import er.ajax.AjaxHighlight;

public class AjaxForm extends AjaxWOWODCPage {
  public String _firstName;
  public String _lastName;
  public boolean _likesStuff;

  public AjaxForm(WOContext context) {
    super(context);
  }

  @Override
  protected boolean useDefaultComponentCSS() {
    return true;
  }

  public WOActionResults formChanged() {
    AjaxHighlight.highlight("formSummary");
    return null;
  }
}