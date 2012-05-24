package er.jquerymobile.components;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXNonSynchronizingComponent;

@SuppressWarnings("serial")
public class ERQMCollapsibleComponentContentGroup extends ERXNonSynchronizingComponent {

  protected static final Logger log = Logger.getLogger(ERQMCollapsibleComponentContentGroup.class);

  //********************************************************************
  //  Constructor
  //********************************************************************  

  public ERQMCollapsibleComponentContentGroup(WOContext aContext) {
    super(aContext);
  }

}
