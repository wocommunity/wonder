package er.jquerymobile.components;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.extensions.components.ERXNonSynchronizingComponent;

@SuppressWarnings("serial")
public class ERQMGrid extends ERXNonSynchronizingComponent {

  protected static final Logger log = Logger.getLogger(ERQMGrid.class);

  //********************************************************************
  //  プロパティー
  //********************************************************************

  public static final NSArray<String> COLUMN_NAME = new NSArray<String>("a", "a", "a", "b", "c", "d"); // 0 + 1 element dummy

  //********************************************************************
  //  Constructor
  //********************************************************************

  public ERQMGrid(WOContext aContext) {
    super(aContext);
  }

  //********************************************************************
  //  Methods
  //********************************************************************

  public String columnIdentifer() {
    return "ui-grid-" + COLUMN_NAME.objectAtIndex(columnCount());
  }

  public int columnCount() {
    int i = intValueForBinding("columnCount", 2);
    if(i < 2) {
      i = 2;
    } else if(i > 5) {
      i = 5;
    }
    return i;
  }

}
