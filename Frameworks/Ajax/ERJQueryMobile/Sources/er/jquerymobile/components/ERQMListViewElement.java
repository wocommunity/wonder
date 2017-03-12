package er.jquerymobile.components;

import com.webobjects.appserver.WOContext;

import er.extensions.eof.ERXConstant;
import er.extensions.foundation.ERXStringUtilities;

/**
 * LI within a listview
 * 
 * data-filtertext - string (filter by this value instead of inner text)
 * data-icon - home | delete | plus | arrow-u | arrow-d | check | gear | grid | star | custom | arrow-r | arrow-l | minus | refresh | forward | back | alert | info | search
 * data-role - list-divider
 * data-theme  - swatch letter (a-z) - can also be set on individual LIs
 * 
 * @author ishimoto
 *
 */
@SuppressWarnings("serial")
public class ERQMListViewElement extends ERQMInputBaseComponent {

  //********************************************************************
  //  Constructor
  //********************************************************************

  public ERQMListViewElement(WOContext aContext) {
    super(aContext);
  }

  @Override
  public boolean isStateless() {
    return true;
  }

  @Override
  public void reset() {
    super.reset();

    filtertext = null;
    autoDivider = false;
  }

  //********************************************************************
  //  Methods
  //********************************************************************

  public boolean hasCountBubble() {
    return countBubble() > ERXConstant.MinusOneInteger;
  }

  public Integer countBubble() {
    int i = intValueForBinding("countBubble", ERXConstant.MinusOneInteger);
    return Integer.valueOf(i);
  }

  public boolean hasSideText() {
    return !ERXStringUtilities.stringIsNullOrEmpty(sideText());
  }

  public String sideText() {
    return stringValueForBinding("sideText", null);
  }

  public boolean isDivider() {
    return booleanValueForBinding("isDivider", false);
  }

  public String dataRel() {
	return (booleanValueForBinding("isDialogCall", false))?"dialog":null;
  }

  public boolean hasAction() {
    return hasBinding("action");
  }

  public boolean hasLinkResource() {
    return hasBinding("linkResource");
  }

  public String filtertext() {
    if(filtertext == null) {
      filtertext = stringValueForBinding("filtertext", null);

      // first Line
      if(ERXStringUtilities.stringIsNullOrEmpty(oldValue)) {
        setAutoDivider(true);
      }

      // First Charge has Changed ?
      if(!ERXStringUtilities.stringIsNullOrEmpty(oldValue) && !ERXStringUtilities.stringIsNullOrEmpty(filtertext)) {
        setAutoDivider(oldValue.charAt(0) != filtertext.charAt(0));
      }
      oldValue = filtertext;
    }
    return filtertext;
  }
  private String filtertext;
  public String oldValue = null;

  public char firstLetter() {
    if(ERXStringUtilities.stringIsNullOrEmpty(filtertext())) {
      return '*';
    }
    if(filtertext().charAt(0) == ' ') {
      return '*';
    }
    return filtertext().charAt(0);
  }

  public void setAutoDivider(boolean autoDivider) {
    this.autoDivider = autoDivider;
  }
  public boolean autoDivider() {
    return autoDivider;
  }
  private boolean autoDivider = false;

  public boolean hasAutomaticDivider() {
    boolean b = booleanValueForBinding("automaticDivider", false);
    if(b)
	{
		filtertext();
	}

    return b;
  }

  public String html() {
    StringBuilder sb = new StringBuilder();

    String s = stringValueForBinding("theme");
    if(!ERXStringUtilities.stringIsNullOrEmpty(s)) {
      sb.append("data-theme='" + s + "' ");
    }

    if(!ERXStringUtilities.stringIsNullOrEmpty(filtertext())) {
      sb.append("data-filtertext='" + filtertext() + "' ");
    }

    if(isDivider()) {
      sb.append("data-role='list-divider' ");
    } else if(booleanValueForBinding("isInputContainer", false)) {
      sb.append("data-role='fieldcontain' ");
    }

    s = stringValueForBinding("icon");
    if(!ERXStringUtilities.stringIsNullOrEmpty(s)) {
      sb.append("data-icon='" + s + "' ");
    }
    
    return sb.toString();
  }
}
