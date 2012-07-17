package er.jquerymobile.components;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.woextensions.WORadioButtonMatrix;

@SuppressWarnings("serial")
public class ERQMRadioList extends WORadioButtonMatrix {

  protected static final Logger log = Logger.getLogger(ERQMRadioList.class);

  //********************************************************************
  //  Constructor
  //********************************************************************

  public ERQMRadioList(WOContext aContext) {
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
