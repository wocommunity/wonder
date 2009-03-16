package er.iui.components;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver._private.WOConstantValueAssociation;
import com.webobjects.appserver._private.WOHyperlink;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.appserver.ERXProxyAssociation;

public class ERIUIBackButton extends WOHyperlink {
  public ERIUIBackButton(String name, NSDictionary associations, WOElement template) {
    super(name, ERIUIBackButton.processAssociations(associations, template), template);
  }

  @SuppressWarnings("unchecked")
  protected static NSDictionary processAssociations(NSDictionary associations, WOElement template) {
    NSMutableDictionary mutableAssociations = (NSMutableDictionary) associations;
    mutableAssociations.setObjectForKey(new WOConstantValueAssociation("backButton"), "id");
    WOAssociation classAssociation = (WOAssociation) mutableAssociations.objectForKey("class");
    mutableAssociations.setObjectForKey(new ERXProxyAssociation(classAssociation, "button transitionPrevious ", null, true), "class");
    if (!associations.containsKey("string") && template == null) {
      mutableAssociations.setObjectForKey(new WOConstantValueAssociation("Back"), "string");
    }
    return mutableAssociations;
  }

}