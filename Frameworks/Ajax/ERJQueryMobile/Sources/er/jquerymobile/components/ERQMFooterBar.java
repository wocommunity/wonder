package er.jquerymobile.components;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXStatelessComponent;
import er.extensions.foundation.ERXStringUtilities;

@SuppressWarnings("serial")
public class ERQMFooterBar extends ERXStatelessComponent {

  //********************************************************************
  //  Constructor
  //********************************************************************

  public ERQMFooterBar(WOContext aContext) {
    super(aContext);
  }

  //********************************************************************
  //  Methods
  //********************************************************************

  public String html() {
    StringBuilder sb = new StringBuilder();

    sb.append("data-role=\"footer\" ");

    if(!booleanValueForBinding("isNavBar", false)) {
      sb.append("class=\"ui-bar\" ");
    }

    if(booleanValueForBinding("isFixed", false)) {
      sb.append("data-id=\"erqmfooterbar\" data-position=\"fixed\" ");
    }

    String s = stringValueForBinding("theme", null); 
    if(!ERXStringUtilities.stringIsNullOrEmpty(s)) {
      sb.append("data-theme=\"" + s + "\"");
    }

    return sb.toString();
  }
}
