package er.jquerymobile.components;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXStatelessComponent;

/**
 * data-role  header これを設定することでヘッダーになります。
 */
@SuppressWarnings("serial")
public class ERQMHeaderBar extends ERXStatelessComponent {

  //********************************************************************
  //  Constructor
  //********************************************************************

  public ERQMHeaderBar(WOContext aContext) {
    super(aContext);
  }

  @Override
  public boolean synchronizesVariablesWithBindings() {
    return false;
  }
  
  //********************************************************************
  //  Methods
  //********************************************************************

  /**
   * data-position
   * 
   * @return "fixed" 又は "inline"
   */
  public String dataPosition() {
    if(hasBinding("isFixed")) {
      return (booleanValueForBinding("isFixed", false) ? "fixed" : "inline");
    }
    return "inline";
  }

  public boolean hasAction() {
    return hasBinding("rightButton-action");
  }

  public boolean hasLinkResource() {
    return hasBinding("rightButton-linkResource");
  }

  public boolean hasBackButtonAction() {
	    return hasBinding("backButton-action");
	  }}
