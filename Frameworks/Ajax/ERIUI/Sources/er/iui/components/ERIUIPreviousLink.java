package er.iui.components;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver._private.WOHyperlink;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.appserver.ERXProxyAssociation;

public class ERIUIPreviousLink extends WOHyperlink {
  public ERIUIPreviousLink(String name, NSDictionary associations, WOElement template) {
    super(name, ERIUIPreviousLink.processAssociations(associations, template), template);
  }

  @SuppressWarnings("unchecked")
  protected static NSDictionary processAssociations(NSDictionary associations, WOElement template) {
    NSMutableDictionary mutableAssociations = (NSMutableDictionary) associations;
    WOAssociation classAssociation = (WOAssociation) mutableAssociations.objectForKey("class");
    mutableAssociations.setObjectForKey(new ERXProxyAssociation(classAssociation, "transitionPrevious", null, true), "class");
    return mutableAssociations;
  }

}