package er.jquerymobile.components;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXNonSynchronizingComponent;

@SuppressWarnings("serial")
public class ERQMCollapsibleComponentContent extends ERXNonSynchronizingComponent {

  protected static final Logger log = Logger.getLogger(ERQMCollapsibleComponentContent.class);

  //********************************************************************
  //  Constructor
  //********************************************************************  

  public ERQMCollapsibleComponentContent(WOContext aContext) {
    super(aContext);
  }

}
