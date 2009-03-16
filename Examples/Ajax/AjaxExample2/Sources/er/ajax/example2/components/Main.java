package er.ajax.example2.components;

import com.webobjects.appserver.WOContext;

public class Main extends AjaxWOWODCPage {
  public Main(WOContext context) {
    super(context);
  }
  
  @Override
  protected boolean useDefaultComponentCSS() {
    return true;
  }
}
