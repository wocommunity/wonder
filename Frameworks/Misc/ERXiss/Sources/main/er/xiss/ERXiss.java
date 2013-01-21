package er.xiss;

import er.extensions.ERXFrameworkPrincipal;

public class ERXiss extends ERXFrameworkPrincipal {

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

  @Override
  public void finishInitialization() {
    log.debug("ERXiss loaded");
  }
}
