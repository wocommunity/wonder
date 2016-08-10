package er.uber;

import ognl.webobjects.WOOgnl;
import er.extensions.appserver.ERXApplication;
import er.extensions.localization.ERXLocalizerAssociation;
import er.taggable.ERTaggableEntity;
import er.uber.model.Employee;

public class Application extends ERXApplication {
  public static void main(String[] argv) {
    ERXApplication.main(argv, Application.class);
  }

  public Application() {
    WOOgnl.setAssociationClassForPrefix(ERXLocalizerAssociation.class, "loc");

    setAllowsConcurrentRequestHandling(true);

    ERTaggableEntity.registerTaggable(Employee.ENTITY_NAME);
  }

  @Override
  public String _rewriteURL(String url) {
    String finalURL = super._rewriteURL(url);
    if (finalURL != null) {
      finalURL = finalURL.replaceFirst("/wa/rewriteBroken", "/wa/rewriteWorked");
    }
    return finalURL;
  }
}
