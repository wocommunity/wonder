package er.jquerymobile.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.woextensions.WOCheckboxMatrix;

@SuppressWarnings("serial")
public class ERQMCheckboxList extends WOCheckboxMatrix {

  //********************************************************************
  //  Constructor
  //********************************************************************

  public ERQMCheckboxList(WOContext aContext) {
    super(aContext);
  }

  //********************************************************************
  //  Methods
  //********************************************************************

  public String html() {
    StringBuilder sb = new StringBuilder();
    if(valueForBooleanBinding("isHorizontal", false)) {
      sb.append("data-type=\"horizontal\" ");
    }

    if(valueForBooleanBinding("mini", false)) {
      sb.append("data-mini=\"true\"");
    }

    return sb.toString();
  }

  public String id() {
    return wrapperElementID + String.valueOf(index);
  }

}
