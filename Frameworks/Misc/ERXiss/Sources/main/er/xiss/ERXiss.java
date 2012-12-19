package er.xiss;

import er.extensions.ERXFrameworkPrincipal;

public class ERXiss extends ERXFrameworkPrincipal {

  //********************************************************************
  //  フレームワーク情報
  //********************************************************************

  public static String frameworkName() {
    return "ERXiss";
  }

  //********************************************************************
  //	ERXFrameworkPrincipal
  //********************************************************************

  @SuppressWarnings("rawtypes")
  public final static Class REQUIRES[] = new Class[] { };

  static {
    setUpFrameworkPrincipalClass(ERXiss.class);
  }

  public void finishInitialization() {
    log.debug("ERXiss loaded");
  }
}
