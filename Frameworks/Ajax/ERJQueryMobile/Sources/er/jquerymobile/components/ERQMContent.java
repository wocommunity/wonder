package er.jquerymobile.components;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXStatelessComponent;

/**
 * data-role            content これを設定することでフッターになります。
 */
@SuppressWarnings("serial")
public class ERQMContent extends ERXStatelessComponent {

  protected static final Logger log = Logger.getLogger(ERQMContent.class);

  //********************************************************************
  //  Constructor
  //********************************************************************

  public ERQMContent(WOContext aContext) {
    super(aContext);
  }

}
