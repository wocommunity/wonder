package er.jquerymobile.components;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;

@SuppressWarnings("serial")
public class ERQMInputMail extends ERQMInputBaseComponent {

  protected static final Logger log = Logger.getLogger(ERQMInputMail.class);

  //********************************************************************
  //  Constructor
  //********************************************************************

  public ERQMInputMail(WOContext aContext) {
    super(aContext);
  }

}
