package er.httpclient;

import org.apache.log4j.Logger;

import er.extensions.ERXFrameworkPrincipal;

/** http://hc.apache.org */
public class ERHttpClient extends ERXFrameworkPrincipal {

  /**
   * <span class="en">At this Point there is no Logger yet</span>
   * <span class="ja">まだロッガーが初期化していない</span>
   */
  private static Logger _log;

  //********************************************************************
  //	Framewrok Information
  //********************************************************************

  public static String frameworkName() {
    return "ERHttpClient";
  }

  //********************************************************************
  //	ERXFrameworkPrincipal Implementation
  //********************************************************************

  public final static Class[] REQUIRES = new Class[] { };

  static {
    setUpFrameworkPrincipalClass(ERHttpClient.class);
  }

  @Override
  protected void initialize() {
    System.out.println("doing now "+ frameworkName() + ".initialize() for setup the Framework.");
  }

  public void finishInitialization() {
    _log = Logger.getLogger(ERHttpClient.class);
    if(_log.isDebugEnabled())
      _log.debug("doing now "+ frameworkName() + ".finishInitialization() for setup the Framework.");
  }
}
