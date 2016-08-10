package er.jquerymobile.components;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXNonSynchronizingComponent;
import er.extensions.foundation.ERXStringUtilities;

@SuppressWarnings("serial")
public class ERQMInputBaseComponent extends ERXNonSynchronizingComponent {

  //********************************************************************
  //  Constructor
  //********************************************************************

  public ERQMInputBaseComponent(WOContext aContext) {
    super(aContext);
  }

  //********************************************************************
  //  Methods
  //********************************************************************

  public String javaScriptElementID() {
    if(ERXStringUtilities.stringIsNullOrEmpty(elementID)) {
		elementID = stringValueForBinding("id", context().javaScriptElementID());
	}

    return elementID;
  }
  private String elementID = null;

  public String inset() {
    if(booleanValueForBinding("inset", false)) {
      return "data-role=\"fieldcontain\""; 
    }
    return null;
  }

  public String elementClass() {
    if(hideLabel()) {
      return "ui-hide-label";
    }   
    return "ui-input-text";
  }

  private boolean hideLabel() {
    return booleanValueForBinding("hideLabel", false);
  }

  public String miniVersion() {
    if(booleanValueForBinding("mini", false)) {
      return "data-mini=\"true\""; 
    }
    return null;
  }
}
