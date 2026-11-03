package er.jquerymobile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import er.extensions.ERXExtensions;
import er.extensions.ERXFrameworkPrincipal;

/**
 * er.jquerymobile.ERJQueryMobile
 */
public class ERJQueryMobile extends ERXFrameworkPrincipal {

  private static Logger _log; // まだロッガーが初期化していない

  //********************************************************************
  //  Framework Info
  //********************************************************************

  public static String frameworkName() {
    return "ERJQueryMobile";
  }

  //********************************************************************
  //  ERXFrameworkPrincipal
  //********************************************************************

  @SuppressWarnings("rawtypes")
  public final static Class REQUIRES[] = new Class[] { };

  static {
    setUpFrameworkPrincipalClass(ERJQueryMobile.class);
  }

  @Override
  protected void initialize() {
    // 念のためにログに書き出す
    System.out.println("doing now " + frameworkName() + ".initialize() for setup the Framework.");
  }

  @Override
  public void finishInitialization() {
    // ログ・サポート
    _log = LoggerFactory.getLogger(ERXExtensions.class);
    if(_log.isDebugEnabled())
      _log.debug("doing now " + frameworkName() + ".finishInitialization() for setup the Framework.");

  }
}
