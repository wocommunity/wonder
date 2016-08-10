package er.jquerymobile.components;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXNonSynchronizingComponent;

@SuppressWarnings("serial")
public class ERQMGrouping extends ERXNonSynchronizingComponent {

  //********************************************************************
  //  Constructor
  //********************************************************************

  public ERQMGrouping(WOContext aContext) {
    super(aContext);
  }

  //********************************************************************
  //  Methods
  //********************************************************************

  public String html() {
    StringBuilder sb = new StringBuilder();
    sb.append("data-role=\"controlgroup\" ");
    sb.append(isHorizontal());
    return sb.toString();
  }

  public String inset() {
    if(booleanValueForBinding("inset", false)) {
      return "data-role=\"fieldcontain\" "; 
    }
    return null;
  }

  public String isHorizontal() {
    if(booleanValueForBinding("isHorizontal", false)) {
      return "data-type=\"horizontal\" "; 
    }
    return null;
  }

}
