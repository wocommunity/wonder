package er.xiss;

import org.apache.log4j.Logger;

import er.extensions.ERXFrameworkPrincipal;

public class ERXiss extends ERXFrameworkPrincipal {

  /** ログ・サポート */
  private static Logger _log; // まだロッガーが初期化していない

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

  @Override
  protected void initialize() {
    // 念のためにログに書き出す
    System.out.println("doing now " + frameworkName() + ".initialize() for setup the Framework.");
  }

  public void finishInitialization() {
    // ログ・サポート
    _log = Logger.getLogger(ERXiss.class);
    if(_log.isDebugEnabled())
      _log.debug("doing now " + frameworkName() + ".finishInitialization() for setup the Framework.");

  }

}
