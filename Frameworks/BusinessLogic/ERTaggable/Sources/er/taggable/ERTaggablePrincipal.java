package er.taggable;

import er.extensions.ERXExtensions;
import er.extensions.ERXFrameworkPrincipal;

/**
 * ERTaggable Framework Principal
 * 
 * @author mschrag
 */
public class ERTaggablePrincipal extends ERXFrameworkPrincipal {
  public final static Class[] REQUIRES = new Class[] { ERXExtensions.class };

  static {
    setUpFrameworkPrincipalClass(ERTaggablePrincipal.class);
  }

  @Override
  public void finishInitialization() {
    // DO NOTHING
  }
}
