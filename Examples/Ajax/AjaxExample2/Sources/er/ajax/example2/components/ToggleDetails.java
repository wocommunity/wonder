package er.ajax.example2.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;

public class ToggleDetails extends AjaxWOWODCPage {
  public boolean _optionsVisible;
  
  public ToggleDetails(WOContext context) {
    super(context);
  }
  
  @Override
  protected boolean useDefaultComponentCSS() {
    return true;
  }
  
  public WOActionResults toggleOptions() {
    _optionsVisible = !_optionsVisible;
    return null;
  }
}