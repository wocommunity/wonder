package er.jquerymobile.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.extensions.components.ERXNonSynchronizingComponent;
import er.extensions.foundation.ERXStringUtilities;

@SuppressWarnings("serial")
public class ERQMGridBlock extends ERXNonSynchronizingComponent {

  //********************************************************************
  //  Properies
  //********************************************************************

  public static final NSArray<String> BLOCK_NAME = new NSArray<String>("a", "a", "b", "c", "d", "e"); // 0 element dummy

  //********************************************************************
  //  Constructor
  //********************************************************************

  public ERQMGridBlock(WOContext aContext) {
    super(aContext);
  }

  //********************************************************************
  //  Methods
  //********************************************************************

  private int blockNumber() {
    int i = intValueForBinding("blockNumber", 1);
    if(i < 1) {
      i = 1;
    } else if(i > 5) {
      i = 5;
    }
    return i;
  }

  public String blockIdentifer() {
    return "ui-block-" + BLOCK_NAME.objectAtIndex(blockNumber());
  }

  public String hasTheme() {
    String s = stringValueForBinding("theme");
    if(ERXStringUtilities.stringIsNullOrEmpty(s)) {
      return null;
    }
    return "ui-bar ui-bar-" + s;
  }

}
