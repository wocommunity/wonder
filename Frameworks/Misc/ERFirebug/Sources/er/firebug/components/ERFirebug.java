package er.firebug.components;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXStatelessComponent;

/**
 * Firebug lite WebObjects Framework
 * 
 * http://getfirebug.com/lite.html
 * 
 * @author ishimoto
 */
@SuppressWarnings("serial")
public class ERFirebug extends ERXStatelessComponent {

  public static String VERSION = "1.4";

  //********************************************************************
  //	Constructor
  //********************************************************************

  public ERFirebug(WOContext context) {
    super(context);
  }

  @Override
  public boolean synchronizesVariablesWithBindings() {
    return false;
  }

  //********************************************************************
  //	Bindings
  //********************************************************************

  public boolean useFirebug() {
    return booleanValueForBinding("useFirebug", true);
  }

}