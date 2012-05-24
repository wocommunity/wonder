package er.jquerymobile.components;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXStatelessComponent;

@SuppressWarnings("serial")
public class ERQMDialog extends ERXStatelessComponent {

  protected static final Logger log = Logger.getLogger(ERQMDialog.class);

  //********************************************************************
  //  Constructor
  //********************************************************************

  public ERQMDialog(WOContext aContext) {
    super(aContext);
  }

}
