package er.jquerymobile.components;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;

/**
 * OL or UL with data-role="listview"
 * 
 * data-count-theme - swatch letter (a-z)
 * data-dividertheme - swatch letter (a-z)
 * data-filter - true | [false]
 * data-filter-placeholder - string
 * data-filter-theme - swatch letter (a-z)
 * data-inset - true | [false]
 * data-split-icon - home | delete | plus | arrow-u | arrow-d | check | gear | grid | star | custom | arrow-r | arrow-l | minus | refresh | forward | back | alert | info | search
 * data-split-theme - swatch letter (a-z)
 * data-theme - swatch letter (a-z)
 */
@SuppressWarnings("serial")
public class ERQMListView extends ERQMInputBaseComponent {

  protected static final Logger log = Logger.getLogger(ERQMListView.class);

  //********************************************************************
  //  Constructor
  //********************************************************************

  public ERQMListView(WOContext aContext) {
    super(aContext);
  }

  //********************************************************************
  //  Methods
  //********************************************************************

  private boolean isNumberedList() {
    return booleanValueForBinding("isNumberedList", false);
  }

  public String elementName() {
    return isNumberedList() ? "ol" : "ul";
  }

}
